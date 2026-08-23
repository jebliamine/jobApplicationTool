package de.jeb.japp.model.cv;

/** Whether CV text extraction has been attempted for a {@link CVDocument}, and how it went. Extraction runs synchronously at upload time, so there is no in-progress state. */
public enum ExtractionStatus {
    NOT_ATTEMPTED,
    COMPLETED,
    FAILED
}
