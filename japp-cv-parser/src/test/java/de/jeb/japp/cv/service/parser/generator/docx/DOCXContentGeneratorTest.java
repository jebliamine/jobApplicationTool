package de.jeb.japp.cv.service.parser.generator.docx;

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

class DOCXContentGeneratorTest {

    private final DOCXContentGenerator generator = new DOCXContentGenerator();

    @Test
    void supportsOnlyDocx() {
        assertThat(generator.supports(DocumentType.DOCX)).isTrue();
        assertThat(generator.supports(DocumentType.PDF)).isFalse();
        assertThat(generator.supports(DocumentType.DOC)).isFalse();
    }

    @Test
    void extractsTextFromAGoodDocx() throws IOException {
        byte[] docx = FixtureFiles.goodDocx();

        ExtractedDocument result = generator.extract(new ByteArrayInputStream(docx), "cv.docx", DocumentType.DOCX);

        assertThat(result.text()).contains("John Doe", "Senior Software Engineer", "Acme Corp");
        assertThat(result.documentType()).isEqualTo(DocumentType.DOCX);
        assertThat(result.extractionMethod()).isEqualTo(ExtractionMethod.POI_DOCX);
    }

    @Test
    void returnsEmptyTextForAnEmptyDocx() throws IOException {
        byte[] docx = FixtureFiles.emptyDocx();

        ExtractedDocument result = generator.extract(new ByteArrayInputStream(docx), "cv.docx", DocumentType.DOCX);

        assertThat(result.text().trim()).isEmpty();
    }

    @Test
    void throwsExtractionExceptionForAMalformedDocx() {
        byte[] malformed = "not a real docx file".getBytes();

        assertThatThrownBy(() -> generator.extract(new ByteArrayInputStream(malformed), "cv.docx", DocumentType.DOCX))
                .isInstanceOf(CVExtractionException.class);
    }
}
