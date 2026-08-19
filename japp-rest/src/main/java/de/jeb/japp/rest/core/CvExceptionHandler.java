package de.jeb.japp.rest.core;

import de.jeb.japp.commons.exceptions.cv.CVAccessDeniedException;
import de.jeb.japp.commons.exceptions.cv.CVNotFoundException;
import de.jeb.japp.commons.exceptions.cv.CVStorageException;
import de.jeb.japp.commons.exceptions.cv.CVValidationException;
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

    @ExceptionHandler(CVValidationException.class)
    public ResponseEntity<Map<String, String>> handleValidation(CVValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(CVNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(CVNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(CVAccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(CVAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(CVStorageException.class)
    public ResponseEntity<Map<String, String>> handleStorageFailure(CVStorageException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "We could not process this CV file. Please try again."));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> handleTooLarge(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "File exceeds the maximum upload size."));
    }
}
