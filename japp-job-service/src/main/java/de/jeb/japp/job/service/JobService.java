package de.jeb.japp.job.service;

import de.jeb.japp.commons.exceptions.job.JobAccessDeniedException;
import de.jeb.japp.commons.exceptions.job.JobNotFoundException;
import de.jeb.japp.commons.exceptions.job.JobValidationException;
import de.jeb.japp.dao.job.JobDao;
import de.jeb.japp.model.company.Company;
import de.jeb.japp.model.job.Job;
import de.jeb.japp.model.job.dto.JobRequest;
import de.jeb.japp.model.user.User;
import de.jeb.japp.model.user.UserRole;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Job is user-owned — a job posting the user is personally tracking, not a
 * shared listing. Same service-layer ownership-check pattern as CVDocument
 * and Company (see CompanyService).
 */
@Service
public class JobService {

    private final JobDao jobDao;
    private final CompanyService companyService;

    public JobService(JobDao jobDao, CompanyService companyService) {
        this.jobDao = jobDao;
        this.companyService = companyService;
    }

    public Job create(JobRequest request, User owner) {
        validate(request);
        // The job's owner is always the authenticated requester, so the
        // referenced company must belong to that same requester.
        Company company = companyService.getOwnedByExactly(request.getCompanyId(), owner);

        Job job = new Job();
        applyRequest(job, request, company);
        job.setOwner(owner);
        LocalDateTime now = LocalDateTime.now();
        job.setCreatedAt(now);
        job.setUpdatedAt(now);

        return jobDao.saveJob(job);
    }

    public Job get(UUID id, User requester) {
        Job job = find(id);
        assertAccess(job.getOwner(), requester);
        return job;
    }

    public List<Job> list(User requester) {
        return requester.getRole() == UserRole.ADMIN
                ? jobDao.getAllJobs()
                : jobDao.getAllJobsByOwner(requester);
    }

    public Job update(UUID id, JobRequest request, User requester) {
        Job job = get(id, requester);
        validate(request);
        // Regardless of who is editing (including an admin editing another
        // user's job), the job may only reference a company owned by the
        // job's actual owner — never the editor's own companies.
        Company company = companyService.getOwnedByExactly(request.getCompanyId(), job.getOwner());
        applyRequest(job, request, company);
        job.setUpdatedAt(LocalDateTime.now());
        return jobDao.saveJob(job);
    }

    public void delete(UUID id, User requester) {
        Job job = get(id, requester);
        jobDao.deleteJob(job.getId());
    }

    private Job find(UUID id) {
        return jobDao.getJobById(id).orElseThrow(() -> new JobNotFoundException("Job not found."));
    }

    private void applyRequest(Job job, JobRequest request, Company company) {
        job.setCompany(company);
        job.setTitle(request.getTitle().trim());
        job.setDescription(request.getDescription().trim());
        job.setLocation(blankToNull(request.getLocation()));
        job.setEmploymentType(request.getEmploymentType());
        job.setWorkMode(request.getWorkMode());
        job.setUrl(blankToNull(request.getUrl()));
        job.setSource(blankToNull(request.getSource()));
    }

    private void validate(JobRequest request) {
        if (request.getCompanyId() == null) {
            throw new JobValidationException("A company is required.");
        }
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new JobValidationException("A job title is required.");
        }
        if (request.getDescription() == null || request.getDescription().isBlank()) {
            throw new JobValidationException("A job description is required.");
        }
    }

    private void assertAccess(User owner, User requester) {
        boolean isOwner = owner != null && owner.getId().equals(requester.getId());
        boolean isAdmin = requester.getRole() == UserRole.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new JobAccessDeniedException("You do not have access to this job.");
        }
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}
