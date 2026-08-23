package de.jeb.japp.commons.exceptions.cv;

/** Thrown when a single content-extraction strategy (Tika, PDFBox, POI, OCR) fails on a malformed/corrupt document. */
public class CVExtractionException extends RuntimeException {
    public CVExtractionException(String message, Throwable cause) {
        super(message, cause);
    }
}
