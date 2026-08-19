package de.jeb.japp.ai.service;

import de.jeb.japp.ai.service.encryption.AiCredentialEncryptor;
import de.jeb.japp.ai.service.gemini.GeminiProperties;
import de.jeb.japp.dao.ai.AiProviderConfigurationDao;
import de.jeb.japp.model.ai.AiProviderConfiguration;
import de.jeb.japp.model.generation.GenerationProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProviderSettingsResolverTest {

    @Mock
    private AiProviderConfigurationDao dao;
    @Mock
    private AiCredentialEncryptor encryptor;

    private GeminiProperties geminiProperties;
    private ProviderSettingsResolver resolver;

    @BeforeEach
    void setUp() {
        geminiProperties = new GeminiProperties();
        geminiProperties.setApiKey("");
        geminiProperties.setModel("gemini-2.0-flash");
        geminiProperties.setBaseUrl("https://generativelanguage.googleapis.com");
        geminiProperties.setTimeout(Duration.ofSeconds(30));

        resolver = new ProviderSettingsResolver(dao, encryptor, geminiProperties);
    }

    private AiProviderConfiguration configFor(String provider, boolean enabled) {
        AiProviderConfiguration config = new AiProviderConfiguration();
        config.setProvider(provider);
        config.setEnabled(enabled);
        return config;
    }

    @Test
    void placeholderIsAvailableWhenNoRowExistsYet() {
        when(dao.getByProvider("PLACEHOLDER")).thenReturn(Optional.empty());

        assertThat(resolver.resolve(GenerationProvider.PLACEHOLDER).isAvailable()).isTrue();
    }

    @Test
    void placeholderIsUnavailableWhenExplicitlyDisabled() {
        when(dao.getByProvider("PLACEHOLDER")).thenReturn(Optional.of(configFor("PLACEHOLDER", false)));

        assertThat(resolver.resolve(GenerationProvider.PLACEHOLDER).isAvailable()).isFalse();
    }

    @Test
    void geminiFallsBackToEnvironmentWhenNoDatabaseRowExists() {
        geminiProperties.setApiKey("env-api-key");
        when(dao.getByProvider("GEMINI")).thenReturn(Optional.empty());

        ResolvedProviderConfig resolved = resolver.resolve(GenerationProvider.GEMINI);

        assertThat(resolved.isAvailable()).isTrue();
        assertThat(resolved.getApiKey()).isEqualTo("env-api-key");
        assertThat(resolved.getModel()).isEqualTo("gemini-2.0-flash");
    }

    @Test
    void geminiIsUnavailableWhenNeitherDatabaseNorEnvironmentIsConfigured() {
        when(dao.getByProvider("GEMINI")).thenReturn(Optional.empty());

        assertThat(resolver.resolve(GenerationProvider.GEMINI).isAvailable()).isFalse();
    }

    @Test
    void databaseConfigurationTakesPrecedenceOverEnvironment() {
        geminiProperties.setApiKey("env-api-key");
        AiProviderConfiguration config = configFor("GEMINI", true);
        config.setEncryptedApiKey("ciphertext");
        config.setDefaultModel("gemini-db-model");
        config.setBaseUrl("https://db-base-url.example");
        when(dao.getByProvider("GEMINI")).thenReturn(Optional.of(config));
        when(encryptor.isAvailable()).thenReturn(true);
        when(encryptor.decrypt("ciphertext")).thenReturn("db-api-key");

        ResolvedProviderConfig resolved = resolver.resolve(GenerationProvider.GEMINI);

        assertThat(resolved.isAvailable()).isTrue();
        assertThat(resolved.getApiKey()).isEqualTo("db-api-key");
        assertThat(resolved.getModel()).isEqualTo("gemini-db-model");
        assertThat(resolved.getBaseUrl()).isEqualTo("https://db-base-url.example");
    }

    @Test
    void disabledDatabaseRowFallsBackToEnvironment() {
        geminiProperties.setApiKey("env-api-key");
        AiProviderConfiguration config = configFor("GEMINI", false);
        config.setEncryptedApiKey("ciphertext");
        when(dao.getByProvider("GEMINI")).thenReturn(Optional.of(config));

        ResolvedProviderConfig resolved = resolver.resolve(GenerationProvider.GEMINI);

        assertThat(resolved.isAvailable()).isTrue();
        assertThat(resolved.getApiKey()).isEqualTo("env-api-key");
        verify(encryptor, never()).decrypt(any());
    }

    @Test
    void databaseRowEnabledButNoKeyStoredFallsBackToEnvironment() {
        geminiProperties.setApiKey("env-api-key");
        AiProviderConfiguration config = configFor("GEMINI", true);
        when(dao.getByProvider("GEMINI")).thenReturn(Optional.of(config));

        ResolvedProviderConfig resolved = resolver.resolve(GenerationProvider.GEMINI);

        assertThat(resolved.isAvailable()).isTrue();
        assertThat(resolved.getApiKey()).isEqualTo("env-api-key");
    }

    @Test
    void decryptionFailureFallsBackToEnvironmentInsteadOfCrashing() {
        geminiProperties.setApiKey("env-api-key");
        AiProviderConfiguration config = configFor("GEMINI", true);
        config.setEncryptedApiKey("ciphertext");
        when(dao.getByProvider("GEMINI")).thenReturn(Optional.of(config));
        when(encryptor.isAvailable()).thenReturn(true);
        when(encryptor.decrypt("ciphertext")).thenThrow(new RuntimeException("bad key"));

        ResolvedProviderConfig resolved = resolver.resolve(GenerationProvider.GEMINI);

        assertThat(resolved.isAvailable()).isTrue();
        assertThat(resolved.getApiKey()).isEqualTo("env-api-key");
    }

    @Test
    void resultsAreCachedAcrossCalls() {
        when(dao.getByProvider("PLACEHOLDER")).thenReturn(Optional.empty());

        resolver.resolve(GenerationProvider.PLACEHOLDER);
        resolver.resolve(GenerationProvider.PLACEHOLDER);

        verify(dao, times(1)).getByProvider("PLACEHOLDER");
    }

    @Test
    void invalidateForcesFreshResolutionOnNextCall() {
        when(dao.getByProvider("PLACEHOLDER")).thenReturn(Optional.empty());

        resolver.resolve(GenerationProvider.PLACEHOLDER);
        resolver.invalidate(GenerationProvider.PLACEHOLDER);
        resolver.resolve(GenerationProvider.PLACEHOLDER);

        verify(dao, times(2)).getByProvider("PLACEHOLDER");
    }
}
