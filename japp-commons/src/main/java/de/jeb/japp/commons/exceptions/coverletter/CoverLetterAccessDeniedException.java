package de.jeb.japp.commons.exceptions.coverletter;

/** Thrown when a CoverLetter exists but the requester (not owner, not admin) may not access it. */
public class CoverLetterAccessDeniedException extends RuntimeException {
    public CoverLetterAccessDeniedException(String message) {
        super(message);
    }
}
