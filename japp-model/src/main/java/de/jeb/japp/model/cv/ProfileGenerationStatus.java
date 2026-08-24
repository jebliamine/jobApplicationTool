package de.jeb.japp.model.cv;

/**
 * Whether AI-based structured profile extraction (name/summary/experience —
 * distinct from the raw-text {@link ExtractionStatus}) has been attempted
 * for a {@link CVDocument}, and how it went. Runs synchronously when
 * triggered, so IN_PROGRESS is only ever observed mid-request.
 */
public enum ProfileGenerationStatus {
    NOT_ATTEMPTED,
    IN_PROGRESS,
    COMPLETED,
    FAILED
}
