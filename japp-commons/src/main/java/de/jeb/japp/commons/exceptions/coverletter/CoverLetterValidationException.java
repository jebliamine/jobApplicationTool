package de.jeb.japp.commons.exceptions.coverletter;

/** Thrown when a CoverLetter update request fails backend validation. */
public class CoverLetterValidationException extends RuntimeException {
    public CoverLetterValidationException(String message) {
        super(message);
    }
}
