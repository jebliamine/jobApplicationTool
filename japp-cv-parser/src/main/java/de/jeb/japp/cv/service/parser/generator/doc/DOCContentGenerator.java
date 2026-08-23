package de.jeb.japp.cv.service.parser.generator.doc;

import de.jeb.japp.commons.exceptions.cv.CVExtractionException;
import de.jeb.japp.cv.service.parser.generator.ContentGenerator;
import de.jeb.japp.cv.service.parser.generator.DocumentType;
import de.jeb.japp.cv.service.parser.generator.ExtractedDocument;
import de.jeb.japp.cv.service.parser.generator.ExtractionMethod;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/** Legacy DOC-specific fallback, used when Tika's extraction is insufficient. */
@Component
public class DOCContentGenerator implements ContentGenerator {

    @Override
    public boolean supports(DocumentType type) {
        return type == DocumentType.DOC;
    }

    @Override
    public ExtractedDocument extract(InputStream stream, String filename, DocumentType type) {
        try (HWPFDocument document = new HWPFDocument(stream);
             WordExtractor extractor = new WordExtractor(document)) {
            String text = extractor.getText();

            return new ExtractedDocument(
                    text,
                    "application/msword",
                    filename,
                    text.length(),
                    0,
                    DocumentType.DOC,
                    ExtractionMethod.POI_HWPF,
                    null
            );
        } catch (Exception e) {
            throw new CVExtractionException("POI DOC extraction failed for " + filename, e);
        }
    }
}
