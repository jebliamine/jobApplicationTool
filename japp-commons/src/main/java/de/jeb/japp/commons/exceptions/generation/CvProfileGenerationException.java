package de.jeb.japp.commons.exceptions.generation;

/**
 * Thrown by a CvProfileExtractionAdapter when it cannot produce a result.
 * Caught by CvProfileExtractionService and turned into a FAILED CVProfile
 * with this message stored as the error — never surfaced to the REST layer
 * as its own exception type (same pattern as CoverLetterGenerationException).
 */
public class CvProfileGenerationException extends RuntimeException {
    public CvProfileGenerationException(String message) {
        super(message);
    }
}
