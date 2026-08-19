package de.jeb.japp.commons.exceptions.application;

/** Thrown when an Application exists but the requester (not owner, not admin) may not access it. */
public class ApplicationAccessDeniedException extends RuntimeException {
    public ApplicationAccessDeniedException(String message) {
        super(message);
    }
}
