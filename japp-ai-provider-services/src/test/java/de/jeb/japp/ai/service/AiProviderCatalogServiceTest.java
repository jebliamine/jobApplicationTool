package de.jeb.japp.ai.service;

import de.jeb.japp.dao.ai.AiProviderConfigurationDao;
import de.jeb.japp.model.ai.AdapterType;
import de.jeb.japp.model.ai.AiProviderConfiguration;
import de.jeb.japp.model.ai.dto.AiProviderResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiProviderCatalogServiceTest {

    @Mock
    private AiProviderConfigurationDao dao;
    @Mock
    private ProviderSettingsResolver resolver;

    private AiProviderCatalogService service;

    @BeforeEach
    void setUp() {
        service = new AiProviderCatalogService(dao, resolver);
    }

    private AiProviderConfiguration instance(UUID id, AdapterType type, String displayName) {
        AiProviderConfiguration config = new AiProviderConfiguration();
        config.setAdapterType(type.name());
        config.setDisplayName(displayName);
        ReflectionTestUtils.setField(config, "id", id);
        return config;
    }

    @Test
    void listsEveryInstanceFromTheRepository() {
        UUID id = UUID.randomUUID();
        when(dao.getAll()).thenReturn(List.of(instance(id, AdapterType.PLACEHOLDER, "Placeholder")));
        when(resolver.resolve(any())).thenReturn(ResolvedProviderConfig.unavailable());

        List<AiProviderResponse> result = service.listProviders();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(id.toString());
        assertThat(result.get(0).getDisplayName()).isEqualTo("Placeholder");
    }

    @Test
    void reflectsAvailabilityFromTheResolver() {
        UUID placeholderId = UUID.randomUUID();
        UUID geminiId = UUID.randomUUID();
        when(dao.getAll()).thenReturn(List.of(
                instance(placeholderId, AdapterType.PLACEHOLDER, "Placeholder"),
                instance(geminiId, AdapterType.GEMINI_GENERATE_CONTENT, "Google Gemini")
        ));
        when(resolver.resolve(placeholderId))
                .thenReturn(new ResolvedProviderConfig(true, null, "deterministic-v1", null));
        when(resolver.resolve(geminiId)).thenReturn(ResolvedProviderConfig.unavailable());

        List<AiProviderResponse> result = service.listProviders();

        AiProviderResponse placeholder =
                result.stream().filter(r -> r.getId().equals(placeholderId.toString())).findFirst().orElseThrow();
        AiProviderResponse gemini =
                result.stream().filter(r -> r.getId().equals(geminiId.toString())).findFirst().orElseThrow();

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
