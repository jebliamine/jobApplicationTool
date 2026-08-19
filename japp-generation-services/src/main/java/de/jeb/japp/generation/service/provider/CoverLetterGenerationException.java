package de.jeb.japp.generation.service.provider;

/**
 * Thrown by a {@link CoverLetterGenerationProvider} when it cannot produce a
 * result. Caught by GenerationRequestService and turned into a FAILED
 * GenerationRequest with this message stored as the error — never surfaced
 * to the REST layer as its own exception type.
 */
public class CoverLetterGenerationException extends RuntimeException {
    public CoverLetterGenerationException(String message) {
        super(message);
    }
}
