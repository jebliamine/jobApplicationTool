package de.jeb.japp.rest.job;

/** Thrown when a Company or Job create/update/delete request fails backend validation. */
public class JobsValidationException extends RuntimeException {
    public JobsValidationException(String message) {
        super(message);
    }
}
