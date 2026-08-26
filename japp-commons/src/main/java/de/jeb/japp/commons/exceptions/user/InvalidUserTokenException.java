package de.jeb.japp.commons.exceptions.user;

/**
 * Thrown when a password-reset or email-verification token is unknown, expired, or already used.
 * Deliberately generic across both flows — the failure semantics (and the message shown to the
 * user) are identical either way.
 */
public class InvalidUserTokenException extends RuntimeException {
    public InvalidUserTokenException(String message) {
        super(message);
    }
}
