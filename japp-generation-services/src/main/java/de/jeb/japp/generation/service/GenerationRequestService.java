package de.jeb.japp.generation.service;

import de.jeb.japp.commons.exceptions.cv.CVAccessDeniedException;
import de.jeb.japp.commons.exceptions.cv.CVNotFoundException;
import de.jeb.japp.commons.exceptions.generation.GenerationRequestAccessDeniedException;
import de.jeb.japp.commons.exceptions.generation.GenerationRequestNotFoundException;
import de.jeb.japp.commons.exceptions.generation.GenerationRequestValidationException;
import de.jeb.japp.commons.exceptions.job.JobAccessDeniedException;
import de.jeb.japp.commons.exceptions.job.JobNotFoundException;
import de.jeb.japp.dao.coverletter.CoverLetterDao;
import de.jeb.japp.dao.cv.CVDao;
import de.jeb.japp.dao.generation.GenerationRequestDao;
import de.jeb.japp.dao.job.JobDao;
import de.jeb.japp.generation.service.provider.CoverLetterGenerationException;
import de.jeb.japp.generation.service.provider.CoverLetterGenerationProvider;
import de.jeb.japp.generation.service.provider.CoverLetterGenerationProviderRegistry;
import de.jeb.japp.generation.service.provider.GenerationInput;
import de.jeb.japp.generation.service.provider.GenerationResult;
import de.jeb.japp.model.coverLetter.CoverLetter;
import de.jeb.japp.model.cv.CVDocument;
import de.jeb.japp.model.generation.GenerationProvider;
import de.jeb.japp.model.generation.GenerationRequest;
import de.jeb.japp.model.generation.GenerationStatus;
import de.jeb.japp.model.generation.dto.GenerationRequestCreateRequest;
import de.jeb.japp.model.job.Job;
import de.jeb.japp.model.user.User;
import de.jeb.japp.model.user.UserRole;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Runs the cover-letter generation workflow: validates ownership of the
 * selected Job/CV, creates the GenerationRequest, and drives it through
 * PENDING → IN_PROGRESS → COMPLETED/FAILED. The actual content generation is
 * delegated to whichever {@link CoverLetterGenerationProvider} the request
 * selects (resolved through {@link CoverLetterGenerationProviderRegistry},
 * defaulting to PLACEHOLDER) — this service has no knowledge of any
 * provider's implementation, so a new provider can be added later without
 * changing this class, the REST API, or Angular.
 */
@Service
public class GenerationRequestService {

    private final GenerationRequestDao generationRequestDao;
    private final CoverLetterDao coverLetterDao;
    private final JobDao jobDao;
    private final CVDao cvDao;
    private final CoverLetterGenerationProviderRegistry providerRegistry;

    public GenerationRequestService(
            GenerationRequestDao generationRequestDao,
            CoverLetterDao coverLetterDao,
            JobDao jobDao,
            CVDao cvDao,
            CoverLetterGenerationProviderRegistry providerRegistry
    ) {
        this.generationRequestDao = generationRequestDao;
        this.coverLetterDao = coverLetterDao;
        this.jobDao = jobDao;
        this.cvDao = cvDao;
        this.providerRegistry = providerRegistry;
    }

    public GenerationRequest create(GenerationRequestCreateRequest request, User owner) {
        validate(request);
        GenerationProvider requestedProvider = request.getProvider() != null
                ? request.getProvider()
                : GenerationProvider.PLACEHOLDER;
        CoverLetterGenerationProvider provider = providerRegistry.resolve(requestedProvider);

        // The generation request's owner is always the authenticated requester,
        // so the referenced job and CV must belong to that same requester.
        Job job = getOwnedJob(request.getJobId(), owner);
        CVDocument cv = getOwnedCv(request.getCvDocumentId(), owner);

        GenerationRequest generationRequest = new GenerationRequest();
        generationRequest.setUser(owner);
        generationRequest.setJob(job);
        generationRequest.setCvDocument(cv);
        generationRequest.setJobDescriptionSnapshot(job.getDescription());
        generationRequest.setProvider(provider.id().name());
        generationRequest.setModel(provider.model());
        generationRequest.setStatus(GenerationStatus.PENDING);
        generationRequest.setCreatedAt(LocalDateTime.now());
        generationRequest = generationRequestDao.saveGenerationRequest(generationRequest);

        return process(generationRequest, provider, job, cv, owner);
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

    /** ADMIN gets the global count, matching {@link #list}'s ADMIN-sees-everything convention. */
    public long count(User requester) {
        return requester.getRole() == UserRole.ADMIN
                ? generationRequestDao.countAll()
                : generationRequestDao.countByOwner(requester);
    }

    /** Same ADMIN/owner scoping as {@link #count}, broken down by {@link GenerationStatus}. */
    public Map<GenerationStatus, Long> countByStatus(User requester) {
        return requester.getRole() == UserRole.ADMIN
                ? generationRequestDao.countAllGroupByStatus()
                : generationRequestDao.countByOwnerGroupByStatus(requester);
    }

    /** The CoverLetter this request produced, if it has reached COMPLETED. */
    public Optional<CoverLetter> findCoverLetter(UUID generationRequestId) {
        return coverLetterDao.getCoverLetterByGenerationRequestId(generationRequestId);
    }

    private GenerationRequest process(
            GenerationRequest generationRequest,
            CoverLetterGenerationProvider provider,
            Job job,
            CVDocument cv,
            User owner
    ) {
        generationRequest.setStatus(GenerationStatus.IN_PROGRESS);
        generationRequest.setStartedAt(LocalDateTime.now());
        generationRequestDao.saveGenerationRequest(generationRequest);

        try {
            GenerationResult result = provider.generate(buildInput(job, cv, owner));

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

    /** Translates the resolved Job/CV/User entities into the plain-value input the provider operates on. */
    private GenerationInput buildInput(Job job, CVDocument cv, User owner) {
        String applicantName = (owner.getFullName() != null && !owner.getFullName().isBlank())
                ? owner.getFullName()
                : owner.getEmail();
        return new GenerationInput(
                job.getTitle(),
                job.getCompany().getName(),
                job.getDescription(),
                cv != null ? cv.getTitle() : null,
                applicantName
        );
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
