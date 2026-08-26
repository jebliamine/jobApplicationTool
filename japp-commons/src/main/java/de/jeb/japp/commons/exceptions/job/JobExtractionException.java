package de.jeb.japp.commons.exceptions.job;

/**
 * Thrown when a JobExtractionAdapter cannot produce a result (provider auth/rate-limit/timeout,
 * unparsable response, adapter not found). Unlike CvProfileGenerationException, there is no
 * persisted entity to attach a FAILED status to here — extraction is a one-shot, unpersisted
 * preview — so this surfaces directly to the REST layer as an error response.
 */
public class JobExtractionException extends RuntimeException {
    public JobExtractionException(String message) {
        super(message);
    }
}
