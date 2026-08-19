package de.jeb.japp.rest.job;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Scoped to CompanyController/JobController — deliberately not a catch-all,
 * so existing behavior elsewhere (e.g. AuthController) is unaffected.
 */
@RestControllerAdvice(assignableTypes = {CompanyController.class, JobController.class})
public class JobsExceptionHandler {

    @ExceptionHandler(JobsValidationException.class)
    public ResponseEntity<Map<String, String>> handleValidation(JobsValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(JobsNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(JobsNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(JobsAccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(JobsAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", ex.getMessage()));
    }
}
