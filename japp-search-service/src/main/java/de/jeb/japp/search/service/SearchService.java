package de.jeb.japp.search.service;

import de.jeb.japp.dao.application.ApplicationDao;
import de.jeb.japp.dao.company.CompanyDao;
import de.jeb.japp.dao.coverletter.CoverLetterDao;
import de.jeb.japp.dao.job.JobDao;
import de.jeb.japp.model.application.Application;
import de.jeb.japp.model.company.Company;
import de.jeb.japp.model.coverLetter.CoverLetter;
import de.jeb.japp.model.job.Job;
import de.jeb.japp.model.search.SearchResultType;
import de.jeb.japp.model.search.dto.SearchResultResponse;
import de.jeb.japp.model.user.User;
import de.jeb.japp.model.user.UserRole;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Searches across Job, Company, Application, and (active) CoverLetter — the same
 * ADMIN-sees-all/USER-sees-own scoping every list() in this app already uses, via the same DAOs
 * those feature-service modules use (matching in memory over each owner-scoped list rather than
 * a dedicated SQL LIKE query — simple and fast enough at this app's personal-tracker data
 * volumes; revisit with real DB-level search if that ever stops being true).
 */
@Service
public class SearchService {

    private static final int MAX_RESULTS_PER_TYPE = 5;

    private final JobDao jobDao;
    private final CompanyDao companyDao;
    private final ApplicationDao applicationDao;
    private final CoverLetterDao coverLetterDao;

    public SearchService(JobDao jobDao, CompanyDao companyDao, ApplicationDao applicationDao, CoverLetterDao coverLetterDao) {
        this.jobDao = jobDao;
        this.companyDao = companyDao;
        this.applicationDao = applicationDao;
        this.coverLetterDao = coverLetterDao;
    }

    public List<SearchResultResponse> search(String query, User requester) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        String term = query.trim().toLowerCase();

        List<SearchResultResponse> results = new ArrayList<>();
        results.addAll(searchJobs(term, requester));
        results.addAll(searchCompanies(term, requester));
        results.addAll(searchApplications(term, requester));
        results.addAll(searchCoverLetters(term, requester));
        return results;
    }

    private List<SearchResultResponse> searchJobs(String term, User requester) {
        List<Job> jobs = requester.getRole() == UserRole.ADMIN ? jobDao.getAllJobs() : jobDao.getAllJobsByOwner(requester);
        return jobs.stream()
                .filter(job -> matches(term, job.getTitle(), job.getCompany().getName(), job.getLocation()))
                .limit(MAX_RESULTS_PER_TYPE)
                .map(job -> new SearchResultResponse(SearchResultType.JOB, job.getId(), job.getTitle(), job.getCompany().getName()))
                .toList();
    }

    private List<SearchResultResponse> searchCompanies(String term, User requester) {
        List<Company> companies = requester.getRole() == UserRole.ADMIN
                ? companyDao.getAllCompanies()
                : companyDao.getAllCompaniesByOwner(requester);
        return companies.stream()
                .filter(company -> matches(term, company.getName(), company.getLocation()))
                .limit(MAX_RESULTS_PER_TYPE)
                .map(company -> new SearchResultResponse(SearchResultType.COMPANY, company.getId(), company.getName(), company.getLocation()))
                .toList();
    }

    private List<SearchResultResponse> searchApplications(String term, User requester) {
        List<Application> applications = requester.getRole() == UserRole.ADMIN
                ? applicationDao.getAllApplications()
                : applicationDao.getAllApplicationsByOwner(requester);
        return applications.stream()
                .filter(application -> matches(term,
                        application.getJob().getTitle(),
                        application.getJob().getCompany().getName(),
                        application.getContactPerson(),
                        application.getNotes()))
                .limit(MAX_RESULTS_PER_TYPE)
                .map(application -> new SearchResultResponse(
                        SearchResultType.APPLICATION,
                        application.getId(),
                        application.getJob().getTitle(),
                        application.getJob().getCompany().getName()))
                .toList();
    }

    /** Archived cover letters are excluded — same default as the cover-letter list page. */
    private List<SearchResultResponse> searchCoverLetters(String term, User requester) {
        List<CoverLetter> coverLetters = requester.getRole() == UserRole.ADMIN
                ? coverLetterDao.getAllCoverLetters(false)
                : coverLetterDao.getAllCoverLettersByOwner(requester, false);
        return coverLetters.stream()
                .filter(coverLetter -> matches(term, jobTitleOf(coverLetter), companyNameOf(coverLetter)))
                .limit(MAX_RESULTS_PER_TYPE)
                .map(coverLetter -> new SearchResultResponse(
                        SearchResultType.COVER_LETTER,
                        coverLetter.getId(),
                        jobTitleOf(coverLetter),
                        companyNameOf(coverLetter)))
                .toList();
    }

    private String jobTitleOf(CoverLetter coverLetter) {
        return coverLetter.getGenerationRequest().getJob().getTitle();
    }

    private String companyNameOf(CoverLetter coverLetter) {
        return coverLetter.getGenerationRequest().getJob().getCompany().getName();
    }

    private boolean matches(String term, String... fields) {
        for (String field : fields) {
            if (field != null && field.toLowerCase().contains(term)) {
                return true;
            }
        }
        return false;
    }
}
