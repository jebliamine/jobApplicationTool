package de.jeb.japp.rest.admin;

import de.jeb.japp.commons.exceptions.user.InvalidUserTokenException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Scoped to AuthController. Deliberately does NOT touch login()/register() — those still throw
 * bare RuntimeException and surface as an unstructured 500 today (a known, documented quirk the
 * frontend's describeAuthError already relies on); only the new password-reset/email-verification
 * endpoints get a properly structured error response here.
 */
@RestControllerAdvice(assignableTypes = AuthController.class)
public class AuthExceptionHandler {

    @ExceptionHandler(InvalidUserTokenException.class)
    public ResponseEntity<Map<String, String>> handleInvalidToken(InvalidUserTokenException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
    }
}
