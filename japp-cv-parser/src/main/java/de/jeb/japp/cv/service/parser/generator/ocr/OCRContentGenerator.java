package de.jeb.japp.cv.service.parser.generator.ocr;

import de.jeb.japp.commons.exceptions.cv.CVExtractionException;
import de.jeb.japp.cv.service.parser.generator.ContentGenerator;
import de.jeb.japp.cv.service.parser.generator.DocumentType;
import de.jeb.japp.cv.service.parser.generator.ExtractedDocument;
import de.jeb.japp.cv.service.parser.generator.ExtractionMethod;
import net.sourceforge.tess4j.Tesseract;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.io.InputStream;

/**
 * Final fallback for scanned/image-based PDFs: rasterizes each page and runs Tesseract OCR over it.
 * Scoped to PDF only — DOC/DOCX are not scanned-image formats in practice.
 */
@Component
public class OCRContentGenerator implements ContentGenerator {

    private static final int RENDER_DPI = 300;
    private static final int MAX_PAGES = 10;

    @Override
    public boolean supports(DocumentType type) {
        return type == DocumentType.PDF;
    }

    @Override
    public ExtractedDocument extract(InputStream stream, String filename, DocumentType type) {
        Tesseract tesseract = new Tesseract();
        StringBuilder text = new StringBuilder();

        try (PDDocument document = Loader.loadPDF(stream.readAllBytes())) {
            PDFRenderer renderer = new PDFRenderer(document);
            int pageCount = document.getNumberOfPages();
            int pagesToProcess = Math.min(pageCount, MAX_PAGES);

            for (int page = 0; page < pagesToProcess; page++) {
                BufferedImage image = renderer.renderImageWithDPI(page, RENDER_DPI);
                text.append(tesseract.doOCR(image)).append('\n');
            }

            String extractedText = text.toString();
            return new ExtractedDocument(
                    extractedText,
                    "application/pdf",
                    filename,
                    extractedText.length(),
                    pageCount,
                    DocumentType.PDF,
                    ExtractionMethod.OCR,
                    null
            );
        } catch (Exception e) {
            throw new CVExtractionException("OCR extraction failed for " + filename, e);
        }
    }
}
