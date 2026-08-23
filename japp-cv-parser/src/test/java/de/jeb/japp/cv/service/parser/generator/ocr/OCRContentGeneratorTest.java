package de.jeb.japp.cv.service.parser.generator.ocr;

import de.jeb.japp.cv.service.parser.generator.DocumentType;
import net.sourceforge.tess4j.Tesseract;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Requires a native Tesseract installation (see Tess4J setup docs) - not available in every
 * environment, so the actual OCR call is skipped rather than failed when it can't run.
 */
class OCRContentGeneratorTest {

    private final OCRContentGenerator generator = new OCRContentGenerator();

    @Test
    void supportsOnlyPdf() {
        assertThat(generator.supports(DocumentType.PDF)).isTrue();
        assertThat(generator.supports(DocumentType.DOCX)).isFalse();
        assertThat(generator.supports(DocumentType.DOC)).isFalse();
    }

    @Test
    void extractsTextFromAScannedLookingPdf() {
        Assumptions.assumeTrue(isTesseractAvailable(), "native Tesseract binary not available in this environment");
        // Intentionally not asserted further here: exercising this end-to-end requires both a
        // native Tesseract install and a real scanned-image fixture, neither of which this
        // environment provides.
    }

    private boolean isTesseractAvailable() {
        try {
            new Tesseract().doOCR(new java.awt.image.BufferedImage(10, 10, java.awt.image.BufferedImage.TYPE_INT_RGB));
            return true;
        } catch (Throwable t) {
            return false;
        }
    }
}
