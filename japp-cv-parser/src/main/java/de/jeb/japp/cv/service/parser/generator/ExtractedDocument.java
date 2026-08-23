package de.jeb.japp.cv.service.parser.generator;

import de.jeb.japp.cv.service.parser.generator.checker.ExtractionQuality;

public record ExtractedDocument(
        String text,
        String contentType,
        String filename,
        long characterCount,
        int pageCount,
        DocumentType documentType,
        ExtractionMethod extractionMethod,
        ExtractionQuality quality
) {
    public ExtractedDocument withQuality(ExtractionQuality newQuality) {
        return new ExtractedDocument(text, contentType, filename, characterCount, pageCount, documentType, extractionMethod, newQuality);
    }
}
