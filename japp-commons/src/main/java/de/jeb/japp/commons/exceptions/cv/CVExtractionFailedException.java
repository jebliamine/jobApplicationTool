package de.jeb.japp.commons.exceptions.cv;

/** Thrown when every applicable extraction strategy (primary, format fallback, OCR) has been exhausted and still produced no usable text. */
public class CVExtractionFailedException extends RuntimeException {
    public CVExtractionFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
