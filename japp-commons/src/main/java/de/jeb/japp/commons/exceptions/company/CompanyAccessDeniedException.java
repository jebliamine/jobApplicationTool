package de.jeb.japp.commons.exceptions.company;

/** Thrown when a Company exists but the requester (not owner, not admin) may not access it. */
public class CompanyAccessDeniedException extends RuntimeException {
    public CompanyAccessDeniedException(String message) {
        super(message);
    }
}
