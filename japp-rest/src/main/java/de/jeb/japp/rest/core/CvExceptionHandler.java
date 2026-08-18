package de.jeb.japp.rest.core;

import de.jeb.japp.cv.service.CvAccessDeniedException;
import de.jeb.japp.cv.service.CvNotFoundException;
import de.jeb.japp.cv.service.CvValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Map;

/**
 * Scoped to CvController — deliberately not a catch-all, so existing
 * behavior elsewhere (e.g. AuthController) is unaffected.
 */
@RestControllerAdvice(assignableTypes = CvController.class)
public class CvExceptionHandler {

    @ExceptionHandler(CvValidationException.class)
    public ResponseEntity<Map<String, String>> handleValidation(CvValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(CvNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(CvNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(CvAccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(CvAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> handleTooLarge(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "File exceeds the maximum upload size."));
    }
}
