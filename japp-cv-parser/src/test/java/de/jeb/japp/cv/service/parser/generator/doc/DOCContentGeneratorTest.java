package de.jeb.japp.cv.service.parser.generator.doc;

import de.jeb.japp.commons.exceptions.cv.CVExtractionException;
import de.jeb.japp.cv.service.parser.generator.DocumentType;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * NOTE: there is no automated happy-path test here exercising a real .doc file. Apache POI's
 * HWPF module has no from-scratch document writer (only read + in-place edit of an existing
 * .doc), and this environment has no Word/LibreOffice to generate one externally. The read path
 * itself uses the standard POI HWPFDocument/WordExtractor API, matching DOCXContentGenerator and
 * PDFContentGenerator's approach - manual verification against a real sample .doc is recommended
 * before relying on this generator in production.
 */
class DOCContentGeneratorTest {

    private final DOCContentGenerator generator = new DOCContentGenerator();

    @Test
    void supportsOnlyDoc() {
        assertThat(generator.supports(DocumentType.DOC)).isTrue();
        assertThat(generator.supports(DocumentType.PDF)).isFalse();
        assertThat(generator.supports(DocumentType.DOCX)).isFalse();
    }

    @Test
    void throwsExtractionExceptionForAMalformedDoc() {
        byte[] malformed = "not a real doc file".getBytes();

        assertThatThrownBy(() -> generator.extract(new ByteArrayInputStream(malformed), "cv.doc", DocumentType.DOC))
                .isInstanceOf(CVExtractionException.class);
    }
}
