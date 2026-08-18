package de.jeb.japp.cv.service;

/** Thrown when a CV exists but the requester (not owner, not admin) may not access it. */
public class CvAccessDeniedException extends RuntimeException {
    public CvAccessDeniedException(String message) {
        super(message);
    }
}
