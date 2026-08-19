package de.jeb.japp.commons.exceptions.application;

/** Thrown when an Application doesn't exist. */
public class ApplicationNotFoundException extends RuntimeException {
    public ApplicationNotFoundException(String message) {
        super(message);
    }
}
