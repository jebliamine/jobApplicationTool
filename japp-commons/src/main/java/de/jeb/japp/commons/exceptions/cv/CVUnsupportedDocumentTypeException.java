package de.jeb.japp.commons.exceptions.cv;

/** Thrown when a document's format cannot be resolved to a supported document type, or no generator supports it. */
public class CVUnsupportedDocumentTypeException extends RuntimeException {
    public CVUnsupportedDocumentTypeException(String message) {
        super(message);
    }
}
