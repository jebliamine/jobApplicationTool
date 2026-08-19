package de.jeb.japp.rest.ai;

import de.jeb.japp.commons.exceptions.ai.AiProviderAccessDeniedException;
import de.jeb.japp.commons.exceptions.ai.AiProviderNotFoundException;
import de.jeb.japp.commons.exceptions.ai.AiProviderValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/** Scoped to AdminAiProviderController — deliberately not a catch-all. */
@RestControllerAdvice(assignableTypes = AdminAiProviderController.class)
public class AdminAiProviderExceptionHandler {

    @ExceptionHandler(AiProviderValidationException.class)
    public ResponseEntity<Map<String, String>> handleValidation(AiProviderValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(AiProviderNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(AiProviderNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(AiProviderAccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(AiProviderAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", ex.getMessage()));
    }
}
