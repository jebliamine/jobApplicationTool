package de.jeb.japp.commons.exceptions.tag;

/** Thrown when a Tag doesn't exist. */
public class TagNotFoundException extends RuntimeException {
    public TagNotFoundException(String message) {
        super(message);
    }
}
