package de.jeb.japp.rest.coverletter;

import de.jeb.japp.commons.exceptions.coverletter.CoverLetterAccessDeniedException;
import de.jeb.japp.commons.exceptions.coverletter.CoverLetterNotFoundException;
import de.jeb.japp.commons.exceptions.coverletter.CoverLetterValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/** Scoped to CoverLetterController — deliberately not a catch-all. */
@RestControllerAdvice(assignableTypes = CoverLetterController.class)
public class CoverLetterExceptionHandler {

    @ExceptionHandler(CoverLetterValidationException.class)
    public ResponseEntity<Map<String, String>> handleValidation(CoverLetterValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(CoverLetterNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(CoverLetterNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(CoverLetterAccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(CoverLetterAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", ex.getMessage()));
    }
}
