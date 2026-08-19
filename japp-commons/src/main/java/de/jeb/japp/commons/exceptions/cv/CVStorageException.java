package de.jeb.japp.commons.exceptions.cv;

/**
 * Thrown when the physical file storage layer fails during a CV
 * upload/delete (e.g. an I/O error writing or removing the file) — distinct
 * from CVNotFoundException, which covers a file that's already missing.
 */
public class CVStorageException extends RuntimeException {
    public CVStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
