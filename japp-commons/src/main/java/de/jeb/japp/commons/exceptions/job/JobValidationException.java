package de.jeb.japp.commons.exceptions.job;

/** Thrown when a Job create/update request fails backend validation. */
public class JobValidationException extends RuntimeException {
    public JobValidationException(String message) {
        super(message);
    }
}
