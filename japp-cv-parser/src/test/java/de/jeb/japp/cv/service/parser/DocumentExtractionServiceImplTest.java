package de.jeb.japp.cv.service.parser;

import de.jeb.japp.commons.exceptions.cv.CVExtractionException;
import de.jeb.japp.commons.exceptions.cv.CVExtractionFailedException;
import de.jeb.japp.commons.exceptions.cv.CVUnsupportedDocumentTypeException;
import de.jeb.japp.cv.service.parser.generator.ContentGenerator;
import de.jeb.japp.cv.service.parser.generator.ContentGeneratorRegistry;
import de.jeb.japp.cv.service.parser.generator.DocumentType;
import de.jeb.japp.cv.service.parser.generator.ExtractedDocument;
import de.jeb.japp.cv.service.parser.generator.ExtractionMethod;
import de.jeb.japp.cv.service.parser.generator.checker.DefaultExtractionQualityChecker;
import de.jeb.japp.cv.service.parser.generator.ocr.OCRContentGenerator;
import de.jeb.japp.cv.service.parser.generator.TikaContentGenerator;
import de.jeb.japp.cv.service.parser.normalizer.DefaultDocumentNormalizer;
import de.jeb.japp.cv.service.parser.normalizer.NormalizedDocument;
import de.jeb.japp.cv.service.parser.support.FixtureFiles;
import de.jeb.japp.cv.service.parser.validator.DefaultDocumentValidator;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DocumentExtractionServiceImplTest {

    private final DefaultDocumentValidator validator = new DefaultDocumentValidator();
    private final DefaultExtractionQualityChecker qualityChecker = new DefaultExtractionQualityChecker();
    private final DefaultDocumentNormalizer normalizer = new DefaultDocumentNormalizer();

    private static final String GOOD_TEXT = String.join("\n", FixtureFiles.CV_LINES).repeat(2);
    private static final String INSUFFICIENT_TEXT = "x";

    private Resource pdfResource() throws IOException {
        return new ByteArrayResource(FixtureFiles.goodPdf());
    }

    private ExtractedDocument document(String text, DocumentType type, ExtractionMethod method) {
        return new ExtractedDocument(text, "application/pdf", "cv.pdf", text.length(), 1, type, method, null);
    }

    @Test
    void tikaSucceedsGood_fallbackAndOcrAreNeverInvoked() throws IOException {
        TikaContentGenerator tika = mock(TikaContentGenerator.class);
        OCRContentGenerator ocr = mock(OCRContentGenerator.class);
        ContentGenerator fallback = mock(ContentGenerator.class);
        when(fallback.supports(DocumentType.PDF)).thenReturn(true);
        when(tika.extract(any(), eq("cv.pdf"), eq(DocumentType.PDF)))
                .thenReturn(document(GOOD_TEXT, DocumentType.PDF, ExtractionMethod.TIKA));

        DocumentExtractionServiceImpl service = new DocumentExtractionServiceImpl(
                validator, tika, new ContentGeneratorRegistry(java.util.List.of(fallback)), ocr, qualityChecker, normalizer);

        NormalizedDocument result = service.process(pdfResource(), "cv.pdf", "application/pdf");

        assertThat(result.text()).contains("John Doe");
        verify(fallback, never()).extract(any(), any(), any());
        verify(ocr, never()).extract(any(), any(), any());
    }

    @Test
    void tikaPoor_fallbackSucceeds_ocrNeverInvoked() throws IOException {
        TikaContentGenerator tika = mock(TikaContentGenerator.class);
        OCRContentGenerator ocr = mock(OCRContentGenerator.class);
        ContentGenerator fallback = mock(ContentGenerator.class);
        when(fallback.supports(DocumentType.PDF)).thenReturn(true);
        when(tika.extract(any(), eq("cv.pdf"), eq(DocumentType.PDF)))
                .thenReturn(document(INSUFFICIENT_TEXT, DocumentType.PDF, ExtractionMethod.TIKA));
        when(fallback.extract(any(), eq("cv.pdf"), eq(DocumentType.PDF)))
                .thenReturn(document(GOOD_TEXT, DocumentType.PDF, ExtractionMethod.PDFBOX));

        DocumentExtractionServiceImpl service = new DocumentExtractionServiceImpl(
                validator, tika, new ContentGeneratorRegistry(java.util.List.of(fallback)), ocr, qualityChecker, normalizer);

        NormalizedDocument result = service.process(pdfResource(), "cv.pdf", "application/pdf");

        assertThat(result.text()).contains("John Doe");
        verify(ocr, never()).extract(any(), any(), any());
    }

    @Test
    void tikaAndFallbackPoor_ocrInvokedAsFinalFallback() throws IOException {
        TikaContentGenerator tika = mock(TikaContentGenerator.class);
        OCRContentGenerator ocr = mock(OCRContentGenerator.class);
        ContentGenerator fallback = mock(ContentGenerator.class);
        when(fallback.supports(DocumentType.PDF)).thenReturn(true);
        when(tika.extract(any(), eq("cv.pdf"), eq(DocumentType.PDF)))
                .thenReturn(document(INSUFFICIENT_TEXT, DocumentType.PDF, ExtractionMethod.TIKA));
        when(fallback.extract(any(), eq("cv.pdf"), eq(DocumentType.PDF)))
                .thenReturn(document(INSUFFICIENT_TEXT, DocumentType.PDF, ExtractionMethod.PDFBOX));
        when(ocr.extract(any(), eq("cv.pdf"), eq(DocumentType.PDF)))
                .thenReturn(document(GOOD_TEXT, DocumentType.PDF, ExtractionMethod.OCR));

        DocumentExtractionServiceImpl service = new DocumentExtractionServiceImpl(
                validator, tika, new ContentGeneratorRegistry(java.util.List.of(fallback)), ocr, qualityChecker, normalizer);

        NormalizedDocument result = service.process(pdfResource(), "cv.pdf", "application/pdf");

        assertThat(result.text()).contains("John Doe");
    }

    @Test
    void allStrategiesEmpty_throwsExtractionFailed() throws IOException {
        TikaContentGenerator tika = mock(TikaContentGenerator.class);
        OCRContentGenerator ocr = mock(OCRContentGenerator.class);
        ContentGenerator fallback = mock(ContentGenerator.class);
        when(fallback.supports(DocumentType.PDF)).thenReturn(true);
        when(tika.extract(any(), eq("cv.pdf"), eq(DocumentType.PDF)))
                .thenReturn(document("", DocumentType.PDF, ExtractionMethod.TIKA));
        when(fallback.extract(any(), eq("cv.pdf"), eq(DocumentType.PDF)))
                .thenReturn(document("", DocumentType.PDF, ExtractionMethod.PDFBOX));
        when(ocr.extract(any(), eq("cv.pdf"), eq(DocumentType.PDF)))
                .thenReturn(document("", DocumentType.PDF, ExtractionMethod.OCR));

        DocumentExtractionServiceImpl service = new DocumentExtractionServiceImpl(
                validator, tika, new ContentGeneratorRegistry(java.util.List.of(fallback)), ocr, qualityChecker, normalizer);

        assertThatThrownBy(() -> service.process(pdfResource(), "cv.pdf", "application/pdf"))
                .isInstanceOf(CVExtractionFailedException.class);
    }

    @Test
    void tikaThrows_fallbackStillAttemptedAndSucceeds() throws IOException {
        TikaContentGenerator tika = mock(TikaContentGenerator.class);
        OCRContentGenerator ocr = mock(OCRContentGenerator.class);
        ContentGenerator fallback = mock(ContentGenerator.class);
        when(fallback.supports(DocumentType.PDF)).thenReturn(true);
        when(tika.extract(any(), eq("cv.pdf"), eq(DocumentType.PDF)))
                .thenThrow(new CVExtractionException("boom", null));
        when(fallback.extract(any(), eq("cv.pdf"), eq(DocumentType.PDF)))
                .thenReturn(document(GOOD_TEXT, DocumentType.PDF, ExtractionMethod.PDFBOX));

        DocumentExtractionServiceImpl service = new DocumentExtractionServiceImpl(
                validator, tika, new ContentGeneratorRegistry(java.util.List.of(fallback)), ocr, qualityChecker, normalizer);

        NormalizedDocument result = service.process(pdfResource(), "cv.pdf", "application/pdf");

        assertThat(result.text()).contains("John Doe");
    }

    @Test
    void unsupportedFileType_throwsBeforeAnyExtractionIsAttempted() throws IOException {
        TikaContentGenerator tika = mock(TikaContentGenerator.class);
        OCRContentGenerator ocr = mock(OCRContentGenerator.class);
        ContentGeneratorRegistry registry = new ContentGeneratorRegistry(java.util.List.of());

        DocumentExtractionServiceImpl service = new DocumentExtractionServiceImpl(
                validator, tika, registry, ocr, qualityChecker, normalizer);

        assertThatThrownBy(() -> service.process(
                new ByteArrayResource("hello".getBytes()), "cv.txt", "text/plain"))
                .isInstanceOf(CVUnsupportedDocumentTypeException.class);

        verify(tika, never()).extract(any(), any(), any());
    }
}
