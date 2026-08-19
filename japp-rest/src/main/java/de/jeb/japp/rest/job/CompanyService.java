package de.jeb.japp.rest.job;

import de.jeb.japp.dao.company.CompanyDao;
import de.jeb.japp.dao.job.JobDao;
import de.jeb.japp.model.company.Company;
import de.jeb.japp.model.company.dto.CompanyRequest;
import de.jeb.japp.model.user.User;
import de.jeb.japp.model.user.UserRole;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Company is user-owned (per-user, not a shared/global directory — see the
 * approved domain decision). Authorization follows the CVDocument/CV feature
 * pattern: service-layer ownership checks rather than {@code @PreAuthorize},
 * since roles have no hierarchy configured and an annotation-based check
 * previously blocked ADMIN entirely.
 */
@Service
public class CompanyService {

    private final CompanyDao companyDao;
    private final JobDao jobDao;

    public CompanyService(CompanyDao companyDao, JobDao jobDao) {
        this.companyDao = companyDao;
        this.jobDao = jobDao;
    }

    public Company create(CompanyRequest request, User owner) {
        validate(request);

        Company company = new Company();
        applyRequest(company, request);
        company.setOwner(owner);
        LocalDateTime now = LocalDateTime.now();
        company.setCreatedAt(now);
        company.setUpdatedAt(now);

        return companyDao.saveCompany(company);
    }

    public Company get(UUID id, User requester) {
        Company company = find(id);
        assertAccess(company.getOwner(), requester);
        return company;
    }

    public List<Company> list(User requester) {
        return requester.getRole() == UserRole.ADMIN
                ? companyDao.getAllCompanies()
                : companyDao.getAllCompaniesByOwner(requester);
    }

    public Company update(UUID id, CompanyRequest request, User requester) {
        Company company = get(id, requester);
        validate(request);
        applyRequest(company, request);
        company.setUpdatedAt(LocalDateTime.now());
        return companyDao.saveCompany(company);
    }

    public void delete(UUID id, User requester) {
        Company company = get(id, requester);
        if (jobDao.existsByCompanyId(company.getId())) {
            throw new JobsValidationException("Cannot delete a company that still has jobs. Delete or reassign those jobs first.");
        }
        companyDao.deleteCompany(company.getId());
    }

    /**
     * Strict ownership check (no admin bypass) — used by JobService to verify
     * a job may only reference a company owned by the same user as the job,
     * regardless of who (including an admin) is performing the request.
     */
    Company getOwnedByExactly(UUID id, User owner) {
        Company company = find(id);
        if (company.getOwner() == null || !company.getOwner().getId().equals(owner.getId())) {
            throw new JobsAccessDeniedException("You do not have access to this company.");
        }
        return company;
    }

    private Company find(UUID id) {
        return companyDao.getCompanyById(id).orElseThrow(() -> new JobsNotFoundException("Company not found."));
    }

    private void applyRequest(Company company, CompanyRequest request) {
        company.setName(request.getName().trim());
        company.setWebsite(blankToNull(request.getWebsite()));
        company.setLocation(blankToNull(request.getLocation()));
        company.setNotes(blankToNull(request.getNotes()));
    }

    private void validate(CompanyRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new JobsValidationException("A company name is required.");
        }
    }

    private void assertAccess(User owner, User requester) {
        boolean isOwner = owner != null && owner.getId().equals(requester.getId());
        boolean isAdmin = requester.getRole() == UserRole.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new JobsAccessDeniedException("You do not have access to this company.");
        }
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}
