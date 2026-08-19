package de.jeb.japp.rest.admin;

import de.jeb.japp.commons.exceptions.user.DuplicateEmailException;
import de.jeb.japp.commons.exceptions.user.InvalidProfileUpdateException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Scoped to the two exceptions UserProfileService throws — deliberately not
 * a catch-all, so existing behavior elsewhere (e.g. AuthController) is
 * unaffected.
 */
@RestControllerAdvice(assignableTypes = UserController.class)
public class UserProfileExceptionHandler {

    @ExceptionHandler(InvalidProfileUpdateException.class)
    public ResponseEntity<Map<String, String>> handleInvalid(InvalidProfileUpdateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<Map<String, String>> handleDuplicate(DuplicateEmailException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", ex.getMessage()));
    }
}
