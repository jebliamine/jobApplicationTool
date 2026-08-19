package de.jeb.japp.commons.exceptions.job;

/** Thrown when a Job doesn't exist. */
public class JobNotFoundException extends RuntimeException {
    public JobNotFoundException(String message) {
        super(message);
    }
}
