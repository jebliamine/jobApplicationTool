package de.jeb.japp.reminder.service;

import de.jeb.japp.commons.exceptions.application.ApplicationAccessDeniedException;
import de.jeb.japp.commons.exceptions.application.ApplicationNotFoundException;
import de.jeb.japp.commons.exceptions.reminder.ReminderValidationException;
import de.jeb.japp.dao.application.ApplicationDao;
import de.jeb.japp.dao.reminder.ReminderDismissalDao;
import de.jeb.japp.model.application.Application;
import de.jeb.japp.model.application.ApplicationStatus;
import de.jeb.japp.model.application.InterviewStage;
import de.jeb.japp.model.reminder.ReminderDismissal;
import de.jeb.japp.model.reminder.ReminderKind;
import de.jeb.japp.model.reminder.ReminderSeverity;
import de.jeb.japp.model.reminder.dto.ReminderDismissRequest;
import de.jeb.japp.model.reminder.dto.ReminderResponse;
import de.jeb.japp.model.user.User;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Reminders are always computed fresh from the Application's own deadline/followUpDate/
 * interviewDate fields — never duplicated into their own table — and are always scoped to the
 * requester's own applications, with no ADMIN-sees-all exception (unlike every other list() in
 * this app): a notification feed showing someone else's reminders would make no sense regardless
 * of role. Only the fact that a specific reminder instance was dismissed/snoozed is persisted
 * (see {@link ReminderDismissal}).
 */
@Service
public class ReminderService {

    private static final int REMINDER_WINDOW_DAYS = 7;
    private static final Set<ApplicationStatus> ACTIVE_STATUSES = Set.of(
            ApplicationStatus.APPLIED, ApplicationStatus.PHONE_SCREEN, ApplicationStatus.INTERVIEWING, ApplicationStatus.OFFER);

    private final ApplicationDao applicationDao;
    private final ReminderDismissalDao dismissalDao;

    public ReminderService(ApplicationDao applicationDao, ReminderDismissalDao dismissalDao) {
        this.applicationDao = applicationDao;
        this.dismissalDao = dismissalDao;
    }

    public List<ReminderResponse> list(User requester) {
        List<Application> applications = applicationDao.getAllApplicationsByOwner(requester);
        List<ReminderDismissal> dismissals = dismissalDao.getAllByUser(requester);
        LocalDate today = LocalDate.now();
        LocalDate horizon = today.plusDays(REMINDER_WINDOW_DAYS);

        List<ReminderResponse> reminders = new ArrayList<>();
        for (Application application : applications) {
            if (!ACTIVE_STATUSES.contains(application.getStatus())) {
                continue;
            }
            addIfDue(reminders, application, ReminderKind.DEADLINE, application.getDeadline(), today, horizon, dismissals);
            addIfDue(reminders, application, ReminderKind.FOLLOW_UP, application.getFollowUpDate(), today, horizon, dismissals);
            // One reminder per not-yet-completed interview round — a multi-round pipeline can have
            // several upcoming stages at once, each dismissed/snoozed independently by its own date.
            for (InterviewStage stage : application.getInterviewStages()) {
                if (!stage.isCompleted()) {
                    addIfDue(reminders, application, ReminderKind.INTERVIEW, stage.getScheduledDate(), today, horizon, dismissals);
                }
            }
        }
        reminders.sort((a, b) -> a.getDueDate().compareTo(b.getDueDate()));
        return reminders;
    }

    /**
     * Ownership is checked against the Application directly (never an admin bypass) — dismissing
     * is an inherently personal notification-state action, not a resource an admin edits on
     * someone else's behalf. Upserts by (user, application, kind, dueDate) so re-dismissing or
     * re-snoozing the same reminder instance updates one row instead of accumulating duplicates.
     */
    public void dismiss(ReminderDismissRequest request, User owner) {
        validate(request);
        Application application = applicationDao.getApplicationById(request.getApplicationId())
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found."));
        if (application.getUser() == null || !application.getUser().getId().equals(owner.getId())) {
            throw new ApplicationAccessDeniedException("You do not have access to this application.");
        }

        ReminderDismissal dismissal = dismissalDao
                .getByUserAndApplicationAndKindAndDueDate(owner, application.getId(), request.getKind(), request.getDueDate())
                .orElseGet(() -> {
                    ReminderDismissal created = new ReminderDismissal();
                    created.setUser(owner);
                    created.setApplication(application);
                    created.setKind(request.getKind());
                    created.setDueDate(request.getDueDate());
                    created.setCreatedAt(LocalDateTime.now());
                    return created;
                });
        dismissal.setSnoozedUntil(request.getSnoozedUntil());
        dismissalDao.save(dismissal);
    }

    private void addIfDue(
            List<ReminderResponse> reminders,
            Application application,
            ReminderKind kind,
            LocalDate dueDate,
            LocalDate today,
            LocalDate horizon,
            List<ReminderDismissal> dismissals
    ) {
        if (dueDate == null || dueDate.isAfter(horizon) || isSuppressed(dismissals, application.getId(), kind, dueDate, today)) {
            return;
        }
        reminders.add(new ReminderResponse(
                application.getId(),
                application.getJob().getTitle(),
                application.getJob().getCompany().getName(),
                kind,
                dueDate,
                severityFor(dueDate, today)
        ));
    }

    private boolean isSuppressed(List<ReminderDismissal> dismissals, java.util.UUID applicationId, ReminderKind kind, LocalDate dueDate, LocalDate today) {
        return dismissals.stream().anyMatch(dismissal ->
                dismissal.getApplication().getId().equals(applicationId)
                        && dismissal.getKind() == kind
                        && dismissal.getDueDate().equals(dueDate)
                        && (dismissal.getSnoozedUntil() == null || today.isBefore(dismissal.getSnoozedUntil())));
    }

    private ReminderSeverity severityFor(LocalDate dueDate, LocalDate today) {
        if (dueDate.isBefore(today)) {
            return ReminderSeverity.ERROR;
        }
        if (dueDate.equals(today) || dueDate.equals(today.plusDays(1))) {
            return ReminderSeverity.WARNING;
        }
        return ReminderSeverity.INFO;
    }

    private void validate(ReminderDismissRequest request) {
        if (request.getApplicationId() == null) {
            throw new ReminderValidationException("An application is required.");
        }
        if (request.getKind() == null) {
            throw new ReminderValidationException("A reminder kind is required.");
        }
        if (request.getDueDate() == null) {
            throw new ReminderValidationException("A due date is required.");
        }
    }
}
