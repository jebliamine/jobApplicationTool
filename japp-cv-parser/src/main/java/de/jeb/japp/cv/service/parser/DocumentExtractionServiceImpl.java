package de.jeb.japp.cv.service.parser;

import de.jeb.japp.commons.exceptions.cv.CVExtractionException;
import de.jeb.japp.commons.exceptions.cv.CVExtractionFailedException;
import de.jeb.japp.commons.exceptions.cv.CVUnsupportedDocumentTypeException;
import de.jeb.japp.cv.service.parser.generator.ContentGenerator;
import de.jeb.japp.cv.service.parser.generator.ContentGeneratorRegistry;
import de.jeb.japp.cv.service.parser.generator.DocumentType;
import de.jeb.japp.cv.service.parser.generator.ExtractedDocument;
import de.jeb.japp.cv.service.parser.generator.TikaContentGenerator;
import de.jeb.japp.cv.service.parser.generator.checker.ExtractionQuality;
import de.jeb.japp.cv.service.parser.generator.checker.ExtractionQualityChecker;
import de.jeb.japp.cv.service.parser.generator.ocr.OCRContentGenerator;
import de.jeb.japp.cv.service.parser.normalizer.DocumentNormalizer;
import de.jeb.japp.cv.service.parser.normalizer.NormalizedDocument;
import de.jeb.japp.cv.service.parser.validator.DocumentValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

@Service
public class DocumentExtractionServiceImpl implements DocumentExtractionService {

    private static final Logger log = LoggerFactory.getLogger(DocumentExtractionServiceImpl.class);

    private final DocumentValidator validator;
    private final TikaContentGenerator tikaContentGenerator;
    private final ContentGeneratorRegistry fallbackRegistry;
    private final OCRContentGenerator ocrContentGenerator;
    private final ExtractionQualityChecker qualityChecker;
    private final DocumentNormalizer normalizer;

    public DocumentExtractionServiceImpl(
            DocumentValidator validator,
            TikaContentGenerator tikaContentGenerator,
            ContentGeneratorRegistry fallbackRegistry,
            OCRContentGenerator ocrContentGenerator,
            ExtractionQualityChecker qualityChecker,
            DocumentNormalizer normalizer
    ) {
        this.validator = validator;
        this.tikaContentGenerator = tikaContentGenerator;
        this.fallbackRegistry = fallbackRegistry;
        this.ocrContentGenerator = ocrContentGenerator;
        this.qualityChecker = qualityChecker;
        this.normalizer = normalizer;
    }

    @Override
    public NormalizedDocument process(Resource resource, String filename, String contentType) {
        long startedAt = System.currentTimeMillis();
        validator.validate(filename, contentType, contentLength(resource));

        DocumentType type = DocumentType.fromFilename(filename)
                .orElseThrow(() -> new CVUnsupportedDocumentTypeException(
                        "Unsupported document type for file: " + filename));

        ExtractedDocument best = attempt(tikaContentGenerator, resource, filename, type);
        log.info("extraction attempt strategy=TIKA type={} quality={}", type, qualityOf(best));

        if (!isGood(best)) {
            Optional<ContentGenerator> fallback = fallbackRegistry.resolveFallback(type);
            if (fallback.isPresent()) {
                ExtractedDocument fallbackResult = attempt(fallback.get(), resource, filename, type);
                log.info("extraction fallback activated type={} quality={}", type, qualityOf(fallbackResult));
                best = betterOf(best, fallbackResult);
            }
        }

        if (!isGood(best) && type == DocumentType.PDF) {
            ExtractedDocument ocrResult = attempt(ocrContentGenerator, resource, filename, type);
            log.info("extraction OCR activated type={} quality={}", type, qualityOf(ocrResult));
            best = betterOf(best, ocrResult);
        }

        if (best == null || qualityOf(best) == ExtractionQuality.EMPTY) {
            throw new CVExtractionFailedException(
                    "All extraction strategies were exhausted for file: " + filename, null);
        }

        long durationMs = System.currentTimeMillis() - startedAt;
        log.info("extraction completed type={} method={} quality={} durationMs={}",
                type, best.extractionMethod(), best.quality(), durationMs);

        return normalizer.normalize(best);
    }

    private ExtractedDocument attempt(ContentGenerator generator, Resource resource, String filename, DocumentType type) {
        try (InputStream stream = resource.getInputStream()) {
            ExtractedDocument result = generator.extract(stream, filename, type);
            ExtractionQuality quality = qualityChecker.check(result.text());
            return result.withQuality(quality);
        } catch (CVExtractionException e) {
            log.warn("extraction strategy failed generator={} type={} reason={}",
                    generator.getClass().getSimpleName(), type, e.getMessage());
            return null;
        } catch (IOException e) {
            log.warn("could not open document stream for extraction generator={} type={}",
                    generator.getClass().getSimpleName(), type);
            return null;
        }
    }

    private boolean isGood(ExtractedDocument document) {
        return document != null && document.quality() == ExtractionQuality.GOOD;
    }

    private ExtractionQuality qualityOf(ExtractedDocument document) {
        return document == null ? ExtractionQuality.EMPTY : document.quality();
    }

    private ExtractedDocument betterOf(ExtractedDocument a, ExtractedDocument b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return a.quality().ordinal() <= b.quality().ordinal() ? a : b;
    }

    private long contentLength(Resource resource) {
        try {
            return resource.contentLength();
        } catch (IOException e) {
            throw new CVUnsupportedDocumentTypeException("Could not read the document to validate it.");
        }
    }
}
