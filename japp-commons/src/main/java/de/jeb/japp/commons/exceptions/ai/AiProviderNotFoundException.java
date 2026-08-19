package de.jeb.japp.commons.exceptions.ai;

/** Thrown when an unknown/unsupported provider id is referenced. */
public class AiProviderNotFoundException extends RuntimeException {
    public AiProviderNotFoundException(String message) {
        super(message);
    }
}
