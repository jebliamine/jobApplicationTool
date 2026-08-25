package de.jeb.japp.commons.exceptions.reminder;

/** Thrown for a malformed dismiss request (missing application/kind/due date). */
public class ReminderValidationException extends RuntimeException {
    public ReminderValidationException(String message) {
        super(message);
    }
}
