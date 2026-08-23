package de.jeb.japp.cv.service.parser.generator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class ContentGeneratorRegistryTest {

    @Mock
    private ContentGenerator pdfGenerator;

    @Mock
    private ContentGenerator docxGenerator;

    @Mock
    private ContentGenerator docGenerator;

    @Test
    void resolvesTheGeneratorThatSupportsTheRequestedType() {
        lenient().when(pdfGenerator.supports(DocumentType.PDF)).thenReturn(true);
        lenient().when(docxGenerator.supports(DocumentType.DOCX)).thenReturn(true);
        lenient().when(docGenerator.supports(DocumentType.DOC)).thenReturn(true);

        ContentGeneratorRegistry registry = new ContentGeneratorRegistry(List.of(pdfGenerator, docxGenerator, docGenerator));

        assertThat(registry.resolveFallback(DocumentType.PDF)).contains(pdfGenerator);
        assertThat(registry.resolveFallback(DocumentType.DOCX)).contains(docxGenerator);
        assertThat(registry.resolveFallback(DocumentType.DOC)).contains(docGenerator);
    }

    @Test
    void returnsEmptyWhenNoGeneratorSupportsTheType() {
        ContentGeneratorRegistry registry = new ContentGeneratorRegistry(List.of());

        Optional<ContentGenerator> result = registry.resolveFallback(DocumentType.PDF);

        assertThat(result).isEmpty();
    }
}
