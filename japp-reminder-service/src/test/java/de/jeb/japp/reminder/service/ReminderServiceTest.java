package de.jeb.japp.reminder.service;

import de.jeb.japp.commons.exceptions.application.ApplicationAccessDeniedException;
import de.jeb.japp.commons.exceptions.application.ApplicationNotFoundException;
import de.jeb.japp.commons.exceptions.reminder.ReminderValidationException;
import de.jeb.japp.dao.application.ApplicationDao;
import de.jeb.japp.dao.reminder.ReminderDismissalDao;
import de.jeb.japp.model.application.Application;
import de.jeb.japp.model.application.ApplicationStatus;
import de.jeb.japp.model.application.InterviewStage;
import de.jeb.japp.model.company.Company;
import de.jeb.japp.model.job.Job;
import de.jeb.japp.model.reminder.ReminderDismissal;
import de.jeb.japp.model.reminder.ReminderKind;
import de.jeb.japp.model.reminder.ReminderSeverity;
import de.jeb.japp.model.reminder.dto.ReminderDismissRequest;
import de.jeb.japp.model.reminder.dto.ReminderResponse;
import de.jeb.japp.model.user.User;
import de.jeb.japp.model.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReminderServiceTest {

    @Mock
    private ApplicationDao applicationDao;
    @Mock
    private ReminderDismissalDao dismissalDao;

    private ReminderService reminderService;

    private User owner;
    private User otherUser;
    private final LocalDate today = LocalDate.now();

    @BeforeEach
    void setUp() {
        reminderService = new ReminderService(applicationDao, dismissalDao);

        owner = new User();
        owner.setId(UUID.randomUUID());
        owner.setRole(UserRole.USER);

        otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        otherUser.setRole(UserRole.USER);

        lenient().when(dismissalDao.getAllByUser(owner)).thenReturn(List.of());
    }

    private Application applicationWith(ApplicationStatus status, LocalDate deadline, LocalDate followUp, LocalDate interview) {
        Company company = new Company();
        company.setName("Acme");
        Job job = new Job();
        job.setTitle("Backend Engineer");
        job.setCompany(company);

        Application application = new Application();
        ReflectionTestUtils.setField(application, "id", UUID.randomUUID());
        application.setUser(owner);
        application.setJob(job);
        application.setStatus(status);
        application.setDeadline(deadline);
        application.setFollowUpDate(followUp);
        if (interview != null) {
            application.getInterviewStages().add(interviewStage(application, interview, false));
        }
        return application;
    }

    private InterviewStage interviewStage(Application application, LocalDate scheduledDate, boolean completed) {
        InterviewStage stage = new InterviewStage();
        stage.setApplication(application);
        stage.setTitle("Interview");
        stage.setScheduledDate(scheduledDate);
        stage.setCompleted(completed);
        return stage;
    }

    @Test
    void returnsAReminderForADeadlineWithinTheWindow() {
        Application application = applicationWith(ApplicationStatus.APPLIED, today.plusDays(3), null, null);
        when(applicationDao.getAllApplicationsByOwner(owner)).thenReturn(List.of(application));

        List<ReminderResponse> reminders = reminderService.list(owner);

        assertThat(reminders).hasSize(1);
        assertThat(reminders.get(0).getKind()).isEqualTo(ReminderKind.DEADLINE);
        assertThat(reminders.get(0).getDueDate()).isEqualTo(today.plusDays(3));
        assertThat(reminders.get(0).getJobTitle()).isEqualTo("Backend Engineer");
        assertThat(reminders.get(0).getCompanyName()).isEqualTo("Acme");
    }

    @Test
    void producesOneInterviewReminderPerUpcomingStage() {
        Application application = applicationWith(ApplicationStatus.INTERVIEWING, null, null, null);
        application.getInterviewStages().add(interviewStage(application, today.plusDays(1), false));
        application.getInterviewStages().add(interviewStage(application, today.plusDays(4), false));
        when(applicationDao.getAllApplicationsByOwner(owner)).thenReturn(List.of(application));

        List<ReminderResponse> reminders = reminderService.list(owner);

        assertThat(reminders).extracting(ReminderResponse::getDueDate)
                .containsExactlyInAnyOrder(today.plusDays(1), today.plusDays(4));
        assertThat(reminders).allMatch(reminder -> reminder.getKind() == ReminderKind.INTERVIEW);
    }

    @Test
    void aCompletedInterviewStageProducesNoReminder() {
        Application application = applicationWith(ApplicationStatus.INTERVIEWING, null, null, null);
        application.getInterviewStages().add(interviewStage(application, today, true));
        when(applicationDao.getAllApplicationsByOwner(owner)).thenReturn(List.of(application));

        assertThat(reminderService.list(owner)).isEmpty();
    }

    @Test
    void ignoresDatesBeyondTheSevenDayWindow() {
        Application application = applicationWith(ApplicationStatus.APPLIED, today.plusDays(8), null, null);
        when(applicationDao.getAllApplicationsByOwner(owner)).thenReturn(List.of(application));

        assertThat(reminderService.list(owner)).isEmpty();
    }

    @Test
    void ignoresClosedApplications() {
        Application application = applicationWith(ApplicationStatus.REJECTED, today, null, null);
        when(applicationDao.getAllApplicationsByOwner(owner)).thenReturn(List.of(application));

        assertThat(reminderService.list(owner)).isEmpty();
    }

    @Test
    void producesOneReminderPerActiveDateField() {
        Application application = applicationWith(ApplicationStatus.INTERVIEWING, today, today.plusDays(1), today.plusDays(2));
        when(applicationDao.getAllApplicationsByOwner(owner)).thenReturn(List.of(application));

        List<ReminderResponse> reminders = reminderService.list(owner);

        assertThat(reminders).extracting(ReminderResponse::getKind)
                .containsExactlyInAnyOrder(ReminderKind.DEADLINE, ReminderKind.FOLLOW_UP, ReminderKind.INTERVIEW);
    }

    @Test
    void anOverdueDateIsErrorSeverity() {
        Application application = applicationWith(ApplicationStatus.APPLIED, today.minusDays(1), null, null);
        when(applicationDao.getAllApplicationsByOwner(owner)).thenReturn(List.of(application));

        assertThat(reminderService.list(owner).get(0).getSeverity()).isEqualTo(ReminderSeverity.ERROR);
    }

    @Test
    void todayAndTomorrowAreWarningSeverity() {
        Application application = applicationWith(ApplicationStatus.APPLIED, today, today.plusDays(1), null);
        when(applicationDao.getAllApplicationsByOwner(owner)).thenReturn(List.of(application));

        assertThat(reminderService.list(owner))
                .extracting(ReminderResponse::getSeverity)
                .containsOnly(ReminderSeverity.WARNING);
    }

    @Test
    void aFurtherOutDateIsInfoSeverity() {
        Application application = applicationWith(ApplicationStatus.APPLIED, today.plusDays(3), null, null);
        when(applicationDao.getAllApplicationsByOwner(owner)).thenReturn(List.of(application));

        assertThat(reminderService.list(owner).get(0).getSeverity()).isEqualTo(ReminderSeverity.INFO);
    }

    @Test
    void resultsAreSortedByDueDate() {
        Application application = applicationWith(ApplicationStatus.INTERVIEWING, today.plusDays(2), today, today.plusDays(1));
        when(applicationDao.getAllApplicationsByOwner(owner)).thenReturn(List.of(application));

        assertThat(reminderService.list(owner))
                .extracting(ReminderResponse::getDueDate)
                .containsExactly(today, today.plusDays(1), today.plusDays(2));
    }

    private ReminderDismissal dismissalFor(Application application, ReminderKind kind, LocalDate dueDate, LocalDate snoozedUntil) {
        ReminderDismissal dismissal = new ReminderDismissal();
        dismissal.setUser(owner);
        dismissal.setApplication(application);
        dismissal.setKind(kind);
        dismissal.setDueDate(dueDate);
        dismissal.setSnoozedUntil(snoozedUntil);
        return dismissal;
    }

    @Test
    void aPermanentlyDismissedReminderIsExcluded() {
        Application application = applicationWith(ApplicationStatus.APPLIED, today.plusDays(3), null, null);
        when(applicationDao.getAllApplicationsByOwner(owner)).thenReturn(List.of(application));
        when(dismissalDao.getAllByUser(owner))
                .thenReturn(List.of(dismissalFor(application, ReminderKind.DEADLINE, today.plusDays(3), null)));

        assertThat(reminderService.list(owner)).isEmpty();
    }

    @Test
    void aReminderStillSnoozedIsExcluded() {
        Application application = applicationWith(ApplicationStatus.APPLIED, today.plusDays(3), null, null);
        when(applicationDao.getAllApplicationsByOwner(owner)).thenReturn(List.of(application));
        when(dismissalDao.getAllByUser(owner))
                .thenReturn(List.of(dismissalFor(application, ReminderKind.DEADLINE, today.plusDays(3), today.plusDays(1))));

        assertThat(reminderService.list(owner)).isEmpty();
    }

    @Test
    void aReminderWhoseSnoozeHasExpiredReappears() {
        Application application = applicationWith(ApplicationStatus.APPLIED, today.plusDays(3), null, null);
        when(applicationDao.getAllApplicationsByOwner(owner)).thenReturn(List.of(application));
        when(dismissalDao.getAllByUser(owner))
                .thenReturn(List.of(dismissalFor(application, ReminderKind.DEADLINE, today.plusDays(3), today.minusDays(1))));

        assertThat(reminderService.list(owner)).hasSize(1);
    }

    @Test
    void aDismissalForADifferentDueDateDoesNotSuppressTheCurrentOne() {
        Application application = applicationWith(ApplicationStatus.APPLIED, today.plusDays(3), null, null);
        when(applicationDao.getAllApplicationsByOwner(owner)).thenReturn(List.of(application));
        // The deadline was moved after this dismissal was recorded — a fresh reminder should surface.
        when(dismissalDao.getAllByUser(owner))
                .thenReturn(List.of(dismissalFor(application, ReminderKind.DEADLINE, today.plusDays(2), null)));

        assertThat(reminderService.list(owner)).hasSize(1);
    }

    private ReminderDismissRequest dismissRequest(UUID applicationId, ReminderKind kind, LocalDate dueDate, LocalDate snoozedUntil) {
        ReminderDismissRequest request = new ReminderDismissRequest();
        request.setApplicationId(applicationId);
        request.setKind(kind);
        request.setDueDate(dueDate);
        request.setSnoozedUntil(snoozedUntil);
        return request;
    }

    @Test
    void dismissCreatesANewDismissalWhenNoneExists() {
        Application application = applicationWith(ApplicationStatus.APPLIED, today, null, null);
        when(applicationDao.getApplicationById(application.getId())).thenReturn(Optional.of(application));
        when(dismissalDao.getByUserAndApplicationAndKindAndDueDate(owner, application.getId(), ReminderKind.DEADLINE, today))
                .thenReturn(Optional.empty());
        when(dismissalDao.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        reminderService.dismiss(dismissRequest(application.getId(), ReminderKind.DEADLINE, today, null), owner);

        ArgumentCaptor<ReminderDismissal> captor = ArgumentCaptor.forClass(ReminderDismissal.class);
        verify(dismissalDao).save(captor.capture());
        assertThat(captor.getValue().getUser()).isEqualTo(owner);
        assertThat(captor.getValue().getApplication()).isEqualTo(application);
        assertThat(captor.getValue().getSnoozedUntil()).isNull();
    }

    @Test
    void reDismissingUpdatesTheExistingRowInsteadOfCreatingANewOne() {
        Application application = applicationWith(ApplicationStatus.APPLIED, today, null, null);
        ReminderDismissal existing = dismissalFor(application, ReminderKind.DEADLINE, today, null);
        when(applicationDao.getApplicationById(application.getId())).thenReturn(Optional.of(application));
        when(dismissalDao.getByUserAndApplicationAndKindAndDueDate(owner, application.getId(), ReminderKind.DEADLINE, today))
                .thenReturn(Optional.of(existing));
        when(dismissalDao.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        reminderService.dismiss(dismissRequest(application.getId(), ReminderKind.DEADLINE, today, today.plusDays(2)), owner);

        ArgumentCaptor<ReminderDismissal> captor = ArgumentCaptor.forClass(ReminderDismissal.class);
        verify(dismissalDao).save(captor.capture());
        assertThat(captor.getValue()).isSameAs(existing);
        assertThat(captor.getValue().getSnoozedUntil()).isEqualTo(today.plusDays(2));
    }

    @Test
    void dismissRejectsAnApplicationOwnedBySomeoneElse() {
        Application application = applicationWith(ApplicationStatus.APPLIED, today, null, null);
        when(applicationDao.getApplicationById(application.getId())).thenReturn(Optional.of(application));

        assertThatThrownBy(() ->
                reminderService.dismiss(dismissRequest(application.getId(), ReminderKind.DEADLINE, today, null), otherUser))
                .isInstanceOf(ApplicationAccessDeniedException.class);
    }

    @Test
    void dismissRejectsAnUnknownApplication() {
        UUID unknownId = UUID.randomUUID();
        when(applicationDao.getApplicationById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reminderService.dismiss(dismissRequest(unknownId, ReminderKind.DEADLINE, today, null), owner))
                .isInstanceOf(ApplicationNotFoundException.class);
    }

    @Test
    void dismissRejectsAMissingApplicationId() {
        assertThatThrownBy(() -> reminderService.dismiss(dismissRequest(null, ReminderKind.DEADLINE, today, null), owner))
                .isInstanceOf(ReminderValidationException.class);
    }

    @Test
    void dismissRejectsAMissingKind() {
        assertThatThrownBy(() -> reminderService.dismiss(dismissRequest(UUID.randomUUID(), null, today, null), owner))
                .isInstanceOf(ReminderValidationException.class);
    }

    @Test
    void dismissRejectsAMissingDueDate() {
        assertThatThrownBy(() -> reminderService.dismiss(dismissRequest(UUID.randomUUID(), ReminderKind.DEADLINE, null, null), owner))
                .isInstanceOf(ReminderValidationException.class);
    }
}
