package de.jeb.japp.cv.service;

/** Thrown when a CV doesn't exist, or the requester doesn't own it (and isn't an admin). */
public class CvNotFoundException extends RuntimeException {
    public CvNotFoundException(String message) {
        super(message);
    }
}
