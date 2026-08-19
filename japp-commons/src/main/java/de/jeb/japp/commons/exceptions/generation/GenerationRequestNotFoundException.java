package de.jeb.japp.commons.exceptions.generation;

/** Thrown when a GenerationRequest doesn't exist. */
public class GenerationRequestNotFoundException extends RuntimeException {
    public GenerationRequestNotFoundException(String message) {
        super(message);
    }
}
