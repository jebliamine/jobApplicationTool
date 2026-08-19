package de.jeb.japp.commons.exceptions.generation;

/** Thrown when a GenerationRequest create request fails backend validation, or when generation itself cannot proceed. */
public class GenerationRequestValidationException extends RuntimeException {
    public GenerationRequestValidationException(String message) {
        super(message);
    }
}
