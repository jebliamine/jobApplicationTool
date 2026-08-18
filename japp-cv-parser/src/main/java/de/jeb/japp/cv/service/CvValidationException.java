package de.jeb.japp.cv.service;

/** Thrown when a CV upload request fails backend validation. */
public class CvValidationException extends RuntimeException {
    public CvValidationException(String message) {
        super(message);
    }
}
