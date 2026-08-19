package de.jeb.japp.commons.exceptions.application;

/** Thrown when an Application create/update request fails backend validation. */
public class ApplicationValidationException extends RuntimeException {
    public ApplicationValidationException(String message) {
        super(message);
    }
}
