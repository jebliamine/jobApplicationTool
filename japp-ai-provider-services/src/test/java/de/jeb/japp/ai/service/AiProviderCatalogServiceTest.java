package de.jeb.japp.ai.service;

import de.jeb.japp.model.ai.dto.AiProviderResponse;
import de.jeb.japp.model.generation.GenerationProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiProviderCatalogServiceTest {

    @Mock
    private ProviderSettingsResolver resolver;

    private AiProviderCatalogService service;

    @BeforeEach
    void setUp() {
        service = new AiProviderCatalogService(resolver);
    }

    @Test
    void listsEveryKnownProvider() {
        when(resolver.resolve(any())).thenReturn(ResolvedProviderConfig.unavailable());

        List<AiProviderResponse> result = service.listProviders();

        assertThat(result).hasSize(GenerationProvider.values().length);
    }

    @Test
    void reflectsAvailabilityFromTheResolver() {
        when(resolver.resolve(GenerationProvider.PLACEHOLDER))
                .thenReturn(new ResolvedProviderConfig(true, null, "deterministic-v1", null));
        when(resolver.resolve(GenerationProvider.GEMINI))
                .thenReturn(ResolvedProviderConfig.unavailable());

        List<AiProviderResponse> result = service.listProviders();

        AiProviderResponse placeholder =
                result.stream().filter(r -> r.getId().equals("PLACEHOLDER")).findFirst().orElseThrow();
        AiProviderResponse gemini = result.stream().filter(r -> r.getId().equals("GEMINI")).findFirst().orElseThrow();

        assertThat(placeholder.isAvailable()).isTrue();
        assertThat(placeholder.getModel()).isEqualTo("deterministic-v1");
        assertThat(gemini.isAvailable()).isFalse();
        assertThat(gemini.getModel()).isNull();
    }

    @Test
    void neverExposesCredentialInformation() {
        assertThat(AiProviderResponse.class.getDeclaredFields())
                .noneMatch(f -> f.getName().toLowerCase().contains("key"));
    }
}
