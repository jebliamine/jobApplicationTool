package de.jeb.japp.commons.exceptions.tag;

/** Thrown for a blank tag name or a duplicate name within the same owner's tag list. */
public class TagValidationException extends RuntimeException {
    public TagValidationException(String message) {
        super(message);
    }
}
