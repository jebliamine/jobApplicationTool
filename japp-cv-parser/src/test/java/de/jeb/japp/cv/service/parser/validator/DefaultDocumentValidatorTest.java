package de.jeb.japp.cv.service.parser.validator;

import de.jeb.japp.commons.exceptions.cv.CVUnsupportedDocumentTypeException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultDocumentValidatorTest {

    private final DefaultDocumentValidator validator = new DefaultDocumentValidator();

    @Test
    void acceptsAValidPdf() {
        assertThatCode(() -> validator.validate("cv.pdf", "application/pdf", 1024))
                .doesNotThrowAnyException();
    }

    @Test
    void acceptsAValidDocx() {
        assertThatCode(() -> validator.validate("cv.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document", 1024))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsAnEmptyFile() {
        assertThatThrownBy(() -> validator.validate("cv.pdf", "application/pdf", 0))
                .isInstanceOf(CVUnsupportedDocumentTypeException.class);
    }

    @Test
    void rejectsAFileOverTheSizeLimit() {
        long overLimit = 11L * 1024 * 1024;
        assertThatThrownBy(() -> validator.validate("cv.pdf", "application/pdf", overLimit))
                .isInstanceOf(CVUnsupportedDocumentTypeException.class);
    }

    @Test
    void rejectsAnUnsupportedExtension() {
        assertThatThrownBy(() -> validator.validate("cv.txt", "text/plain", 1024))
                .isInstanceOf(CVUnsupportedDocumentTypeException.class);
    }

    @Test
    void rejectsAMismatchedContentType() {
        assertThatThrownBy(() -> validator.validate("cv.pdf", "text/plain", 1024))
                .isInstanceOf(CVUnsupportedDocumentTypeException.class);
    }
}
