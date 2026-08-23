package de.jeb.japp.cv.service.parser.validator;

public interface DocumentValidator {
    void validate(String filename, String contentType, long size);
}
