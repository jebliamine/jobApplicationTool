package de.jeb.japp.commons.exceptions.user;

/**
 * Thrown when a user create/update request fails backend validation. Not
 * wired up anywhere yet — see UserNotFoundException.
 */
public class UserValidationException extends RuntimeException {
    public UserValidationException(String message) {
        super(message);
    }
}
