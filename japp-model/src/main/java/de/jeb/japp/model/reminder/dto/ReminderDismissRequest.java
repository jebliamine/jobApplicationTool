package de.jeb.japp.model.reminder.dto;

import de.jeb.japp.model.reminder.ReminderKind;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Request body for POST /api/v1/reminders/dismiss. {@code snoozedUntil} is optional — omit it
 * (or send null) to dismiss permanently for this exact due date; set it to resurface the
 * reminder automatically once that date arrives.
 */
public class ReminderDismissRequest {
    private UUID applicationId;
    private ReminderKind kind;
    private LocalDate dueDate;
    private LocalDate snoozedUntil;

    public ReminderDismissRequest() {
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(UUID applicationId) {
        this.applicationId = applicationId;
    }

    public ReminderKind getKind() {
        return kind;
    }

    public void setKind(ReminderKind kind) {
        this.kind = kind;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getSnoozedUntil() {
        return snoozedUntil;
    }

    public void setSnoozedUntil(LocalDate snoozedUntil) {
        this.snoozedUntil = snoozedUntil;
    }
}
