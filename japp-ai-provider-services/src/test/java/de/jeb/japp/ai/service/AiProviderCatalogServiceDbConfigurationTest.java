package de.jeb.japp.ai.service;

import de.jeb.japp.ai.service.encryption.AiCredentialEncryptor;
import de.jeb.japp.ai.service.gemini.GeminiProperties;
import de.jeb.japp.dao.ai.AiProviderConfigurationDao;
import de.jeb.japp.model.ai.AiProviderConfiguration;
import de.jeb.japp.model.ai.dto.AiProviderResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Regression tests for the reported bug: an admin-configured, enabled Gemini
 * row did not appear as available in GET /api/v1/ai/providers. These wire a
 * REAL ProviderSettingsResolver and a REAL AiProviderCatalogService (only the
 * DAO and encryptor — the actual I/O boundaries — are test doubles), so a
 * regression anywhere in the DAO -> resolver -> catalog chain is caught here,
 * unlike a test that mocks the resolver itself away.
 */
@ExtendWith(MockitoExtension.class)
class AiProviderCatalogServiceDbConfigurationTest {

    @Mock
    private AiProviderConfigurationDao dao;
    @Mock
    private AiCredentialEncryptor encryptor;

    private AiProviderCatalogService catalogService;

    @BeforeEach
    void setUp() {
        GeminiProperties geminiProperties = new GeminiProperties();
        geminiProperties.setApiKey("");
        geminiProperties.setModel("gemini-3.7-flash");
        ProviderSettingsResolver resolver = new ProviderSettingsResolver(dao, encryptor, geminiProperties);
        catalogService = new AiProviderCatalogService(resolver);

        // listProviders() resolves every GenerationProvider, including PLACEHOLDER —
        // stub it harmlessly so tests can focus on the GEMINI row under test.
        lenient().when(dao.getByProvider("PLACEHOLDER")).thenReturn(Optional.empty());
    }

    private AiProviderResponse gemini(List<AiProviderResponse> providers) {
        return providers.stream().filter(p -> p.getId().equals("GEMINI")).findFirst().orElseThrow();
    }

    @Test
    void enabledGeminiWithAStoredKeyAppearsAsAvailable() {
        AiProviderConfiguration row = new AiProviderConfiguration();
        row.setProvider("GEMINI");
        row.setEnabled(true);
        row.setEncryptedApiKey("ciphertext");
        row.setDefaultModel("gemini-3.7-flash");
        when(dao.getByProvider("GEMINI")).thenReturn(Optional.of(row));
        when(encryptor.isAvailable()).thenReturn(true);
        when(encryptor.decrypt("ciphertext")).thenReturn("real-key");

        AiProviderResponse gemini = gemini(catalogService.listProviders());

        assertThat(gemini.isAvailable()).isTrue();
        assertThat(gemini.getModel()).isEqualTo("gemini-3.7-flash");
    }

    @Test
    void disabledGeminiDoesNotAppearAsAvailableEvenWithAStoredKey() {
        AiProviderConfiguration row = new AiProviderConfiguration();
        row.setProvider("GEMINI");
        row.setEnabled(false);
        row.setEncryptedApiKey("ciphertext");
        when(dao.getByProvider("GEMINI")).thenReturn(Optional.of(row));

        assertThat(gemini(catalogService.listProviders()).isAvailable()).isFalse();
    }

    @Test
    void enabledGeminiWithNoStoredKeyAndNoEnvironmentFallbackDoesNotAppearAsAvailable() {
        AiProviderConfiguration row = new AiProviderConfiguration();
        row.setProvider("GEMINI");
        row.setEnabled(true);
        when(dao.getByProvider("GEMINI")).thenReturn(Optional.of(row));

        assertThat(gemini(catalogService.listProviders()).isAvailable()).isFalse();
    }

    @Test
    void enabledGeminiWithAStoredKeyButNoEncryptionKeyConfiguredDoesNotAppearAsAvailable() {
        AiProviderConfiguration row = new AiProviderConfiguration();
        row.setProvider("GEMINI");
        row.setEnabled(true);
        row.setEncryptedApiKey("ciphertext");
        when(dao.getByProvider("GEMINI")).thenReturn(Optional.of(row));
        when(encryptor.isAvailable()).thenReturn(false);

        assertThat(gemini(catalogService.listProviders()).isAvailable()).isFalse();
    }

    @Test
    void placeholderAlwaysAppearsAsAvailableByDefault() {
        when(dao.getByProvider("GEMINI")).thenReturn(Optional.empty());

        List<AiProviderResponse> providers = catalogService.listProviders();

        AiProviderResponse placeholder =
                providers.stream().filter(p -> p.getId().equals("PLACEHOLDER")).findFirst().orElseThrow();
        assertThat(placeholder.isAvailable()).isTrue();
    }
}
