package de.jeb.japp.model.reminder.dto;

import de.jeb.japp.model.reminder.ReminderKind;
import de.jeb.japp.model.reminder.ReminderSeverity;

import java.time.LocalDate;
import java.util.UUID;

/** One computed, not-yet-dismissed reminder — see ReminderService#list. Never persisted as-is. */
public class ReminderResponse {
    private UUID applicationId;
    private String jobTitle;
    private String companyName;
    private ReminderKind kind;
    private LocalDate dueDate;
    private ReminderSeverity severity;

    public ReminderResponse() {
    }

    public ReminderResponse(UUID applicationId, String jobTitle, String companyName, ReminderKind kind, LocalDate dueDate, ReminderSeverity severity) {
        this.applicationId = applicationId;
        this.jobTitle = jobTitle;
        this.companyName = companyName;
        this.kind = kind;
        this.dueDate = dueDate;
        this.severity = severity;
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public String getCompanyName() {
        return companyName;
    }

    public ReminderKind getKind() {
        return kind;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public ReminderSeverity getSeverity() {
        return severity;
    }
}
