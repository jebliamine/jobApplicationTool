package de.jeb.japp.application.service;

import de.jeb.japp.commons.exceptions.application.ApplicationAccessDeniedException;
import de.jeb.japp.commons.exceptions.application.ApplicationNotFoundException;
import de.jeb.japp.commons.exceptions.application.ApplicationValidationException;
import de.jeb.japp.commons.exceptions.coverletter.CoverLetterAccessDeniedException;
import de.jeb.japp.commons.exceptions.coverletter.CoverLetterNotFoundException;
import de.jeb.japp.commons.exceptions.cv.CVAccessDeniedException;
import de.jeb.japp.commons.exceptions.cv.CVNotFoundException;
import de.jeb.japp.commons.exceptions.job.JobAccessDeniedException;
import de.jeb.japp.commons.exceptions.job.JobNotFoundException;
import de.jeb.japp.dao.application.ApplicationDao;
import de.jeb.japp.dao.coverletter.CoverLetterDao;
import de.jeb.japp.dao.cv.CVDao;
import de.jeb.japp.dao.job.JobDao;
import de.jeb.japp.model.application.Application;
import de.jeb.japp.model.application.ApplicationStatus;
import de.jeb.japp.model.application.dto.ApplicationRequest;
import de.jeb.japp.model.coverLetter.CoverLetter;
import de.jeb.japp.model.cv.CVDocument;
import de.jeb.japp.model.job.Job;
import de.jeb.japp.model.user.User;
import de.jeb.japp.model.user.UserRole;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Application is user-owned — a user's application to one of their own Jobs.
 * Same service-layer ownership-check pattern as CVDocument, Job, and Company.
 */
@Service
public class ApplicationService {

    private final ApplicationDao applicationDao;
    private final JobDao jobDao;
    private final CVDao cvDao;
    private final CoverLetterDao coverLetterDao;

    public ApplicationService(ApplicationDao applicationDao, JobDao jobDao, CVDao cvDao, CoverLetterDao coverLetterDao) {
        this.applicationDao = applicationDao;
        this.jobDao = jobDao;
        this.cvDao = cvDao;
        this.coverLetterDao = coverLetterDao;
    }

    public Application create(ApplicationRequest request, User owner) {
        validate(request);
        // The application's owner is always the authenticated requester, so
        // the referenced job (and CV/CoverLetter, if any) must belong to that
        // same requester. A CoverLetter is independently owned and optional —
        // it is never created, deleted, or required by an Application.
        Job job = getOwnedJob(request.getJobId(), owner);
        CVDocument cv = getOwnedCv(request.getCvDocumentId(), owner);
        CoverLetter coverLetter = getOwnedCoverLetter(request.getCoverLetterId(), owner);

        Application application = new Application();
        application.setUser(owner);
        applyRequest(application, request, job, cv, coverLetter);
        LocalDateTime now = LocalDateTime.now();
        application.setCreatedAt(now);
        application.setUpdatedAt(now);

        return applicationDao.saveApplication(application);
    }

    public Application get(UUID id, User requester) {
        Application application = find(id);
        assertAccess(application.getUser(), requester);
        return application;
    }

    public List<Application> list(User requester) {
        return requester.getRole() == UserRole.ADMIN
                ? applicationDao.getAllApplications()
                : applicationDao.getAllApplicationsByOwner(requester);
    }

    /** ADMIN gets the global count, matching {@link #list}'s ADMIN-sees-everything convention. */
    public long count(User requester) {
        return requester.getRole() == UserRole.ADMIN
                ? applicationDao.countAll()
                : applicationDao.countByOwner(requester);
    }

    public Application update(UUID id, ApplicationRequest request, User requester) {
        Application application = get(id, requester);
        validate(request);
        // Regardless of who is editing (including an admin editing another
        // user's application), the job/CV/CoverLetter may only reference
        // resources owned by the application's actual owner — never the
        // editor's own.
        Job job = getOwnedJob(request.getJobId(), application.getUser());
        CVDocument cv = getOwnedCv(request.getCvDocumentId(), application.getUser());
        CoverLetter coverLetter = getOwnedCoverLetter(request.getCoverLetterId(), application.getUser());
        applyRequest(application, request, job, cv, coverLetter);
        application.setUpdatedAt(LocalDateTime.now());
        return applicationDao.saveApplication(application);
    }

    public void delete(UUID id, User requester) {
        Application application = get(id, requester);
        applicationDao.deleteApplication(application.getId());
    }

    private Application find(UUID id) {
        return applicationDao.getApplicationById(id)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found."));
    }

    private Job getOwnedJob(UUID jobId, User owner) {
        Job job = jobDao.getJobById(jobId).orElseThrow(() -> new JobNotFoundException("Job not found."));
        if (job.getOwner() == null || !job.getOwner().getId().equals(owner.getId())) {
            throw new JobAccessDeniedException("You do not have access to this job.");
        }
        return job;
    }

    private CVDocument getOwnedCv(UUID cvId, User owner) {
        if (cvId == null) {
            return null;
        }
        CVDocument cv = cvDao.getCVById(cvId).orElseThrow(() -> new CVNotFoundException("CV not found."));
        if (cv.getOwner() == null || !cv.getOwner().getId().equals(owner.getId())) {
            throw new CVAccessDeniedException("You do not have access to this CV.");
        }
        return cv;
    }

    private CoverLetter getOwnedCoverLetter(UUID coverLetterId, User owner) {
        if (coverLetterId == null) {
            return null;
        }
        CoverLetter coverLetter = coverLetterDao.getCoverLetterById(coverLetterId)
                .orElseThrow(() -> new CoverLetterNotFoundException("Cover letter not found."));
        if (coverLetter.getOwner() == null || !coverLetter.getOwner().getId().equals(owner.getId())) {
            throw new CoverLetterAccessDeniedException("You do not have access to this cover letter.");
        }
        return coverLetter;
    }

    private void applyRequest(Application application, ApplicationRequest request, Job job, CVDocument cv, CoverLetter coverLetter) {
        application.setJob(job);
        application.setCvDocument(cv);
        application.setCoverLetter(coverLetter);
        application.setStatus(request.getStatus() != null ? request.getStatus() : ApplicationStatus.APPLIED);
        application.setAppliedAt(request.getAppliedAt() != null ? request.getAppliedAt() : LocalDate.now());
        application.setDeadline(request.getDeadline());
        application.setFollowUpDate(request.getFollowUpDate());
        application.setInterviewDate(request.getInterviewDate());
        application.setContactPerson(blankToNull(request.getContactPerson()));
        application.setNotes(blankToNull(request.getNotes()));
    }

    private void validate(ApplicationRequest request) {
        if (request.getJobId() == null) {
            throw new ApplicationValidationException("A job is required.");
        }
    }

    private void assertAccess(User owner, User requester) {
        boolean isOwner = owner != null && owner.getId().equals(requester.getId());
        boolean isAdmin = requester.getRole() == UserRole.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new ApplicationAccessDeniedException("You do not have access to this application.");
        }
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}
