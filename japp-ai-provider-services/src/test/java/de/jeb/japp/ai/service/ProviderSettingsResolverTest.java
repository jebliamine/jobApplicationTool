package de.jeb.japp.ai.service;

import de.jeb.japp.ai.service.encryption.AiCredentialEncryptor;
import de.jeb.japp.dao.ai.AiProviderConfigurationDao;
import de.jeb.japp.model.ai.AdapterType;
import de.jeb.japp.model.ai.AiProviderConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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

    private ProviderSettingsResolver resolver;

    private UUID instanceId;

    @BeforeEach
    void setUp() {
        resolver = new ProviderSettingsResolver(dao, encryptor);
        instanceId = UUID.randomUUID();
    }

    private AiProviderConfiguration configFor(AdapterType type, boolean enabled) {
        AiProviderConfiguration config = new AiProviderConfiguration();
        config.setAdapterType(type.name());
        config.setEnabled(enabled);
        return config;
    }

    @Test
    void nullInstanceIdIsUnavailable() {
        assertThat(resolver.resolve(null).isAvailable()).isFalse();
    }

    @Test
    void unknownInstanceIsUnavailable() {
        when(dao.getById(instanceId)).thenReturn(Optional.empty());

        assertThat(resolver.resolve(instanceId).isAvailable()).isFalse();
    }

    @Test
    void disabledInstanceIsUnavailable() {
        when(dao.getById(instanceId)).thenReturn(Optional.of(configFor(AdapterType.GEMINI_GENERATE_CONTENT, false)));

        assertThat(resolver.resolve(instanceId).isAvailable()).isFalse();
    }

    @Test
    void enabledPlaceholderIsAvailableWithNoCredential() {
        AiProviderConfiguration config = configFor(AdapterType.PLACEHOLDER, true);
        config.setDefaultModel("deterministic-v1");
        when(dao.getById(instanceId)).thenReturn(Optional.of(config));

        var resolved = resolver.resolve(instanceId);

        assertThat(resolved.isAvailable()).isTrue();
        assertThat(resolved.getApiKey()).isNull();
        assertThat(resolved.getModel()).isEqualTo("deterministic-v1");
    }

    @Test
    void enabledWithADecryptableKeyIsAvailable() {
        AiProviderConfiguration config = configFor(AdapterType.GEMINI_GENERATE_CONTENT, true);
        config.setEncryptedApiKey("ciphertext");
        config.setDefaultModel("gemini-3.7-flash");
        config.setBaseUrl("https://generativelanguage.googleapis.com");
        when(dao.getById(instanceId)).thenReturn(Optional.of(config));
        when(encryptor.isAvailable()).thenReturn(true);
        when(encryptor.decrypt("ciphertext")).thenReturn("real-key");

        var resolved = resolver.resolve(instanceId);

        assertThat(resolved.isAvailable()).isTrue();
        assertThat(resolved.getApiKey()).isEqualTo("real-key");
        assertThat(resolved.getModel()).isEqualTo("gemini-3.7-flash");
        assertThat(resolved.getBaseUrl()).isEqualTo("https://generativelanguage.googleapis.com");
    }

    @Test
    void enabledWithNoStoredKeyIsUnavailable() {
        when(dao.getById(instanceId)).thenReturn(Optional.of(configFor(AdapterType.GEMINI_GENERATE_CONTENT, true)));

        assertThat(resolver.resolve(instanceId).isAvailable()).isFalse();
    }

    @Test
    void enabledWithAStoredKeyButNoEncryptionKeyConfiguredIsUnavailable() {
        AiProviderConfiguration config = configFor(AdapterType.GEMINI_GENERATE_CONTENT, true);
        config.setEncryptedApiKey("ciphertext");
        when(dao.getById(instanceId)).thenReturn(Optional.of(config));
        when(encryptor.isAvailable()).thenReturn(false);

        assertThat(resolver.resolve(instanceId).isAvailable()).isFalse();
    }

    @Test
    void decryptionFailureIsUnavailableInsteadOfCrashing() {
        AiProviderConfiguration config = configFor(AdapterType.GEMINI_GENERATE_CONTENT, true);
        config.setEncryptedApiKey("ciphertext");
        when(dao.getById(instanceId)).thenReturn(Optional.of(config));
        when(encryptor.isAvailable()).thenReturn(true);
        when(encryptor.decrypt("ciphertext")).thenThrow(new RuntimeException("bad key"));

        assertThat(resolver.resolve(instanceId).isAvailable()).isFalse();
    }

    @Test
    void resultsAreCachedAcrossCalls() {
        when(dao.getById(instanceId)).thenReturn(Optional.of(configFor(AdapterType.PLACEHOLDER, true)));

        resolver.resolve(instanceId);
        resolver.resolve(instanceId);

        verify(dao, times(1)).getById(instanceId);
    }

    @Test
    void invalidateForcesFreshResolutionOnNextCall() {
        when(dao.getById(instanceId)).thenReturn(Optional.of(configFor(AdapterType.PLACEHOLDER, true)));

        resolver.resolve(instanceId);
        resolver.invalidate(instanceId);
        resolver.resolve(instanceId);

        verify(dao, times(2)).getById(instanceId);
    }

    @Test
    void placeholderNeverAttemptsDecryption() {
        when(dao.getById(instanceId)).thenReturn(Optional.of(configFor(AdapterType.PLACEHOLDER, true)));

        resolver.resolve(instanceId);

        verify(encryptor, never()).decrypt(org.mockito.ArgumentMatchers.any());
    }
}
