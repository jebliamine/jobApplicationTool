package de.jeb.japp.cv.service.parser.generator.docx;

import de.jeb.japp.commons.exceptions.cv.CVExtractionException;
import de.jeb.japp.cv.service.parser.generator.ContentGenerator;
import de.jeb.japp.cv.service.parser.generator.DocumentType;
import de.jeb.japp.cv.service.parser.generator.ExtractedDocument;
import de.jeb.japp.cv.service.parser.generator.ExtractionMethod;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/** DOCX-specific fallback, used when Tika's extraction is insufficient. */
@Component
public class DOCXContentGenerator implements ContentGenerator {

    @Override
    public boolean supports(DocumentType type) {
        return type == DocumentType.DOCX;
    }

    @Override
    public ExtractedDocument extract(InputStream stream, String filename, DocumentType type) {
        try (XWPFDocument document = new XWPFDocument(stream);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            String text = extractor.getText();

            return new ExtractedDocument(
                    text,
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    filename,
                    text.length(),
                    0,
                    DocumentType.DOCX,
                    ExtractionMethod.POI_DOCX,
                    null
            );
        } catch (Exception e) {
            throw new CVExtractionException("POI DOCX extraction failed for " + filename, e);
        }
    }
}
