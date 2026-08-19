package de.jeb.japp.rest.generation;

import de.jeb.japp.commons.exceptions.cv.CVAccessDeniedException;
import de.jeb.japp.commons.exceptions.cv.CVNotFoundException;
import de.jeb.japp.commons.exceptions.generation.GenerationRequestAccessDeniedException;
import de.jeb.japp.commons.exceptions.generation.GenerationRequestNotFoundException;
import de.jeb.japp.commons.exceptions.generation.GenerationRequestValidationException;
import de.jeb.japp.commons.exceptions.job.JobAccessDeniedException;
import de.jeb.japp.commons.exceptions.job.JobNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Scoped to GenerationRequestController — deliberately not a catch-all.
 * Also handles Job/CV exceptions since creating a GenerationRequest
 * validates ownership of the referenced Job and CVDocument.
 */
@RestControllerAdvice(assignableTypes = GenerationRequestController.class)
public class GenerationRequestExceptionHandler {

    @ExceptionHandler(GenerationRequestValidationException.class)
    public ResponseEntity<Map<String, String>> handleValidation(GenerationRequestValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler({GenerationRequestNotFoundException.class, JobNotFoundException.class, CVNotFoundException.class})
    public ResponseEntity<Map<String, String>> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler({
            GenerationRequestAccessDeniedException.class,
            JobAccessDeniedException.class,
            CVAccessDeniedException.class
    })
    public ResponseEntity<Map<String, String>> handleAccessDenied(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", ex.getMessage()));
    }
}
