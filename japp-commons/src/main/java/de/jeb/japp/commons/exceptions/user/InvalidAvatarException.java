package de.jeb.japp.commons.exceptions.user;

/** Thrown when a POST /api/v1/users/me/avatar upload fails validation (type/size) or storage. */
public class InvalidAvatarException extends RuntimeException {
    public InvalidAvatarException(String message) {
        super(message);
    }
}
