package de.jeb.japp.application.service;

import de.jeb.japp.commons.exceptions.application.ApplicationAccessDeniedException;
import de.jeb.japp.commons.exceptions.application.ApplicationNotFoundException;
import de.jeb.japp.commons.exceptions.application.ApplicationValidationException;
import de.jeb.japp.commons.exceptions.coverletter.CoverLetterAccessDeniedException;
import de.jeb.japp.commons.exceptions.cv.CVAccessDeniedException;
import de.jeb.japp.commons.exceptions.application.InterviewStageNotFoundException;
import de.jeb.japp.commons.exceptions.job.JobAccessDeniedException;
import de.jeb.japp.commons.exceptions.tag.TagNotFoundException;
import de.jeb.japp.dao.application.ApplicationDao;
import de.jeb.japp.dao.coverletter.CoverLetterDao;
import de.jeb.japp.dao.cv.CVDao;
import de.jeb.japp.dao.job.JobDao;
import de.jeb.japp.model.application.Application;
import de.jeb.japp.model.application.ApplicationStatus;
import de.jeb.japp.model.application.InterviewStage;
import de.jeb.japp.model.application.dto.ApplicationRequest;
import de.jeb.japp.model.application.dto.InterviewStageRequest;
import de.jeb.japp.model.coverLetter.CoverLetter;
import de.jeb.japp.model.cv.CVDocument;
import de.jeb.japp.model.job.Job;
import de.jeb.japp.model.tag.Tag;
import de.jeb.japp.model.user.User;
import de.jeb.japp.model.user.UserRole;
import de.jeb.japp.tag.service.TagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private ApplicationDao applicationDao;
    @Mock
    private JobDao jobDao;
    @Mock
    private CVDao cvDao;
    @Mock
    private CoverLetterDao coverLetterDao;
    @Mock
    private TagService tagService;

    private ApplicationService applicationService;

    private User owner;
    private User otherUser;
    private User admin;
    private Job job;
    private CVDocument cv;
    private CoverLetter coverLetter;
    private CoverLetter otherCoverLetter;

    @BeforeEach
    void setUp() {
        applicationService = new ApplicationService(applicationDao, jobDao, cvDao, coverLetterDao, tagService);

        owner = new User();
        owner.setId(UUID.randomUUID());
        owner.setRole(UserRole.USER);

        otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        otherUser.setRole(UserRole.USER);

        admin = new User();
        admin.setId(UUID.randomUUID());
        admin.setRole(UserRole.ADMIN);

        job = new Job();
        job.setOwner(owner);
        job.setTitle("Backend Engineer");

        cv = new CVDocument();
        cv.setOwner(owner);
        cv.setTitle("My Resume");

        coverLetter = new CoverLetter();
        coverLetter.setOwner(owner);
        coverLetter.setResultText("Dear Hiring Team, ...");

        otherCoverLetter = new CoverLetter();
        otherCoverLetter.setOwner(otherUser);
        otherCoverLetter.setResultText("Someone else's cover letter.");
    }

    private ApplicationRequest validRequest() {
        ApplicationRequest request = new ApplicationRequest();
        request.setJobId(UUID.randomUUID());
        request.setCvDocumentId(UUID.randomUUID());
        request.setStatus(ApplicationStatus.APPLIED);
        request.setAppliedAt(LocalDate.of(2026, 1, 1));
        request.setNotes("Applied via referral.");
        return request;
    }

    private Application applicationOwnedBy(User user) {
        Application application = new Application();
        application.setUser(user);
        application.setJob(job);
        application.setStatus(ApplicationStatus.APPLIED);
        return application;
    }

    @Test
    void createSetsOwnerFromAuthenticatedUserNotFromRequest() {
        ApplicationRequest request = validRequest();
        when(jobDao.getJobById(request.getJobId())).thenReturn(Optional.of(job));
        when(cvDao.getCVById(request.getCvDocumentId())).thenReturn(Optional.of(cv));
        when(applicationDao.saveApplication(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Application created = applicationService.create(request, owner);

        assertThat(created.getUser()).isEqualTo(owner);
        assertThat(created.getJob()).isEqualTo(job);
        assertThat(created.getCvDocument()).isEqualTo(cv);
        assertThat(created.getStatus()).isEqualTo(ApplicationStatus.APPLIED);
        assertThat(created.getCreatedAt()).isNotNull();
        assertThat(created.getUpdatedAt()).isNotNull();
    }

    @Test
    void createRejectsMissingJob() {
        ApplicationRequest request = validRequest();
        request.setJobId(null);

        assertThatThrownBy(() -> applicationService.create(request, owner))
                .isInstanceOf(ApplicationValidationException.class);

        verifyNoInteractions(applicationDao);
    }

    @Test
    void createRejectsJobOwnedBySomeoneElse() {
        ApplicationRequest request = validRequest();
        Job othersJob = new Job();
        othersJob.setOwner(otherUser);
        when(jobDao.getJobById(request.getJobId())).thenReturn(Optional.of(othersJob));

        assertThatThrownBy(() -> applicationService.create(request, owner))
                .isInstanceOf(JobAccessDeniedException.class);

        verify(applicationDao, never()).saveApplication(any());
    }

    @Test
    void createRejectsCvOwnedBySomeoneElse() {
        ApplicationRequest request = validRequest();
        CVDocument othersCv = new CVDocument();
        othersCv.setOwner(otherUser);
        when(jobDao.getJobById(request.getJobId())).thenReturn(Optional.of(job));
        when(cvDao.getCVById(request.getCvDocumentId())).thenReturn(Optional.of(othersCv));

        assertThatThrownBy(() -> applicationService.create(request, owner))
                .isInstanceOf(CVAccessDeniedException.class);

        verify(applicationDao, never()).saveApplication(any());
    }

    @Test
    void createAllowsNullCv() {
        ApplicationRequest request = validRequest();
        request.setCvDocumentId(null);
        when(jobDao.getJobById(request.getJobId())).thenReturn(Optional.of(job));
        when(applicationDao.saveApplication(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Application created = applicationService.create(request, owner);

        assertThat(created.getCvDocument()).isNull();
        verifyNoInteractions(cvDao);
    }

    @Test
    void createWithUsersCoverLetterSucceeds() {
        ApplicationRequest request = validRequest();
        UUID coverLetterId = UUID.randomUUID();
        request.setCoverLetterId(coverLetterId);
        when(jobDao.getJobById(request.getJobId())).thenReturn(Optional.of(job));
        when(cvDao.getCVById(request.getCvDocumentId())).thenReturn(Optional.of(cv));
        when(coverLetterDao.getCoverLetterById(coverLetterId)).thenReturn(Optional.of(coverLetter));
        when(applicationDao.saveApplication(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Application created = applicationService.create(request, owner);

        assertThat(created.getCoverLetter()).isEqualTo(coverLetter);
    }

    @Test
    void createRejectsCoverLetterOwnedBySomeoneElse() {
        ApplicationRequest request = validRequest();
        UUID coverLetterId = UUID.randomUUID();
        request.setCoverLetterId(coverLetterId);
        when(jobDao.getJobById(request.getJobId())).thenReturn(Optional.of(job));
        when(cvDao.getCVById(request.getCvDocumentId())).thenReturn(Optional.of(cv));
        when(coverLetterDao.getCoverLetterById(coverLetterId)).thenReturn(Optional.of(otherCoverLetter));

        assertThatThrownBy(() -> applicationService.create(request, owner))
                .isInstanceOf(CoverLetterAccessDeniedException.class);

        verify(applicationDao, never()).saveApplication(any());
    }

    @Test
    void createDefaultsStatusAndAppliedAtWhenOmitted() {
        ApplicationRequest request = validRequest();
        request.setStatus(null);
        request.setAppliedAt(null);
        request.setCvDocumentId(null);
        when(jobDao.getJobById(request.getJobId())).thenReturn(Optional.of(job));
        when(applicationDao.saveApplication(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Application created = applicationService.create(request, owner);

        assertThat(created.getStatus()).isEqualTo(ApplicationStatus.APPLIED);
        assertThat(created.getAppliedAt()).isEqualTo(LocalDate.now());
    }

    @Test
    void ownerCanGetTheirOwnApplication() {
        UUID id = UUID.randomUUID();
        Application application = applicationOwnedBy(owner);
        when(applicationDao.getApplicationById(id)).thenReturn(Optional.of(application));

        assertThat(applicationService.get(id, owner)).isEqualTo(application);
    }

    @Test
    void adminCanGetAnyApplication() {
        UUID id = UUID.randomUUID();
        Application application = applicationOwnedBy(owner);
        when(applicationDao.getApplicationById(id)).thenReturn(Optional.of(application));

        assertThat(applicationService.get(id, admin)).isEqualTo(application);
    }

    @Test
    void otherUserCannotGetSomeoneElsesApplication() {
        UUID id = UUID.randomUUID();
        Application application = applicationOwnedBy(owner);
        when(applicationDao.getApplicationById(id)).thenReturn(Optional.of(application));

        assertThatThrownBy(() -> applicationService.get(id, otherUser))
                .isInstanceOf(ApplicationAccessDeniedException.class);
    }

    @Test
    void getThrowsNotFoundForMissingApplication() {
        UUID id = UUID.randomUUID();
        when(applicationDao.getApplicationById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> applicationService.get(id, owner))
                .isInstanceOf(ApplicationNotFoundException.class);
    }

    @Test
    void listReturnsAllApplicationsForAdmin() {
        applicationService.list(admin);
        verify(applicationDao).getAllApplications();
        verify(applicationDao, never()).getAllApplicationsByOwner(any());
    }

    @Test
    void listReturnsOnlyOwnApplicationsForRegularUser() {
        applicationService.list(owner);
        verify(applicationDao).getAllApplicationsByOwner(owner);
        verify(applicationDao, never()).getAllApplications();
    }

    @Test
    void listReturnsWhatDaoProvides() {
        when(applicationDao.getAllApplicationsByOwner(owner)).thenReturn(List.of(applicationOwnedBy(owner)));

        List<Application> result = applicationService.list(owner);

        assertThat(result).hasSize(1);
    }

    @Test
    void updateByAdminResolvesJobAndCvAgainstApplicationOwnerNotAdmin() {
        UUID id = UUID.randomUUID();
        Application application = applicationOwnedBy(owner);
        ApplicationRequest request = validRequest();
        when(applicationDao.getApplicationById(id)).thenReturn(Optional.of(application));
        when(jobDao.getJobById(request.getJobId())).thenReturn(Optional.of(job));
        when(cvDao.getCVById(request.getCvDocumentId())).thenReturn(Optional.of(cv));
        when(applicationDao.saveApplication(any())).thenAnswer(invocation -> invocation.getArgument(0));

        applicationService.update(id, request, admin);

        // The job/CV must be checked against the application's actual owner, not the admin performing the edit.
        verify(jobDao).getJobById(request.getJobId());
        assertThat(application.getJob()).isEqualTo(job);
    }

    @Test
    void updateChangesStatus() {
        UUID id = UUID.randomUUID();
        Application application = applicationOwnedBy(owner);
        ApplicationRequest request = validRequest();
        request.setStatus(ApplicationStatus.INTERVIEWING);
        when(applicationDao.getApplicationById(id)).thenReturn(Optional.of(application));
        when(jobDao.getJobById(request.getJobId())).thenReturn(Optional.of(job));
        when(cvDao.getCVById(request.getCvDocumentId())).thenReturn(Optional.of(cv));
        when(applicationDao.saveApplication(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Application updated = applicationService.update(id, request, owner);

        assertThat(updated.getStatus()).isEqualTo(ApplicationStatus.INTERVIEWING);
    }

    @Test
    void updateCanAttachACoverLetter() {
        UUID id = UUID.randomUUID();
        Application application = applicationOwnedBy(owner);
        ApplicationRequest request = validRequest();
        UUID coverLetterId = UUID.randomUUID();
        request.setCoverLetterId(coverLetterId);
        when(applicationDao.getApplicationById(id)).thenReturn(Optional.of(application));
        when(jobDao.getJobById(request.getJobId())).thenReturn(Optional.of(job));
        when(cvDao.getCVById(request.getCvDocumentId())).thenReturn(Optional.of(cv));
        when(coverLetterDao.getCoverLetterById(coverLetterId)).thenReturn(Optional.of(coverLetter));
        when(applicationDao.saveApplication(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Application updated = applicationService.update(id, request, owner);

        assertThat(updated.getCoverLetter()).isEqualTo(coverLetter);
    }

    @Test
    void updateCanChangeFromOneCoverLetterToAnother() {
        UUID id = UUID.randomUUID();
        Application application = applicationOwnedBy(owner);
        application.setCoverLetter(coverLetter);
        CoverLetter replacementCoverLetter = new CoverLetter();
        replacementCoverLetter.setOwner(owner);
        replacementCoverLetter.setResultText("A different cover letter.");
        UUID replacementId = UUID.randomUUID();

        ApplicationRequest request = validRequest();
        request.setCoverLetterId(replacementId);
        when(applicationDao.getApplicationById(id)).thenReturn(Optional.of(application));
        when(jobDao.getJobById(request.getJobId())).thenReturn(Optional.of(job));
        when(cvDao.getCVById(request.getCvDocumentId())).thenReturn(Optional.of(cv));
        when(coverLetterDao.getCoverLetterById(replacementId)).thenReturn(Optional.of(replacementCoverLetter));
        when(applicationDao.saveApplication(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Application updated = applicationService.update(id, request, owner);

        assertThat(updated.getCoverLetter()).isEqualTo(replacementCoverLetter);
    }

    @Test
    void updateCanRemoveItsCoverLetter() {
        UUID id = UUID.randomUUID();
        Application application = applicationOwnedBy(owner);
        application.setCoverLetter(coverLetter);

        ApplicationRequest request = validRequest();
        request.setCoverLetterId(null);
        when(applicationDao.getApplicationById(id)).thenReturn(Optional.of(application));
        when(jobDao.getJobById(request.getJobId())).thenReturn(Optional.of(job));
        when(cvDao.getCVById(request.getCvDocumentId())).thenReturn(Optional.of(cv));
        when(applicationDao.saveApplication(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Application updated = applicationService.update(id, request, owner);

        assertThat(updated.getCoverLetter()).isNull();
        verifyNoInteractions(coverLetterDao);
    }

    @Test
    void updateRejectsCoverLetterOwnedBySomeoneElse() {
        UUID id = UUID.randomUUID();
        Application application = applicationOwnedBy(owner);
        ApplicationRequest request = validRequest();
        UUID coverLetterId = UUID.randomUUID();
        request.setCoverLetterId(coverLetterId);
        when(applicationDao.getApplicationById(id)).thenReturn(Optional.of(application));
        when(jobDao.getJobById(request.getJobId())).thenReturn(Optional.of(job));
        when(cvDao.getCVById(request.getCvDocumentId())).thenReturn(Optional.of(cv));
        when(coverLetterDao.getCoverLetterById(coverLetterId)).thenReturn(Optional.of(otherCoverLetter));

        assertThatThrownBy(() -> applicationService.update(id, request, owner))
                .isInstanceOf(CoverLetterAccessDeniedException.class);

        verify(applicationDao, never()).saveApplication(any());
    }

    @Test
    void updateByAdminResolvesCoverLetterAgainstApplicationOwnerNotAdmin() {
        UUID id = UUID.randomUUID();
        Application application = applicationOwnedBy(owner);
        ApplicationRequest request = validRequest();
        UUID coverLetterId = UUID.randomUUID();
        request.setCoverLetterId(coverLetterId);
        when(applicationDao.getApplicationById(id)).thenReturn(Optional.of(application));
        when(jobDao.getJobById(request.getJobId())).thenReturn(Optional.of(job));
        when(cvDao.getCVById(request.getCvDocumentId())).thenReturn(Optional.of(cv));
        when(coverLetterDao.getCoverLetterById(coverLetterId)).thenReturn(Optional.of(coverLetter));
        when(applicationDao.saveApplication(any())).thenAnswer(invocation -> invocation.getArgument(0));

        applicationService.update(id, request, admin);

        // Must check the cover letter against the application's actual owner, not the admin performing the edit.
        assertThat(application.getCoverLetter()).isEqualTo(coverLetter);
    }

    @Test
    void otherUserCannotUpdateSomeoneElsesApplication() {
        UUID id = UUID.randomUUID();
        Application application = applicationOwnedBy(owner);
        ApplicationRequest request = validRequest();
        when(applicationDao.getApplicationById(id)).thenReturn(Optional.of(application));

        assertThatThrownBy(() -> applicationService.update(id, request, otherUser))
                .isInstanceOf(ApplicationAccessDeniedException.class);

        verify(applicationDao, never()).saveApplication(any());
    }

    @Test
    void otherUserCannotDeleteSomeoneElsesApplication() {
        UUID id = UUID.randomUUID();
        Application application = applicationOwnedBy(owner);
        when(applicationDao.getApplicationById(id)).thenReturn(Optional.of(application));

        assertThatThrownBy(() -> applicationService.delete(id, otherUser))
                .isInstanceOf(ApplicationAccessDeniedException.class);

        verify(applicationDao, never()).deleteApplication(any());
    }

    @Test
    void deleteSucceedsForOwner() {
        UUID id = UUID.randomUUID();
        Application application = applicationOwnedBy(owner);
        when(applicationDao.getApplicationById(id)).thenReturn(Optional.of(application));

        applicationService.delete(id, owner);

        verify(applicationDao).deleteApplication(application.getId());
    }

    @Test
    void setTagsReplacesTheApplicationsTagSet() {
        UUID id = UUID.randomUUID();
        Application application = applicationOwnedBy(owner);
        Tag tag = new Tag();
        tag.setName("Dream job");
        List<UUID> tagIds = List.of(UUID.randomUUID());
        when(applicationDao.getApplicationById(id)).thenReturn(Optional.of(application));
        when(tagService.getOwnedByExactlyAll(tagIds, owner)).thenReturn(List.of(tag));
        when(applicationDao.saveApplication(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Application result = applicationService.setTags(id, tagIds, owner);

        assertThat(result.getTags()).containsExactly(tag);
    }

    @Test
    void setTagsByAdminValidatesAgainstApplicationOwnerNotAdmin() {
        UUID id = UUID.randomUUID();
        Application application = applicationOwnedBy(owner);
        List<UUID> tagIds = List.of(UUID.randomUUID());
        when(applicationDao.getApplicationById(id)).thenReturn(Optional.of(application));
        when(tagService.getOwnedByExactlyAll(tagIds, owner)).thenReturn(List.of());
        when(applicationDao.saveApplication(any())).thenAnswer(invocation -> invocation.getArgument(0));

        applicationService.setTags(id, tagIds, admin);

        verify(tagService).getOwnedByExactlyAll(tagIds, owner);
        verify(tagService, never()).getOwnedByExactlyAll(tagIds, admin);
    }

    @Test
    void setTagsRejectsATagNotOwnedByTheApplicationsOwner() {
        UUID id = UUID.randomUUID();
        Application application = applicationOwnedBy(owner);
        List<UUID> tagIds = List.of(UUID.randomUUID());
        when(applicationDao.getApplicationById(id)).thenReturn(Optional.of(application));
        when(tagService.getOwnedByExactlyAll(tagIds, owner))
                .thenThrow(new TagNotFoundException("One or more tags were not found."));

        assertThatThrownBy(() -> applicationService.setTags(id, tagIds, owner))
                .isInstanceOf(TagNotFoundException.class);

        verify(applicationDao, never()).saveApplication(any());
    }

    private InterviewStageRequest stageRequest(String title, LocalDate scheduledDate, boolean completed) {
        InterviewStageRequest request = new InterviewStageRequest();
        request.setTitle(title);
        request.setScheduledDate(scheduledDate);
        request.setCompleted(completed);
        return request;
    }

    @Test
    void addInterviewStageAppendsANewStage() {
        UUID id = UUID.randomUUID();
        Application application = applicationOwnedBy(owner);
        when(applicationDao.getApplicationById(id)).thenReturn(Optional.of(application));
        when(applicationDao.saveApplication(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Application result = applicationService.addInterviewStage(
                id, stageRequest("Phone Screen", LocalDate.of(2026, 2, 1), false), owner);

        assertThat(result.getInterviewStages()).hasSize(1);
        InterviewStage stage = result.getInterviewStages().get(0);
        assertThat(stage.getTitle()).isEqualTo("Phone Screen");
        assertThat(stage.getScheduledDate()).isEqualTo(LocalDate.of(2026, 2, 1));
        assertThat(stage.getApplication()).isSameAs(application);
        assertThat(stage.isCompleted()).isFalse();
    }

    @Test
    void addInterviewStageRejectsABlankTitle() {
        UUID id = UUID.randomUUID();
        Application application = applicationOwnedBy(owner);
        when(applicationDao.getApplicationById(id)).thenReturn(Optional.of(application));

        assertThatThrownBy(() -> applicationService.addInterviewStage(id, stageRequest("  ", null, false), owner))
                .isInstanceOf(ApplicationValidationException.class);

        verify(applicationDao, never()).saveApplication(any());
    }

    @Test
    void addInterviewStageRejectsAnApplicationOwnedBySomeoneElse() {
        UUID id = UUID.randomUUID();
        Application application = applicationOwnedBy(owner);
        when(applicationDao.getApplicationById(id)).thenReturn(Optional.of(application));

        assertThatThrownBy(() ->
                applicationService.addInterviewStage(id, stageRequest("Onsite", null, false), otherUser))
                .isInstanceOf(ApplicationAccessDeniedException.class);
    }

    @Test
    void updateInterviewStageChangesTheExistingStageInPlace() {
        UUID id = UUID.randomUUID();
        Application application = applicationOwnedBy(owner);
        InterviewStage stage = new InterviewStage();
        stage.setApplication(application);
        stage.setTitle("Phone Screen");
        ReflectionTestUtils.setField(stage, "id", UUID.randomUUID());
        application.getInterviewStages().add(stage);
        when(applicationDao.getApplicationById(id)).thenReturn(Optional.of(application));
        when(applicationDao.saveApplication(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Application result = applicationService.updateInterviewStage(
                id, stage.getId(), stageRequest("Onsite", LocalDate.of(2026, 3, 1), true), owner);

        assertThat(result.getInterviewStages()).hasSize(1);
        InterviewStage updated = result.getInterviewStages().get(0);
        assertThat(updated).isSameAs(stage);
        assertThat(updated.getTitle()).isEqualTo("Onsite");
        assertThat(updated.getScheduledDate()).isEqualTo(LocalDate.of(2026, 3, 1));
        assertThat(updated.isCompleted()).isTrue();
    }

    @Test
    void updateInterviewStageRejectsAnUnknownStageId() {
        UUID id = UUID.randomUUID();
        Application application = applicationOwnedBy(owner);
        when(applicationDao.getApplicationById(id)).thenReturn(Optional.of(application));

        assertThatThrownBy(() ->
                applicationService.updateInterviewStage(id, UUID.randomUUID(), stageRequest("Onsite", null, false), owner))
                .isInstanceOf(InterviewStageNotFoundException.class);
    }

    @Test
    void removeInterviewStageDeletesOnlyTheMatchingStage() {
        UUID id = UUID.randomUUID();
        Application application = applicationOwnedBy(owner);
        InterviewStage keep = new InterviewStage();
        keep.setApplication(application);
        keep.setTitle("Onsite");
        ReflectionTestUtils.setField(keep, "id", UUID.randomUUID());
        InterviewStage remove = new InterviewStage();
        remove.setApplication(application);
        remove.setTitle("Phone Screen");
        ReflectionTestUtils.setField(remove, "id", UUID.randomUUID());
        application.getInterviewStages().add(keep);
        application.getInterviewStages().add(remove);
        when(applicationDao.getApplicationById(id)).thenReturn(Optional.of(application));
        when(applicationDao.saveApplication(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Application result = applicationService.removeInterviewStage(id, remove.getId(), owner);

        assertThat(result.getInterviewStages()).containsExactly(keep);
    }

    @Test
    void exportCsvUsesTheSameOwnerScopedListAsList() {
        when(applicationDao.getAllApplicationsByOwner(owner)).thenReturn(List.of(applicationOwnedBy(owner)));

        String csv = applicationService.exportCsv(owner);

        assertThat(csv).contains("Backend Engineer");
        verify(applicationDao).getAllApplicationsByOwner(owner);
        verify(applicationDao, never()).getAllApplications();
    }

    @Test
    void exportCsvUsesTheGlobalListForAdmin() {
        when(applicationDao.getAllApplications()).thenReturn(List.of(applicationOwnedBy(owner)));

        applicationService.exportCsv(admin);

        verify(applicationDao).getAllApplications();
        verify(applicationDao, never()).getAllApplicationsByOwner(any());
    }
}
