package de.jeb.japp.commons.exceptions.company;

/** Thrown when a Company create/update/delete request fails backend validation. */
public class CompanyValidationException extends RuntimeException {
    public CompanyValidationException(String message) {
        super(message);
    }
}
