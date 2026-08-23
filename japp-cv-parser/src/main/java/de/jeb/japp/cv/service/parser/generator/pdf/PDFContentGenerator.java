package de.jeb.japp.cv.service.parser.generator.pdf;

import de.jeb.japp.commons.exceptions.cv.CVExtractionException;
import de.jeb.japp.cv.service.parser.generator.ContentGenerator;
import de.jeb.japp.cv.service.parser.generator.DocumentType;
import de.jeb.japp.cv.service.parser.generator.ExtractedDocument;
import de.jeb.japp.cv.service.parser.generator.ExtractionMethod;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/** PDF-specific fallback, used when Tika's extraction is insufficient. */
@Component
public class PDFContentGenerator implements ContentGenerator {

    @Override
    public boolean supports(DocumentType type) {
        return type == DocumentType.PDF;
    }

    @Override
    public ExtractedDocument extract(InputStream stream, String filename, DocumentType type) {
        try (PDDocument document = Loader.loadPDF(stream.readAllBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            return new ExtractedDocument(
                    text,
                    "application/pdf",
                    filename,
                    text.length(),
                    document.getNumberOfPages(),
                    DocumentType.PDF,
                    ExtractionMethod.PDFBOX,
                    null
            );
        } catch (Exception e) {
            throw new CVExtractionException("PDFBox extraction failed for " + filename, e);
        }
    }
}
