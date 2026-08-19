package de.jeb.japp.rest.application;

import de.jeb.japp.commons.exceptions.application.ApplicationAccessDeniedException;
import de.jeb.japp.commons.exceptions.application.ApplicationNotFoundException;
import de.jeb.japp.commons.exceptions.application.ApplicationValidationException;
import de.jeb.japp.commons.exceptions.coverletter.CoverLetterAccessDeniedException;
import de.jeb.japp.commons.exceptions.coverletter.CoverLetterNotFoundException;
import de.jeb.japp.commons.exceptions.cv.CVAccessDeniedException;
import de.jeb.japp.commons.exceptions.cv.CVNotFoundException;
import de.jeb.japp.commons.exceptions.job.JobAccessDeniedException;
import de.jeb.japp.commons.exceptions.job.JobNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Scoped to ApplicationController — deliberately not a catch-all, so
 * existing behavior elsewhere (e.g. JobController) is unaffected. Also
 * handles Job/CV/CoverLetter exceptions since creating/updating an
 * Application validates ownership of the referenced Job, CVDocument, and CoverLetter.
 */
@RestControllerAdvice(assignableTypes = ApplicationController.class)
public class ApplicationExceptionHandler {

    @ExceptionHandler(ApplicationValidationException.class)
    public ResponseEntity<Map<String, String>> handleValidation(ApplicationValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler({
            ApplicationNotFoundException.class,
            JobNotFoundException.class,
            CVNotFoundException.class,
            CoverLetterNotFoundException.class
    })
    public ResponseEntity<Map<String, String>> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler({
            ApplicationAccessDeniedException.class,
            JobAccessDeniedException.class,
            CVAccessDeniedException.class,
            CoverLetterAccessDeniedException.class
    })
    public ResponseEntity<Map<String, String>> handleAccessDenied(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", ex.getMessage()));
    }
}
