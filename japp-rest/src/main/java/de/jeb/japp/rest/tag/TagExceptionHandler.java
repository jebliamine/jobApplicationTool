package de.jeb.japp.rest.tag;

import de.jeb.japp.commons.exceptions.tag.TagAccessDeniedException;
import de.jeb.japp.commons.exceptions.tag.TagNotFoundException;
import de.jeb.japp.commons.exceptions.tag.TagValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Scoped to TagController — deliberately not a catch-all, so existing
 * behavior elsewhere is unaffected. Tag exceptions thrown from JobController
 * and ApplicationController's own tag-assignment endpoints are instead
 * handled by JobsExceptionHandler and ApplicationExceptionHandler
 * respectively, following each controller's existing per-feature pattern of
 * absorbing the cross-domain exceptions its own flows can throw.
 */
@RestControllerAdvice(assignableTypes = TagController.class)
public class TagExceptionHandler {

    @ExceptionHandler(TagValidationException.class)
    public ResponseEntity<Map<String, String>> handleValidation(TagValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(TagNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(TagNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(TagAccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(TagAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", ex.getMessage()));
    }
}
