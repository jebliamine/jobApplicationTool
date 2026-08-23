package de.jeb.japp.generation.service.provider;

import de.jeb.japp.commons.exceptions.generation.CoverLetterGenerationException;
import de.jeb.japp.model.generation.GenerationProvider;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CoverLetterGenerationProviderRegistryTest {

    private CoverLetterGenerationProvider providerFor(GenerationProvider id) {
        CoverLetterGenerationProvider provider = mock(CoverLetterGenerationProvider.class);
        when(provider.id()).thenReturn(id);
        return provider;
    }

    @Test
    void resolvesPlaceholder() {
        CoverLetterGenerationProvider placeholder = providerFor(GenerationProvider.PLACEHOLDER);
        CoverLetterGenerationProviderRegistry registry =
                new CoverLetterGenerationProviderRegistry(List.of(placeholder));

        assertThat(registry.resolve(GenerationProvider.PLACEHOLDER)).isSameAs(placeholder);
    }

    @Test
    void resolvesGemini() {
        CoverLetterGenerationProvider gemini = providerFor(GenerationProvider.GEMINI);
        CoverLetterGenerationProviderRegistry registry =
                new CoverLetterGenerationProviderRegistry(List.of(gemini));

        assertThat(registry.resolve(GenerationProvider.GEMINI)).isSameAs(gemini);
    }

    @Test
    void resolvesTheCorrectProviderWhenBothAreRegistered() {
        CoverLetterGenerationProvider placeholder = providerFor(GenerationProvider.PLACEHOLDER);
        CoverLetterGenerationProvider gemini = providerFor(GenerationProvider.GEMINI);
        CoverLetterGenerationProviderRegistry registry =
                new CoverLetterGenerationProviderRegistry(List.of(placeholder, gemini));

        assertThat(registry.resolve(GenerationProvider.PLACEHOLDER)).isSameAs(placeholder);
        assertThat(registry.resolve(GenerationProvider.GEMINI)).isSameAs(gemini);
    }

    @Test
    void unknownProviderThrowsCoverLetterGenerationException() {
        CoverLetterGenerationProviderRegistry registry =
                new CoverLetterGenerationProviderRegistry(List.of(providerFor(GenerationProvider.PLACEHOLDER)));

        assertThatThrownBy(() -> registry.resolve(GenerationProvider.GEMINI))
                .isInstanceOf(CoverLetterGenerationException.class);
    }
}
