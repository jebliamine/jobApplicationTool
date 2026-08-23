package de.jeb.japp.cv.service.parser.normalizer;

import de.jeb.japp.cv.service.parser.generator.ExtractedDocument;

public interface DocumentNormalizer {
    NormalizedDocument normalize(ExtractedDocument document);
}
