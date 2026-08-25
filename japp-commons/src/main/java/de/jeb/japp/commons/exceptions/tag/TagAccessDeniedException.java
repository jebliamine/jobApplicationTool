package de.jeb.japp.commons.exceptions.tag;

/** Thrown when a requester tries to use or modify a Tag they don't own. */
public class TagAccessDeniedException extends RuntimeException {
    public TagAccessDeniedException(String message) {
        super(message);
    }
}
