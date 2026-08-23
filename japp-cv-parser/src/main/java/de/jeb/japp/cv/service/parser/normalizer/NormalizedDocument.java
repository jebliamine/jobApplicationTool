package de.jeb.japp.cv.service.parser.normalizer;

public record NormalizedDocument(
        String text,
        String filename,
        String contentType
) {
}
