package de.jeb.japp.rest.job;

/** Thrown when a Company or Job exists but the requester (not owner, not admin) may not access it. */
public class JobsAccessDeniedException extends RuntimeException {
    public JobsAccessDeniedException(String message) {
        super(message);
    }
}
