package de.jeb.japp.cv.service.parser.generator;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentTypeTest {

    @Test
    void resolvesEachSupportedExtension() {
        assertThat(DocumentType.fromFilename("cv.pdf")).contains(DocumentType.PDF);
        assertThat(DocumentType.fromFilename("cv.docx")).contains(DocumentType.DOCX);
        assertThat(DocumentType.fromFilename("cv.doc")).contains(DocumentType.DOC);
        assertThat(DocumentType.fromFilename("CV.PDF")).contains(DocumentType.PDF);
    }

    @Test
    void returnsEmptyForUnsupportedOrMissingExtension() {
        assertThat(DocumentType.fromFilename("cv.txt")).isEmpty();
        assertThat(DocumentType.fromFilename("cv")).isEmpty();
        assertThat(DocumentType.fromFilename(null)).isEmpty();
    }
}
