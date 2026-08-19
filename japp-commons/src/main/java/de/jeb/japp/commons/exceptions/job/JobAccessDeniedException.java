package de.jeb.japp.commons.exceptions.job;

/** Thrown when a Job exists but the requester (not owner, not admin) may not access it. */
public class JobAccessDeniedException extends RuntimeException {
    public JobAccessDeniedException(String message) {
        super(message);
    }
}
