package de.jeb.japp.cv.service.parser.generator;

import java.io.InputStream;

public interface ContentGenerator {

    boolean supports(DocumentType type);

    ExtractedDocument extract(InputStream stream, String filename, DocumentType type);
}
