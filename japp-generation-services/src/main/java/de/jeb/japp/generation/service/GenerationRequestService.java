package de.jeb.japp.generation.service;

import de.jeb.japp.ai.service.ProviderSettingsResolver;
import de.jeb.japp.ai.service.ResolvedProviderConfig;
import de.jeb.japp.commons.exceptions.ai.AiProviderNotFoundException;
import de.jeb.japp.commons.exceptions.cv.CVAccessDeniedException;
import de.jeb.japp.commons.exceptions.cv.CVNotFoundException;
import de.jeb.japp.commons.exceptions.generation.CoverLetterGenerationException;
import de.jeb.japp.commons.exceptions.generation.GenerationRequestAccessDeniedException;
import de.jeb.japp.commons.exceptions.generation.GenerationRequestNotFoundException;
import de.jeb.japp.commons.exceptions.generation.GenerationRequestValidationException;
import de.jeb.japp.commons.exceptions.job.JobAccessDeniedException;
import de.jeb.japp.commons.exceptions.job.JobNotFoundException;
import de.jeb.japp.dao.ai.AiProviderConfigurationDao;
import de.jeb.japp.dao.coverletter.CoverLetterDao;
import de.jeb.japp.dao.cv.CVDao;
import de.jeb.japp.dao.cv.CVProfileDao;
import de.jeb.japp.dao.generation.GenerationRequestDao;
import de.jeb.japp.dao.job.JobDao;
import de.jeb.japp.generation.service.provider.CoverLetterGenerationAdapter;
import de.jeb.japp.generation.service.provider.CoverLetterGenerationAdapterRegistry;
import de.jeb.japp.generation.service.provider.GenerationInput;
import de.jeb.japp.generation.service.provider.GenerationResult;
import de.jeb.japp.model.ai.AdapterType;
import de.jeb.japp.model.ai.AiProviderConfiguration;
import de.jeb.japp.model.coverLetter.CoverLetter;
import de.jeb.japp.model.cv.CVDocument;
import de.jeb.japp.model.cv.CVProfile;
import de.jeb.japp.model.cv.Experience;
import de.jeb.japp.model.cv.Language;
import de.jeb.japp.model.cv.ProfileGenerationStatus;
import de.jeb.japp.model.cv.Skill;
import de.jeb.japp.model.generation.GenerationRequest;
import de.jeb.japp.model.generation.GenerationStatus;
import de.jeb.japp.model.generation.dto.GenerationRequestCreateRequest;
import de.jeb.japp.model.job.Job;
import de.jeb.japp.model.user.User;
import de.jeb.japp.model.user.UserRole;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Runs the cover-letter generation workflow: validates ownership of the
 * selected Job/CV, creates the GenerationRequest, and drives it through
 * PENDING → IN_PROGRESS → COMPLETED/FAILED. The actual content generation is
 * delegated to whichever {@link CoverLetterGenerationAdapter} the selected
 * provider instance's adapter type resolves to (through
 * {@link CoverLetterGenerationAdapterRegistry}, defaulting to the built-in
 * Placeholder instance) — this service has no knowledge of any adapter's
 * implementation, and adding a new admin-configured provider instance never
 * requires changing this class, the REST API, or Angular.
 * <p>
 * The CV context fed into the prompt comes from either the CV's raw extracted
 * text or its AI-structured {@code CVProfile}, selected per-request via
 * {@code useStructuredCv} — see {@link #resolveCvText}.
 */
@Service
public class GenerationRequestService {

    private final GenerationRequestDao generationRequestDao;
    private final CoverLetterDao coverLetterDao;
    private final JobDao jobDao;
    private final CVDao cvDao;
    private final CVProfileDao cvProfileDao;
    private final AiProviderConfigurationDao providerDao;
    private final ProviderSettingsResolver providerSettingsResolver;
    private final CoverLetterGenerationAdapterRegistry adapterRegistry;

    public GenerationRequestService(
            GenerationRequestDao generationRequestDao,
            CoverLetterDao coverLetterDao,
            JobDao jobDao,
            CVDao cvDao,
            CVProfileDao cvProfileDao,
            AiProviderConfigurationDao providerDao,
            ProviderSettingsResolver providerSettingsResolver,
            CoverLetterGenerationAdapterRegistry adapterRegistry
    ) {
        this.generationRequestDao = generationRequestDao;
        this.coverLetterDao = coverLetterDao;
        this.jobDao = jobDao;
        this.cvDao = cvDao;
        this.cvProfileDao = cvProfileDao;
        this.providerDao = providerDao;
        this.providerSettingsResolver = providerSettingsResolver;
        this.adapterRegistry = adapterRegistry;
    }

    public GenerationRequest create(GenerationRequestCreateRequest request, User owner) {
        validate(request);
        AiProviderConfiguration providerInstance = resolveProviderInstance(request.getProviderId());
        CoverLetterGenerationAdapter adapter = adapterRegistry.resolve(resolveAdapterType(providerInstance));
        ResolvedProviderConfig resolvedConfig = providerSettingsResolver.resolve(providerInstance.getId());

        // The generation request's owner is always the authenticated requester,
        // so the referenced job and CV must belong to that same requester.
        Job job = getOwnedJob(request.getJobId(), owner);
        CVDocument cv = getOwnedCv(request.getCvDocumentId(), owner);
        String cvText = resolveCvText(cv, request.isUseStructuredCv());

        GenerationRequest generationRequest = new GenerationRequest();
        generationRequest.setUser(owner);
        generationRequest.setJob(job);
        generationRequest.setCvDocument(cv);
        generationRequest.setJobDescriptionSnapshot(job.getDescription());
        generationRequest.setCvTextSnapshot(cvText);
        generationRequest.setProviderInstance(providerInstance);
        generationRequest.setProvider(providerInstance.getDisplayName());
        generationRequest.setModel(resolvedConfig.getModel());
        generationRequest.setStatus(GenerationStatus.PENDING);
        generationRequest.setCreatedAt(LocalDateTime.now());
        generationRequest = generationRequestDao.saveGenerationRequest(generationRequest);

        return process(generationRequest, adapter, resolvedConfig, job, cv, cvText, owner);
    }

    /**
     * Chooses the CV context text for the prompt: the AI-extracted {@link CVProfile} (formatted as
     * plain text) when {@code useStructuredCv} was requested and that profile reached COMPLETED,
     * otherwise the CV's raw extracted text — the same source used when the flag isn't set at all.
     * A requested-but-unavailable profile (never generated, still running, or failed) silently
     * falls back to the raw text rather than failing the request: both are genuine CV content, so
     * there's nothing to warn about here — the frontend is what decides whether to offer the
     * choice based on the profile's actual status.
     */
    private String resolveCvText(CVDocument cv, boolean useStructuredCv) {
        if (cv == null) {
            return null;
        }
        if (useStructuredCv) {
            Optional<CVProfile> profile = cvProfileDao.getByCvDocumentId(cv.getId());
            if (profile.isPresent() && profile.get().getStatus() == ProfileGenerationStatus.COMPLETED) {
                return formatStructuredCvText(profile.get());
            }
        }
        return cv.getExtractedText();
    }

    /** cvTextSnapshot is a varchar(8000) column; see {@link #truncateToColumnLimit}. */
    private static final int CV_TEXT_SNAPSHOT_MAX_LENGTH = 7997;

    private String formatStructuredCvText(CVProfile profile) {
        StringBuilder text = new StringBuilder();
        if (profile.getFullName() != null && !profile.getFullName().isBlank()) {
            text.append("Name: ").append(profile.getFullName()).append('\n');
        }
        if (profile.getSummary() != null && !profile.getSummary().isBlank()) {
            text.append("Zusammenfassung: ").append(profile.getSummary()).append('\n');
        }
        List<Experience> experiences = profile.getExperiences();
        if (experiences != null && !experiences.isEmpty()) {
            text.append("\nBerufserfahrung:\n");
            for (Experience experience : experiences) {
                text.append(formatExperience(experience)).append('\n');
            }
        }
        List<Skill> skills = profile.getSkills();
        if (skills != null && !skills.isEmpty()) {
            String names = skills.stream()
                    .map(Skill::getName)
                    .filter(name -> name != null && !name.isBlank())
                    .collect(Collectors.joining(", "));
            if (!names.isBlank()) {
                text.append("\nFähigkeiten: ").append(names).append('\n');
            }
        }
        List<Language> languages = profile.getLanguages();
        if (languages != null && !languages.isEmpty()) {
            text.append("\nSprachen:\n");
            for (Language language : languages) {
                text.append("- ").append(language.getName());
                if (language.getLevel() != null && !language.getLevel().isBlank()) {
                    text.append(" (").append(language.getLevel()).append(")");
                }
                text.append('\n');
            }
        }
        return truncateToColumnLimit(text.toString());
    }

    /**
     * Unlike the per-experience descriptions ({@link CvProfilePromptBuilder} deliberately asks the
     * model to preserve those in full), the formatted CV text as a whole still has to fit the
     * cvTextSnapshot column (varchar(8000)). Truncating the tail here trades a small amount of
     * detail from whichever section happens to run last for the request always completing, rather
     * than failing to save because a very detailed profile is a few characters over.
     */
    private String truncateToColumnLimit(String text) {
        return text.length() <= CV_TEXT_SNAPSHOT_MAX_LENGTH
                ? text
                : text.substring(0, CV_TEXT_SNAPSHOT_MAX_LENGTH) + "…";
    }

    private String formatExperience(Experience experience) {
        StringBuilder line = new StringBuilder("- ");
        if (experience.getTitle() != null && !experience.getTitle().isBlank()) {
            line.append(experience.getTitle());
        }
        if (experience.getCompany() != null && !experience.getCompany().isBlank()) {
            line.append(line.length() > 2 ? " bei " : "").append(experience.getCompany());
        }
        String range = formatDateRange(experience.getStartDate(), experience.getEndDate());
        if (!range.isBlank()) {
            line.append(" (").append(range).append(")");
        }
        if (experience.getDescription() != null && !experience.getDescription().isBlank()) {
            line.append(": ").append(experience.getDescription());
        }
        return line.toString();
    }

    private String formatDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return "";
        }
        String start = startDate != null ? startDate.toString() : "?";
        String end = endDate != null ? endDate.toString() : "heute";
        return start + " – " + end;
    }

    public GenerationRequest get(UUID id, User requester) {
        GenerationRequest generationRequest = find(id);
        assertAccess(generationRequest.getUser(), requester);
        return generationRequest;
    }

    public List<GenerationRequest> list(User requester) {
        return requester.getRole() == UserRole.ADMIN
                ? generationRequestDao.getAllGenerationRequests()
                : generationRequestDao.getAllGenerationRequestsByOwner(requester);
    }

    /**
     * ADMIN gets the global count, matching {@link #list}'s ADMIN-sees-everything convention.
     */
    public long count(User requester) {
        return requester.getRole() == UserRole.ADMIN
                ? generationRequestDao.countAll()
                : generationRequestDao.countByOwner(requester);
    }

    /**
     * Same ADMIN/owner scoping as {@link #count}, broken down by {@link GenerationStatus}.
     */
    public Map<GenerationStatus, Long> countByStatus(User requester) {
        return requester.getRole() == UserRole.ADMIN
                ? generationRequestDao.countAllGroupByStatus()
                : generationRequestDao.countByOwnerGroupByStatus(requester);
    }

    /**
     * The CoverLetter this request produced, if it has reached COMPLETED.
     */
    public Optional<CoverLetter> findCoverLetter(UUID generationRequestId) {
        return coverLetterDao.getCoverLetterByGenerationRequestId(generationRequestId);
    }

    private GenerationRequest process(
            GenerationRequest generationRequest,
            CoverLetterGenerationAdapter adapter,
            ResolvedProviderConfig resolvedConfig,
            Job job,
            CVDocument cv,
            String cvText,
            User owner
    ) {
        generationRequest.setStatus(GenerationStatus.IN_PROGRESS);
        generationRequest.setStartedAt(LocalDateTime.now());
        generationRequestDao.saveGenerationRequest(generationRequest);

        try {
            GenerationResult result = adapter.generate(resolvedConfig, buildInput(job, cv, cvText, owner));

            CoverLetter coverLetter = new CoverLetter();
            coverLetter.setOwner(owner);
            coverLetter.setGenerationRequest(generationRequest);
            coverLetter.setResultText(result.content());
            LocalDateTime now = LocalDateTime.now();
            coverLetter.setCreatedAt(now);
            coverLetter.setUpdatedAt(now);
            coverLetterDao.saveCoverLetter(coverLetter);

            generationRequest.setStatus(GenerationStatus.COMPLETED);
            generationRequest.setCompletedAt(LocalDateTime.now());
        } catch (CoverLetterGenerationException e) {
            generationRequest.setStatus(GenerationStatus.FAILED);
            generationRequest.setErrorMessage(e.getMessage());
            generationRequest.setCompletedAt(LocalDateTime.now());
        }

        return generationRequestDao.saveGenerationRequest(generationRequest);
    }

    /**
     * Translates the resolved Job/CV/User entities into the plain-value input the adapter operates
     * on. {@code cvText} is whatever {@link #resolveCvText} already decided (raw extracted text or
     * the formatted structured profile) — this method never re-derives it from {@code cv}.
     */
    private GenerationInput buildInput(Job job, CVDocument cv, String cvText, User owner) {
        String applicantName = (owner.getFullName() != null && !owner.getFullName().isBlank())
                ? owner.getFullName()
                : owner.getEmail();
        return new GenerationInput(
                job.getTitle(),
                job.getCompany().getName(),
                job.getDescription(),
                cv != null ? cv.getTitle() : null,
                cvText,
                applicantName
        );
    }

    /** No providerId means "use the built-in Placeholder instance". */
    private AiProviderConfiguration resolveProviderInstance(UUID providerId) {
        if (providerId == null) {
            return providerDao.getFirstByAdapterType(AdapterType.PLACEHOLDER.name())
                    .orElseThrow(() -> new AiProviderNotFoundException("The built-in Placeholder provider is not available."));
        }
        return providerDao.getById(providerId)
                .orElseThrow(() -> new AiProviderNotFoundException("Unknown AI provider instance: " + providerId));
    }

    private AdapterType resolveAdapterType(AiProviderConfiguration providerInstance) {
        try {
            return AdapterType.valueOf(providerInstance.getAdapterType());
        } catch (IllegalArgumentException e) {
            throw new CoverLetterGenerationException("This provider instance has an unknown adapter type.");
        }
    }

    private GenerationRequest find(UUID id) {
        return generationRequestDao.getGenerationRequestById(id)
                .orElseThrow(() -> new GenerationRequestNotFoundException("Generation request not found."));
    }

    private Job getOwnedJob(UUID jobId, User owner) {
        Job job = jobDao.getJobById(jobId).orElseThrow(() -> new JobNotFoundException("Job not found."));
        if (job.getOwner() == null || !job.getOwner().getId().equals(owner.getId())) {
            throw new JobAccessDeniedException("You do not have access to this job.");
        }
        return job;
    }

    private CVDocument getOwnedCv(UUID cvId, User owner) {
        CVDocument cv = cvDao.getCVById(cvId).orElseThrow(() -> new CVNotFoundException("CV not found."));
        if (cv.getOwner() == null || !cv.getOwner().getId().equals(owner.getId())) {
            throw new CVAccessDeniedException("You do not have access to this CV.");
        }
        return cv;
    }

    private void validate(GenerationRequestCreateRequest request) {
        if (request.getJobId() == null) {
            throw new GenerationRequestValidationException("A job is required.");
        }
        if (request.getCvDocumentId() == null) {
            throw new GenerationRequestValidationException("A CV is required.");
        }
    }

    private void assertAccess(User owner, User requester) {
        boolean isOwner = owner != null && owner.getId().equals(requester.getId());
        boolean isAdmin = requester.getRole() == UserRole.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new GenerationRequestAccessDeniedException("You do not have access to this generation request.");
        }
    }
}
