package de.jeb.japp.commons.exceptions.cv;

/** Thrown when a CV doesn't exist, or the requester doesn't own it (and isn't an admin). */
public class CVNotFoundException extends RuntimeException {
    public CVNotFoundException(String message) {
        super(message);
    }
}
