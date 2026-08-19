package de.jeb.japp.commons.exceptions.ai;

/** Thrown when a non-ADMIN requester attempts to access AI provider configuration. */
public class AiProviderAccessDeniedException extends RuntimeException {
    public AiProviderAccessDeniedException(String message) {
        super(message);
    }
}
