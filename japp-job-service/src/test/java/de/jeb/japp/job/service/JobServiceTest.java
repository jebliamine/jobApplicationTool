package de.jeb.japp.job.service;

import de.jeb.japp.commons.exceptions.company.CompanyAccessDeniedException;
import de.jeb.japp.commons.exceptions.job.JobAccessDeniedException;
import de.jeb.japp.commons.exceptions.job.JobNotFoundException;
import de.jeb.japp.commons.exceptions.job.JobValidationException;
import de.jeb.japp.commons.exceptions.tag.TagNotFoundException;
import de.jeb.japp.dao.application.ApplicationDao;
import de.jeb.japp.dao.generation.GenerationRequestDao;
import de.jeb.japp.dao.job.JobDao;
import de.jeb.japp.model.company.Company;
import de.jeb.japp.model.job.Job;
import de.jeb.japp.model.job.dto.JobRequest;
import de.jeb.japp.model.tag.Tag;
import de.jeb.japp.model.user.User;
import de.jeb.japp.model.user.UserRole;
import de.jeb.japp.tag.service.TagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
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
    @Mock
    private ApplicationDao applicationDao;
    @Mock
    private GenerationRequestDao generationRequestDao;
    @Mock
    private TagService tagService;

    private JobService jobService;

    private User owner;
    private User otherUser;
    private User admin;
    private Company company;

    @BeforeEach
    void setUp() {
        jobService = new JobService(jobDao, companyService, applicationDao, generationRequestDao, tagService);

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
                .isInstanceOf(JobValidationException.class);

        verifyNoInteractions(jobDao);
    }

    @Test
    void createRejectsMissingDescription() {
        JobRequest request = validRequest();
        request.setDescription(null);

        assertThatThrownBy(() -> jobService.create(request, owner))
                .isInstanceOf(JobValidationException.class);
    }

    @Test
    void createRejectsMissingCompany() {
        JobRequest request = validRequest();
        request.setCompanyId(null);

        assertThatThrownBy(() -> jobService.create(request, owner))
                .isInstanceOf(JobValidationException.class);

        verifyNoInteractions(jobDao);
    }

    @Test
    void createRejectsADuplicateTitleAtTheSameCompany() {
        JobRequest request = validRequest();
        when(companyService.getOwnedByExactly(request.getCompanyId(), owner)).thenReturn(company);
        when(jobDao.existsByOwnerAndTitleAndCompanyId(owner, request.getTitle(), company.getId())).thenReturn(true);

        assertThatThrownBy(() -> jobService.create(request, owner))
                .isInstanceOf(JobValidationException.class);

        verify(jobDao, never()).saveJob(any());
    }

    @Test
    void createAllowsTheSameTitleAtADifferentCompany() {
        JobRequest request = validRequest();
        when(companyService.getOwnedByExactly(request.getCompanyId(), owner)).thenReturn(company);
        when(jobDao.existsByOwnerAndTitleAndCompanyId(owner, request.getTitle(), company.getId())).thenReturn(false);
        when(jobDao.saveJob(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Job created = jobService.create(request, owner);

        assertThat(created.getTitle()).isEqualTo("Backend Engineer");
    }

    @Test
    void createRejectsCompanyOwnedBySomeoneElse() {
        JobRequest request = validRequest();
        when(companyService.getOwnedByExactly(request.getCompanyId(), owner))
                .thenThrow(new CompanyAccessDeniedException("You do not have access to this company."));

        assertThatThrownBy(() -> jobService.create(request, owner))
                .isInstanceOf(CompanyAccessDeniedException.class);

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
                .isInstanceOf(JobAccessDeniedException.class);
    }

    @Test
    void getThrowsNotFoundForMissingJob() {
        UUID id = UUID.randomUUID();
        when(jobDao.getJobById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobService.get(id, owner))
                .isInstanceOf(JobNotFoundException.class);
    }

    @Test
    void otherUserCannotDeleteSomeoneElsesJob() {
        UUID id = UUID.randomUUID();
        Job job = jobOwnedBy(owner);
        when(jobDao.getJobById(id)).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> jobService.delete(id, otherUser))
                .isInstanceOf(JobAccessDeniedException.class);

        verify(jobDao, never()).deleteJob(any());
    }

    @Test
    void deleteBlockedWhenJobHasApplications() {
        UUID id = UUID.randomUUID();
        Job job = jobOwnedBy(owner);
        when(jobDao.getJobById(id)).thenReturn(Optional.of(job));
        when(applicationDao.existsByJobId(any())).thenReturn(true);

        assertThatThrownBy(() -> jobService.delete(id, owner))
                .isInstanceOf(JobValidationException.class);

        verify(jobDao, never()).deleteJob(any());
    }

    @Test
    void deleteBlockedWhenJobHasGenerationRequests() {
        UUID id = UUID.randomUUID();
        Job job = jobOwnedBy(owner);
        when(jobDao.getJobById(id)).thenReturn(Optional.of(job));
        when(applicationDao.existsByJobId(any())).thenReturn(false);
        when(generationRequestDao.existsByJobId(any())).thenReturn(true);

        assertThatThrownBy(() -> jobService.delete(id, owner))
                .isInstanceOf(JobValidationException.class);

        verify(jobDao, never()).deleteJob(any());
    }

    @Test
    void deleteSucceedsWhenNoApplicationsOrGenerationRequestsReferenceJob() {
        UUID id = UUID.randomUUID();
        Job job = jobOwnedBy(owner);
        when(jobDao.getJobById(id)).thenReturn(Optional.of(job));
        when(applicationDao.existsByJobId(any())).thenReturn(false);
        when(generationRequestDao.existsByJobId(any())).thenReturn(false);

        jobService.delete(id, owner);

        verify(jobDao).deleteJob(job.getId());
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

    @Test
    void setTagsReplacesTheJobsTagSet() {
        UUID id = UUID.randomUUID();
        Job job = jobOwnedBy(owner);
        Tag tag = new Tag();
        tag.setName("Remote");
        List<UUID> tagIds = List.of(UUID.randomUUID());
        when(jobDao.getJobById(id)).thenReturn(Optional.of(job));
        when(tagService.getOwnedByExactlyAll(tagIds, owner)).thenReturn(List.of(tag));
        when(jobDao.saveJob(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Job result = jobService.setTags(id, tagIds, owner);

        assertThat(result.getTags()).containsExactly(tag);
    }

    @Test
    void setTagsByAdminValidatesAgainstJobOwnerNotAdmin() {
        UUID id = UUID.randomUUID();
        Job job = jobOwnedBy(owner);
        List<UUID> tagIds = List.of(UUID.randomUUID());
        when(jobDao.getJobById(id)).thenReturn(Optional.of(job));
        when(tagService.getOwnedByExactlyAll(tagIds, owner)).thenReturn(List.of());
        when(jobDao.saveJob(any())).thenAnswer(invocation -> invocation.getArgument(0));

        jobService.setTags(id, tagIds, admin);

        verify(tagService).getOwnedByExactlyAll(tagIds, owner);
        verify(tagService, never()).getOwnedByExactlyAll(tagIds, admin);
    }

    @Test
    void setTagsRejectsATagNotOwnedByTheJobsOwner() {
        UUID id = UUID.randomUUID();
        Job job = jobOwnedBy(owner);
        List<UUID> tagIds = List.of(UUID.randomUUID());
        when(jobDao.getJobById(id)).thenReturn(Optional.of(job));
        when(tagService.getOwnedByExactlyAll(tagIds, owner))
                .thenThrow(new TagNotFoundException("One or more tags were not found."));

        assertThatThrownBy(() -> jobService.setTags(id, tagIds, owner))
                .isInstanceOf(TagNotFoundException.class);

        verify(jobDao, never()).saveJob(any());
    }
}
