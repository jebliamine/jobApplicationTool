package de.jeb.japp.cv.service.parser.config;

import de.jeb.japp.cv.service.parser.generator.ContentGenerator;
import de.jeb.japp.cv.service.parser.generator.doc.DOCContentGenerator;
import de.jeb.japp.cv.service.parser.generator.docx.DOCXContentGenerator;
import de.jeb.japp.cv.service.parser.generator.pdf.PDFContentGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Explicitly wires the format-specific fallback generator list rather than letting Spring
 * auto-collect every {@link ContentGenerator} bean — that would also sweep in
 * TikaContentGenerator (the primary strategy) and OCRContentGenerator (the final, separately
 * orchestrated fallback), neither of which belongs in this pool.
 */
@Configuration
public class ExtractionPipelineConfig {

    @Bean
    public List<ContentGenerator> fallbackContentGenerators(
            PDFContentGenerator pdfContentGenerator,
            DOCXContentGenerator docxContentGenerator,
            DOCContentGenerator docContentGenerator
    ) {
        return List.of(pdfContentGenerator, docxContentGenerator, docContentGenerator);
    }
}
