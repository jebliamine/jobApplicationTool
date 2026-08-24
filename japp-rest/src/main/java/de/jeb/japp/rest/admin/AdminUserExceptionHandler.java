package de.jeb.japp.rest.admin;

import de.jeb.japp.commons.exceptions.user.UserAccessDeniedException;
import de.jeb.japp.commons.exceptions.user.UserNotFoundException;
import de.jeb.japp.commons.exceptions.user.UserValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/** Scoped to AdminUserController — deliberately not a catch-all. */
@RestControllerAdvice(assignableTypes = AdminUserController.class)
public class AdminUserExceptionHandler {

    @ExceptionHandler(UserValidationException.class)
    public ResponseEntity<Map<String, String>> handleValidation(UserValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(UserAccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(UserAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", ex.getMessage()));
    }
}
