package de.jeb.japp.commons.exceptions.ai;

/** Thrown when an admin AI provider configuration request fails validation. */
public class AiProviderValidationException extends RuntimeException {
    public AiProviderValidationException(String message) {
        super(message);
    }
}
