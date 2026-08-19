package de.jeb.japp.commons.exceptions.generation;

/** Thrown when a GenerationRequest exists but the requester (not owner, not admin) may not access it. */
public class GenerationRequestAccessDeniedException extends RuntimeException {
    public GenerationRequestAccessDeniedException(String message) {
        super(message);
    }
}
