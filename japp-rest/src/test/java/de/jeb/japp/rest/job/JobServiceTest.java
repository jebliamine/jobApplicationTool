package de.jeb.japp.rest.job;

import de.jeb.japp.dao.job.JobDao;
import de.jeb.japp.model.company.Company;
import de.jeb.japp.model.job.Job;
import de.jeb.japp.model.job.dto.JobRequest;
import de.jeb.japp.model.user.User;
import de.jeb.japp.model.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobDao jobDao;
    @Mock
    private CompanyService companyService;

    private JobService jobService;

    private User owner;
    private User otherUser;
    private User admin;
    private Company company;

    @BeforeEach
    void setUp() {
        jobService = new JobService(jobDao, companyService);

        owner = new User();
        owner.setId(UUID.randomUUID());
        owner.setRole(UserRole.USER);

        otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        otherUser.setRole(UserRole.USER);

        admin = new User();
        admin.setId(UUID.randomUUID());
        admin.setRole(UserRole.ADMIN);

        company = new Company();
        company.setOwner(owner);
        company.setName("Acme");
    }

    private JobRequest validRequest() {
        JobRequest request = new JobRequest();
        request.setCompanyId(UUID.randomUUID());
        request.setTitle("Backend Engineer");
        request.setDescription("Build things.");
        return request;
    }

    private Job jobOwnedBy(User user) {
        Job job = new Job();
        job.setOwner(user);
        job.setCompany(company);
        job.setTitle("Backend Engineer");
        job.setDescription("Build things.");
        return job;
    }

    @Test
    void createSetsOwnerFromAuthenticatedUserAndResolvesCompany() {
        JobRequest request = validRequest();
        when(companyService.getOwnedByExactly(request.getCompanyId(), owner)).thenReturn(company);
        when(jobDao.saveJob(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Job created = jobService.create(request, owner);

        assertThat(created.getOwner()).isEqualTo(owner);
        assertThat(created.getCompany()).isEqualTo(company);
        assertThat(created.getTitle()).isEqualTo("Backend Engineer");
        assertThat(created.getCreatedAt()).isNotNull();
    }

    @Test
    void createRejectsMissingTitle() {
        JobRequest request = validRequest();
        request.setTitle(" ");

        assertThatThrownBy(() -> jobService.create(request, owner))
                .isInstanceOf(JobsValidationException.class);

        verifyNoInteractions(jobDao);
    }

    @Test
    void createRejectsMissingDescription() {
        JobRequest request = validRequest();
        request.setDescription(null);

        assertThatThrownBy(() -> jobService.create(request, owner))
                .isInstanceOf(JobsValidationException.class);
    }

    @Test
    void createRejectsMissingCompany() {
        JobRequest request = validRequest();
        request.setCompanyId(null);

        assertThatThrownBy(() -> jobService.create(request, owner))
                .isInstanceOf(JobsValidationException.class);

        verifyNoInteractions(jobDao);
    }

    @Test
    void createRejectsCompanyOwnedBySomeoneElse() {
        JobRequest request = validRequest();
        when(companyService.getOwnedByExactly(request.getCompanyId(), owner))
                .thenThrow(new JobsAccessDeniedException("You do not have access to this company."));

        assertThatThrownBy(() -> jobService.create(request, owner))
                .isInstanceOf(JobsAccessDeniedException.class);

        verify(jobDao, never()).saveJob(any());
    }

    @Test
    void ownerCanGetTheirOwnJob() {
        UUID id = UUID.randomUUID();
        Job job = jobOwnedBy(owner);
        when(jobDao.getJobById(id)).thenReturn(Optional.of(job));

        assertThat(jobService.get(id, owner)).isEqualTo(job);
    }

    @Test
    void adminCanGetAnyJob() {
        UUID id = UUID.randomUUID();
        Job job = jobOwnedBy(owner);
        when(jobDao.getJobById(id)).thenReturn(Optional.of(job));

        assertThat(jobService.get(id, admin)).isEqualTo(job);
    }

    @Test
    void otherUserCannotGetSomeoneElsesJob() {
        UUID id = UUID.randomUUID();
        Job job = jobOwnedBy(owner);
        when(jobDao.getJobById(id)).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> jobService.get(id, otherUser))
                .isInstanceOf(JobsAccessDeniedException.class);
    }

    @Test
    void getThrowsNotFoundForMissingJob() {
        UUID id = UUID.randomUUID();
        when(jobDao.getJobById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobService.get(id, owner))
                .isInstanceOf(JobsNotFoundException.class);
    }

    @Test
    void otherUserCannotDeleteSomeoneElsesJob() {
        UUID id = UUID.randomUUID();
        Job job = jobOwnedBy(owner);
        when(jobDao.getJobById(id)).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> jobService.delete(id, otherUser))
                .isInstanceOf(JobsAccessDeniedException.class);

        verify(jobDao, never()).deleteJob(any());
    }

    @Test
    void listReturnsAllJobsForAdmin() {
        jobService.list(admin);
        verify(jobDao).getAllJobs();
        verify(jobDao, never()).getAllJobsByOwner(any());
    }

    @Test
    void listReturnsOnlyOwnJobsForRegularUser() {
        jobService.list(owner);
        verify(jobDao).getAllJobsByOwner(owner);
        verify(jobDao, never()).getAllJobs();
    }

    @Test
    void updateByAdminResolvesCompanyAgainstJobOwnerNotAdmin() {
        UUID id = UUID.randomUUID();
        Job job = jobOwnedBy(owner);
        JobRequest request = validRequest();
        when(jobDao.getJobById(id)).thenReturn(Optional.of(job));
        when(companyService.getOwnedByExactly(request.getCompanyId(), owner)).thenReturn(company);
        when(jobDao.saveJob(any())).thenAnswer(invocation -> invocation.getArgument(0));

        jobService.update(id, request, admin);

        // Must check the company against the job's actual owner, not the admin performing the edit.
        verify(companyService).getOwnedByExactly(request.getCompanyId(), owner);
        verify(companyService, never()).getOwnedByExactly(request.getCompanyId(), admin);
    }
}
