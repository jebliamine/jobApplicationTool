package de.jeb.japp.commons.exceptions.cv;

/** Thrown when a CV exists but the requester (not owner, not admin) may not access it. */
public class CVAccessDeniedException extends RuntimeException {
    public CVAccessDeniedException(String message) {
        super(message);
    }
}
