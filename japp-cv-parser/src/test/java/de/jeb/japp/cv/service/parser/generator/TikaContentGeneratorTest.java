package de.jeb.japp.cv.service.parser.generator;

import de.jeb.japp.cv.service.parser.support.FixtureFiles;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class TikaContentGeneratorTest {

    private final TikaContentGenerator generator = new TikaContentGenerator();

    @Test
    void supportsAllThreeDocumentTypes() {
        assertThat(generator.supports(DocumentType.PDF)).isTrue();
        assertThat(generator.supports(DocumentType.DOCX)).isTrue();
        assertThat(generator.supports(DocumentType.DOC)).isTrue();
    }

    @Test
    void extractsTextFromAGoodPdf() throws IOException {
        byte[] pdf = FixtureFiles.goodPdf();

        ExtractedDocument result = generator.extract(new ByteArrayInputStream(pdf), "cv.pdf", DocumentType.PDF);

        assertThat(result.text()).contains("John Doe", "Senior Software Engineer", "Acme Corp");
        assertThat(result.extractionMethod()).isEqualTo(ExtractionMethod.TIKA);
    }

    @Test
    void extractsTextFromAGoodDocx() throws IOException {
        byte[] docx = FixtureFiles.goodDocx();

        ExtractedDocument result = generator.extract(new ByteArrayInputStream(docx), "cv.docx", DocumentType.DOCX);

        assertThat(result.text()).contains("John Doe", "Senior Software Engineer", "Acme Corp");
    }

    @Test
    void garbageInputDoesNotCrashAndProducesNoMeaningfulText() {
        byte[] garbage = "not a real document, just some plain garbage bytes".getBytes();

        ExtractedDocument result = generator.extract(new ByteArrayInputStream(garbage), "cv.docx", DocumentType.DOCX);

        // Tika auto-detects this as plain text rather than throwing - this is exactly why the
        // fallback chain exists: quality checking, not just exception handling, gates progression.
        assertThat(result.text()).doesNotContain("John Doe");
    }
}
