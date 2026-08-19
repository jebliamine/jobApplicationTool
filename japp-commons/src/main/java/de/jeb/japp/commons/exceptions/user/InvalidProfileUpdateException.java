package de.jeb.japp.commons.exceptions.user;

/** Thrown when a PUT /api/v1/users/me request contains invalid fullName/email input. */
public class InvalidProfileUpdateException extends RuntimeException {
    public InvalidProfileUpdateException(String message) {
        super(message);
    }
}
