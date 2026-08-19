package de.jeb.japp.commons.exceptions.user;

/**
 * Thrown when a user exists but the requester may not access/modify it. Not
 * wired up anywhere yet — see UserNotFoundException.
 */
public class UserAccessDeniedException extends RuntimeException {
    public UserAccessDeniedException(String message) {
        super(message);
    }
}
