package de.jeb.japp.commons.exceptions.user;

/**
 * Thrown when a user doesn't exist. Not wired up anywhere yet — auth/profile
 * still use their existing exception types (DuplicateEmailException,
 * InvalidProfileUpdateException); migrating those is a separate change.
 */
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
