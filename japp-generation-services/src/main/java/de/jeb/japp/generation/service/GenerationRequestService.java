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
import de.jeb.japp.model.coverLetter.CoverLetter;
import de.jeb.japp.model.cv.CVDocument;
import de.jeb.japp.model.generation.GenerationRequest;
import de.jeb.japp.model.generation.GenerationStatus;
import de.jeb.japp.model.generation.dto.GenerationRequestCreateRequest;
import de.jeb.japp.model.job.Job;
import de.jeb.japp.model.user.User;
import de.jeb.japp.model.user.UserRole;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Runs the cover-letter generation workflow: validates ownership of the
 * selected Job/CV, creates the GenerationRequest, and drives it through
 * PENDING → IN_PROGRESS → COMPLETED/FAILED. The placeholder generator below
 * completes synchronously (no external AI call) but the domain model and API
 * are shaped to support a real async provider later without changes to
 * callers — see {@link #process}.
 */
@Service
public class GenerationRequestService {

    private static final String PROVIDER = "placeholder";
    private static final String MODEL = "deterministic-v1";
    private static final int DESCRIPTION_EXCERPT_LENGTH = 400;

    private final GenerationRequestDao generationRequestDao;
    private final CoverLetterDao coverLetterDao;
    private final JobDao jobDao;
    private final CVDao cvDao;

    public GenerationRequestService(
            GenerationRequestDao generationRequestDao,
            CoverLetterDao coverLetterDao,
            JobDao jobDao,
            CVDao cvDao
    ) {
        this.generationRequestDao = generationRequestDao;
        this.coverLetterDao = coverLetterDao;
        this.jobDao = jobDao;
        this.cvDao = cvDao;
    }

    public GenerationRequest create(GenerationRequestCreateRequest request, User owner) {
        validate(request);
        // The generation request's owner is always the authenticated requester,
        // so the referenced job and CV must belong to that same requester.
        Job job = getOwnedJob(request.getJobId(), owner);
        CVDocument cv = getOwnedCv(request.getCvDocumentId(), owner);

        GenerationRequest generationRequest = new GenerationRequest();
        generationRequest.setUser(owner);
        generationRequest.setJob(job);
        generationRequest.setCvDocument(cv);
        generationRequest.setJobDescriptionSnapshot(job.getDescription());
        generationRequest.setProvider(PROVIDER);
        generationRequest.setModel(MODEL);
        generationRequest.setStatus(GenerationStatus.PENDING);
        generationRequest.setCreatedAt(LocalDateTime.now());
        generationRequest = generationRequestDao.saveGenerationRequest(generationRequest);

        return process(generationRequest, job, cv, owner);
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

    /** The CoverLetter this request produced, if it has reached COMPLETED. */
    public Optional<CoverLetter> findCoverLetter(UUID generationRequestId) {
        return coverLetterDao.getCoverLetterByGenerationRequestId(generationRequestId);
    }

    private GenerationRequest process(GenerationRequest generationRequest, Job job, CVDocument cv, User owner) {
        generationRequest.setStatus(GenerationStatus.IN_PROGRESS);
        generationRequest.setStartedAt(LocalDateTime.now());
        generationRequestDao.saveGenerationRequest(generationRequest);

        try {
            String content = generatePlaceholderContent(job, cv, owner);

            CoverLetter coverLetter = new CoverLetter();
            coverLetter.setOwner(owner);
            coverLetter.setGenerationRequest(generationRequest);
            coverLetter.setResultText(content);
            LocalDateTime now = LocalDateTime.now();
            coverLetter.setCreatedAt(now);
            coverLetter.setUpdatedAt(now);
            coverLetterDao.saveCoverLetter(coverLetter);

            generationRequest.setStatus(GenerationStatus.COMPLETED);
            generationRequest.setCompletedAt(LocalDateTime.now());
        } catch (GenerationRequestValidationException e) {
            generationRequest.setStatus(GenerationStatus.FAILED);
            generationRequest.setErrorMessage(e.getMessage());
            generationRequest.setCompletedAt(LocalDateTime.now());
        }

        return generationRequestDao.saveGenerationRequest(generationRequest);
    }

    /**
     * Deterministic, no external AI call: template the Job/CV metadata into
     * a cover letter body. Fails when the Job has no description, since a
     * real generator would have nothing to generate from either.
     */
    private String generatePlaceholderContent(Job job, CVDocument cv, User owner) {
        if (job.getDescription() == null || job.getDescription().isBlank()) {
            throw new GenerationRequestValidationException("The selected job has no description to generate from.");
        }

        String applicantName = (owner.getFullName() != null && !owner.getFullName().isBlank())
                ? owner.getFullName()
                : owner.getEmail();
        String cvReference = cv != null ? " and my CV \"" + cv.getTitle() + "\"" : "";
        String description = job.getDescription().trim();
        String descriptionExcerpt = description.length() > DESCRIPTION_EXCERPT_LENGTH
                ? description.substring(0, DESCRIPTION_EXCERPT_LENGTH).trim() + "…"
                : description;

        return "Dear Hiring Team at " + job.getCompany().getName() + ",\n\n"
                + "I am writing to express my interest in the " + job.getTitle() + " position. "
                + "Based on the role description" + cvReference
                + ", I believe my background aligns well with what you are looking for:\n\n"
                + "\"" + descriptionExcerpt + "\"\n\n"
                + "I would welcome the opportunity to discuss how I can contribute to your team.\n\n"
                + "Sincerely,\n" + applicantName
                + "\n\n[This is a placeholder cover letter generated without an AI provider.]";
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
