package de.jeb.japp.rest.reminder;

import de.jeb.japp.commons.exceptions.application.ApplicationAccessDeniedException;
import de.jeb.japp.commons.exceptions.application.ApplicationNotFoundException;
import de.jeb.japp.commons.exceptions.reminder.ReminderValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Scoped to ReminderController — deliberately not a catch-all. Also handles Application
 * exceptions since dismissing a reminder validates ownership of the referenced Application
 * (see ReminderService#dismiss), same reasoning as ApplicationExceptionHandler absorbing
 * Job/CV/CoverLetter exceptions for its own flows.
 */
@RestControllerAdvice(assignableTypes = ReminderController.class)
public class ReminderExceptionHandler {

    @ExceptionHandler(ReminderValidationException.class)
    public ResponseEntity<Map<String, String>> handleValidation(ReminderValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(ApplicationNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(ApplicationNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(ApplicationAccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(ApplicationAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", ex.getMessage()));
    }
}
