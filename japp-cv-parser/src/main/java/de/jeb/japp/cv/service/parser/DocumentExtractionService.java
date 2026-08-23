package de.jeb.japp.cv.service.parser;

import de.jeb.japp.cv.service.parser.normalizer.NormalizedDocument;
import org.springframework.core.io.Resource;

public interface DocumentExtractionService {
    NormalizedDocument process(Resource resource, String filename, String contentType);
}
