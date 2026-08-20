package de.jeb.japp.commons.exceptions.user;

/** Thrown when a PUT /api/v1/users/me/password request has an incorrect current password or an invalid new password. */
public class InvalidPasswordChangeException extends RuntimeException {
    public InvalidPasswordChangeException(String message) {
        super(message);
    }
}
