package de.jeb.japp.cv.service.parser.generator.pdf;

import de.jeb.japp.commons.exceptions.cv.CVExtractionException;
import de.jeb.japp.cv.service.parser.generator.DocumentType;
import de.jeb.japp.cv.service.parser.generator.ExtractedDocument;
import de.jeb.japp.cv.service.parser.generator.ExtractionMethod;
import de.jeb.japp.cv.service.parser.support.FixtureFiles;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PDFContentGeneratorTest {

    private final PDFContentGenerator generator = new PDFContentGenerator();

    @Test
    void supportsOnlyPdf() {
        assertThat(generator.supports(DocumentType.PDF)).isTrue();
        assertThat(generator.supports(DocumentType.DOCX)).isFalse();
        assertThat(generator.supports(DocumentType.DOC)).isFalse();
    }

    @Test
    void extractsTextFromAGoodPdf() throws IOException {
        byte[] pdf = FixtureFiles.goodPdf();

        ExtractedDocument result = generator.extract(new ByteArrayInputStream(pdf), "cv.pdf", DocumentType.PDF);

        assertThat(result.text()).contains("John Doe", "Senior Software Engineer", "Acme Corp");
        assertThat(result.documentType()).isEqualTo(DocumentType.PDF);
        assertThat(result.extractionMethod()).isEqualTo(ExtractionMethod.PDFBOX);
        assertThat(result.pageCount()).isEqualTo(1);
    }

    @Test
    void returnsNearEmptyTextForAnEmptyPdf() throws IOException {
        byte[] pdf = FixtureFiles.emptyPdf();

        ExtractedDocument result = generator.extract(new ByteArrayInputStream(pdf), "cv.pdf", DocumentType.PDF);

        assertThat(result.text().trim()).isEmpty();
    }

    @Test
    void throwsExtractionExceptionForAMalformedPdf() throws IOException {
        byte[] malformed = FixtureFiles.truncate(FixtureFiles.goodPdf(), 50);

        assertThatThrownBy(() -> generator.extract(new ByteArrayInputStream(malformed), "cv.pdf", DocumentType.PDF))
                .isInstanceOf(CVExtractionException.class);
    }
}
