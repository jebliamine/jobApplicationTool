package de.jeb.japp.generation.service.provider;

import de.jeb.japp.commons.exceptions.generation.CoverLetterGenerationException;
import de.jeb.japp.model.ai.AdapterType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CoverLetterGenerationAdapterRegistryTest {

    private CoverLetterGenerationAdapter adapterFor(AdapterType type) {
        CoverLetterGenerationAdapter adapter = mock(CoverLetterGenerationAdapter.class);
        when(adapter.type()).thenReturn(type);
        return adapter;
    }

    @Test
    void resolvesPlaceholder() {
        CoverLetterGenerationAdapter placeholder = adapterFor(AdapterType.PLACEHOLDER);
        CoverLetterGenerationAdapterRegistry registry = new CoverLetterGenerationAdapterRegistry(List.of(placeholder));

        assertThat(registry.resolve(AdapterType.PLACEHOLDER)).isSameAs(placeholder);
    }

    @Test
    void resolvesTheCorrectAdapterWhenSeveralAreRegistered() {
        CoverLetterGenerationAdapter placeholder = adapterFor(AdapterType.PLACEHOLDER);
        CoverLetterGenerationAdapter gemini = adapterFor(AdapterType.GEMINI_GENERATE_CONTENT);
        CoverLetterGenerationAdapter openAi = adapterFor(AdapterType.OPENAI_COMPATIBLE);
        CoverLetterGenerationAdapterRegistry registry =
                new CoverLetterGenerationAdapterRegistry(List.of(placeholder, gemini, openAi));

        assertThat(registry.resolve(AdapterType.PLACEHOLDER)).isSameAs(placeholder);
        assertThat(registry.resolve(AdapterType.GEMINI_GENERATE_CONTENT)).isSameAs(gemini);
        assertThat(registry.resolve(AdapterType.OPENAI_COMPATIBLE)).isSameAs(openAi);
    }

    @Test
    void unregisteredAdapterTypeThrowsCoverLetterGenerationException() {
        CoverLetterGenerationAdapterRegistry registry =
                new CoverLetterGenerationAdapterRegistry(List.of(adapterFor(AdapterType.PLACEHOLDER)));

        assertThatThrownBy(() -> registry.resolve(AdapterType.ANTHROPIC_MESSAGES))
                .isInstanceOf(CoverLetterGenerationException.class);
    }
}
