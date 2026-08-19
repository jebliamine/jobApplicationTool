package de.jeb.japp.rest.job;

import de.jeb.japp.commons.exceptions.company.CompanyAccessDeniedException;
import de.jeb.japp.commons.exceptions.company.CompanyNotFoundException;
import de.jeb.japp.commons.exceptions.company.CompanyValidationException;
import de.jeb.japp.commons.exceptions.job.JobAccessDeniedException;
import de.jeb.japp.commons.exceptions.job.JobNotFoundException;
import de.jeb.japp.commons.exceptions.job.JobValidationException;
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

    @ExceptionHandler({JobValidationException.class, CompanyValidationException.class})
    public ResponseEntity<Map<String, String>> handleValidation(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler({JobNotFoundException.class, CompanyNotFoundException.class})
    public ResponseEntity<Map<String, String>> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler({JobAccessDeniedException.class, CompanyAccessDeniedException.class})
    public ResponseEntity<Map<String, String>> handleAccessDenied(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", ex.getMessage()));
    }
}
