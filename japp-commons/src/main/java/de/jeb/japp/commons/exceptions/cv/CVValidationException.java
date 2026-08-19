package de.jeb.japp.commons.exceptions.cv;

/** Thrown when a CV upload request fails backend validation. */
public class CVValidationException extends RuntimeException {
    public CVValidationException(String message) {
        super(message);
    }
}
