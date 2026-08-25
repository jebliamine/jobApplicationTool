package de.jeb.japp.search.service;

import de.jeb.japp.dao.application.ApplicationDao;
import de.jeb.japp.dao.company.CompanyDao;
import de.jeb.japp.dao.coverletter.CoverLetterDao;
import de.jeb.japp.dao.job.JobDao;
import de.jeb.japp.model.application.Application;
import de.jeb.japp.model.application.ApplicationStatus;
import de.jeb.japp.model.company.Company;
import de.jeb.japp.model.coverLetter.CoverLetter;
import de.jeb.japp.model.generation.GenerationRequest;
import de.jeb.japp.model.job.Job;
import de.jeb.japp.model.search.SearchResultType;
import de.jeb.japp.model.search.dto.SearchResultResponse;
import de.jeb.japp.model.user.User;
import de.jeb.japp.model.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private JobDao jobDao;
    @Mock
    private CompanyDao companyDao;
    @Mock
    private ApplicationDao applicationDao;
    @Mock
    private CoverLetterDao coverLetterDao;

    private SearchService searchService;

    private User owner;
    private User admin;
    private Company company;

    @BeforeEach
    void setUp() {
        searchService = new SearchService(jobDao, companyDao, applicationDao, coverLetterDao);

        owner = new User();
        owner.setId(UUID.randomUUID());
        owner.setRole(UserRole.USER);

        admin = new User();
        admin.setId(UUID.randomUUID());
        admin.setRole(UserRole.ADMIN);

        company = new Company();
        ReflectionTestUtils.setField(company, "id", UUID.randomUUID());
        company.setName("Acme Corp");
        company.setLocation("Berlin");

        lenient().when(jobDao.getAllJobsByOwner(owner)).thenReturn(List.of());
        lenient().when(companyDao.getAllCompaniesByOwner(owner)).thenReturn(List.of());
        lenient().when(applicationDao.getAllApplicationsByOwner(owner)).thenReturn(List.of());
        lenient().when(coverLetterDao.getAllCoverLettersByOwner(owner, false)).thenReturn(List.of());
    }

    private Job job(String title, Company company, String location) {
        Job job = new Job();
        ReflectionTestUtils.setField(job, "id", UUID.randomUUID());
        job.setTitle(title);
        job.setCompany(company);
        job.setLocation(location);
        job.setDescription("Some description.");
        return job;
    }

    @Test
    void returnsEmptyForABlankOrNullQueryWithoutQueryingAnything() {
        assertThat(searchService.search("", owner)).isEmpty();
        assertThat(searchService.search("   ", owner)).isEmpty();
        assertThat(searchService.search(null, owner)).isEmpty();

        verifyNoInteractions(jobDao, companyDao, applicationDao, coverLetterDao);
    }

    @Test
    void matchesAJobByTitleCaseInsensitively() {
        Job job = job("Backend Engineer", company, "Berlin");
        when(jobDao.getAllJobsByOwner(owner)).thenReturn(List.of(job));

        List<SearchResultResponse> results = searchService.search("backend", owner);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getType()).isEqualTo(SearchResultType.JOB);
        assertThat(results.get(0).getId()).isEqualTo(job.getId());
        assertThat(results.get(0).getTitle()).isEqualTo("Backend Engineer");
        assertThat(results.get(0).getSubtitle()).isEqualTo("Acme Corp");
    }

    @Test
    void matchesAJobByCompanyNameOrLocation() {
        Job job = job("Data Analyst", company, "Berlin");
        when(jobDao.getAllJobsByOwner(owner)).thenReturn(List.of(job));

        assertThat(searchService.search("acme", owner)).hasSize(1);
        assertThat(searchService.search("berlin", owner)).hasSize(1);
        assertThat(searchService.search("munich", owner)).isEmpty();
    }

    @Test
    void matchesACompanyByNameOrLocation() {
        when(companyDao.getAllCompaniesByOwner(owner)).thenReturn(List.of(company));

        List<SearchResultResponse> results = searchService.search("acme", owner);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getType()).isEqualTo(SearchResultType.COMPANY);
        assertThat(results.get(0).getSubtitle()).isEqualTo("Berlin");
    }

    private Application application(Job job, String contactPerson, String notes) {
        Application application = new Application();
        ReflectionTestUtils.setField(application, "id", UUID.randomUUID());
        application.setUser(owner);
        application.setJob(job);
        application.setStatus(ApplicationStatus.APPLIED);
        application.setContactPerson(contactPerson);
        application.setNotes(notes);
        return application;
    }

    @Test
    void matchesAnApplicationByJobCompanyContactOrNotes() {
        Job job = job("Backend Engineer", company, "Berlin");
        Application application = application(job, "Jane Recruiter", "Referred by a friend.");
        when(applicationDao.getAllApplicationsByOwner(owner)).thenReturn(List.of(application));

        assertThat(searchService.search("recruiter", owner)).hasSize(1);
        assertThat(searchService.search("referred", owner)).hasSize(1);
        assertThat(searchService.search("nonexistent", owner)).isEmpty();
    }

    private CoverLetter coverLetter(Job job) {
        GenerationRequest generationRequest = new GenerationRequest();
        generationRequest.setJob(job);
        CoverLetter coverLetter = new CoverLetter();
        ReflectionTestUtils.setField(coverLetter, "id", UUID.randomUUID());
        coverLetter.setGenerationRequest(generationRequest);
        return coverLetter;
    }

    @Test
    void matchesACoverLetterByItsJobsTitleOrCompany() {
        Job job = job("Backend Engineer", company, "Berlin");
        CoverLetter coverLetter = coverLetter(job);
        when(coverLetterDao.getAllCoverLettersByOwner(owner, false)).thenReturn(List.of(coverLetter));

        List<SearchResultResponse> results = searchService.search("backend", owner);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getType()).isEqualTo(SearchResultType.COVER_LETTER);
        assertThat(results.get(0).getTitle()).isEqualTo("Backend Engineer");
        assertThat(results.get(0).getSubtitle()).isEqualTo("Acme Corp");
    }

    @Test
    void onlySearchesActiveCoverLetters() {
        searchService.search("backend", owner);

        verify(coverLetterDao).getAllCoverLettersByOwner(owner, false);
        verify(coverLetterDao, never()).getAllCoverLettersByOwner(any(), org.mockito.ArgumentMatchers.eq(true));
    }

    @Test
    void anAdminSearchesEveryUsersDataNotJustTheirOwn() {
        searchService.search("backend", admin);

        verify(jobDao).getAllJobs();
        verify(jobDao, never()).getAllJobsByOwner(any());
        verify(companyDao).getAllCompanies();
        verify(applicationDao).getAllApplications();
        verify(coverLetterDao).getAllCoverLetters(false);
    }

    @Test
    void capsResultsAtFivePerType() {
        List<Job> jobs = IntStream.range(0, 10)
                .mapToObj(i -> job("Backend Engineer " + i, company, "Berlin"))
                .toList();
        when(jobDao.getAllJobsByOwner(owner)).thenReturn(jobs);

        List<SearchResultResponse> results = searchService.search("backend", owner);

        assertThat(results).hasSize(5);
    }
}
