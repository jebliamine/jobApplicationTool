package de.jeb.japp.cv.service.parser.generator;

import java.util.Optional;

public enum DocumentType {
    PDF,
    DOCX,
    DOC;

    public static Optional<DocumentType> fromFilename(String filename) {
        if (filename == null || !filename.contains(".")) {
            return Optional.empty();
        }
        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        return switch (extension) {
            case "pdf" -> Optional.of(PDF);
            case "docx" -> Optional.of(DOCX);
            case "doc" -> Optional.of(DOC);
            default -> Optional.empty();
        };
    }
}
