package de.jeb.japp.commons.exceptions.coverletter;

/** Thrown when a CoverLetter doesn't exist. */
public class CoverLetterNotFoundException extends RuntimeException {
    public CoverLetterNotFoundException(String message) {
        super(message);
    }
}
