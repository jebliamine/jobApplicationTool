package de.jeb.japp.commons.exceptions.company;

/** Thrown when a Company doesn't exist. */
public class CompanyNotFoundException extends RuntimeException {
    public CompanyNotFoundException(String message) {
        super(message);
    }
}
