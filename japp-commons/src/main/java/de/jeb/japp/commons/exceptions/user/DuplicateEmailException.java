package de.jeb.japp.commons.exceptions.user;

/** Thrown when a PUT /api/v1/users/me request tries to change the email to one already in use. */
public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String message) {
        super(message);
    }
}
