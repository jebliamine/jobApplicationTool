package de.jeb.japp.model.generation;

/**
 * The AI provider a cover-letter generation request should use. Selected by
 * the client (or defaulted to PLACEHOLDER) at request time; distinct from
 * {@code GenerationRequest.provider}, which stores the resolved provider's
 * name as plain text for display (existing rows predate this enum and store
 * the lowercase literal "placeholder" — those are historical and are never
 * parsed back into this enum, so they remain valid as-is).
 */
public enum GenerationProvider {
    PLACEHOLDER,
    GEMINI
}
