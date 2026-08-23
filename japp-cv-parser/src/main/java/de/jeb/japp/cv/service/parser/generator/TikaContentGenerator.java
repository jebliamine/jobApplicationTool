package de.jeb.japp.cv.service.parser.generator;

import de.jeb.japp.commons.exceptions.cv.CVExtractionException;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

/** Primary extraction strategy: Apache Tika, auto-detecting the format and delegating to its bundled parsers. */
@Component
public class TikaContentGenerator implements ContentGenerator {

    private final Parser parser = new AutoDetectParser();

    @Override
    public boolean supports(DocumentType type) {
        return type == DocumentType.PDF || type == DocumentType.DOC || type == DocumentType.DOCX;
    }

    @Override
    public ExtractedDocument extract(InputStream stream, String filename, DocumentType type) {
        Metadata metadata = new Metadata();
        BodyContentHandler handler = new BodyContentHandler(-1);

        try {
            parser.parse(stream, handler, metadata, new ParseContext());
        } catch (IOException | SAXException | TikaException | RuntimeException e) {
            throw new CVExtractionException("Tika extraction failed for " + filename, e);
        }

        String text = handler.toString();
        int pageCount = parsePageCount(metadata);

        return new ExtractedDocument(
                text,
                metadata.get(Metadata.CONTENT_TYPE),
                filename,
                text.length(),
                pageCount,
                type,
                ExtractionMethod.TIKA,
                null
        );
    }

    private int parsePageCount(Metadata metadata) {
        String pageCount = metadata.get("xmpTPg:NPages");
        if (pageCount == null) {
            pageCount = metadata.get("meta:page-count");
        }
        if (pageCount == null) {
            return 0;
        }
        try {
            return Integer.parseInt(pageCount);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
