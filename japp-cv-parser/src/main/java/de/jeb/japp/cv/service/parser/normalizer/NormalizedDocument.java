package de.jeb.japp.cv.service.parser.normalizer;

import de.jeb.japp.cv.service.parser.generator.ExtractionMethod;
import de.jeb.japp.cv.service.parser.generator.checker.ExtractionQuality;

public record NormalizedDocument(
        String text,
        String filename,
        String contentType,
        ExtractionQuality quality,
        ExtractionMethod extractionMethod
) {
}
