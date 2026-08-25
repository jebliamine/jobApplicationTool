package de.jeb.japp.model.reminder;

/** How urgent a reminder is, computed from how close/overdue its due date is — never persisted. */
public enum ReminderSeverity {
    ERROR,
    WARNING,
    INFO
}
