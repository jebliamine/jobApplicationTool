package de.jeb.japp.ai.service;

import de.jeb.japp.ai.service.encryption.AiCredentialEncryptor;
import de.jeb.japp.commons.exceptions.ai.AiProviderAccessDeniedException;
import de.jeb.japp.commons.exceptions.ai.AiProviderNotFoundException;
import de.jeb.japp.commons.exceptions.ai.AiProviderValidationException;
import de.jeb.japp.dao.ai.AiProviderConfigurationDao;
import de.jeb.japp.model.ai.AiProviderConfiguration;
import de.jeb.japp.model.ai.dto.AdminAiProviderResponse;
import de.jeb.japp.model.ai.dto.AiProviderTestResult;
import de.jeb.japp.model.ai.dto.AiProviderUpdateRequest;
import de.jeb.japp.model.generation.GenerationProvider;
import de.jeb.japp.model.user.User;
import de.jeb.japp.model.user.UserRole;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Admin-only AI provider configuration: list, update, test connection.
 * Authorization follows the existing project-wide convention — a manual
 * requester.getRole() == ADMIN check in the service layer, never
 * Spring Method Security. Controllers only pass the authenticated User in.
 */
@Service
public class AdminAiProviderService {

    private final AiProviderConfigurationDao dao;
    private final AiCredentialEncryptor encryptor;
    private final ProviderSettingsResolver resolver;
    private final ProviderConnectionTester connectionTester;

    public AdminAiProviderService(
            AiProviderConfigurationDao dao,
            AiCredentialEncryptor encryptor,
            ProviderSettingsResolver resolver,
            ProviderConnectionTester connectionTester
    ) {
        this.dao = dao;
        this.encryptor = encryptor;
        this.resolver = resolver;
        this.connectionTester = connectionTester;
    }

    public List<AdminAiProviderResponse> listProviders(User requester) {
        assertAdmin(requester);
        return Arrays.stream(GenerationProvider.values())
                .map(this::toAdminResponse)
                .toList();
    }

    public AdminAiProviderResponse updateProvider(String providerId, AiProviderUpdateRequest request, User requester) {
        assertAdmin(requester);
        GenerationProvider provider = resolveProviderId(providerId);
        AiProviderConfiguration configuration = dao.getByProvider(provider.name())
                .orElseGet(() -> newConfiguration(provider));

        if (request.getEnabled() != null) {
            configuration.setEnabled(request.getEnabled());
        }
        if (request.getDefaultModel() != null) {
            configuration.setDefaultModel(blankToNull(request.getDefaultModel()));
        }
        if (request.getBaseUrl() != null) {
            configuration.setBaseUrl(blankToNull(request.getBaseUrl()));
        }

        if (request.isClearApiKey()) {
            configuration.setEncryptedApiKey(null);
        } else if (request.getApiKey() != null && !request.getApiKey().isBlank()) {
            if (!encryptor.isAvailable()) {
                throw new AiProviderValidationException(
                        "Cannot save an API key: encryption is not configured (AI_CREDENTIALS_ENCRYPTION_KEY is missing).");
            }
            configuration.setEncryptedApiKey(encryptor.encrypt(request.getApiKey()));
        }

        configuration.setUpdatedBy(requester);
        configuration.setUpdatedAt(LocalDateTime.now());
        AiProviderConfiguration saved = dao.save(configuration);

        resolver.invalidate(provider);

        return toAdminResponse(provider, saved);
    }

    public AiProviderTestResult testConnection(String providerId, User requester) {
        assertAdmin(requester);
        GenerationProvider provider = resolveProviderId(providerId);
        return connectionTester.test(provider);
    }

    private AdminAiProviderResponse toAdminResponse(GenerationProvider provider) {
        Optional<AiProviderConfiguration> row = dao.getByProvider(provider.name());
        return row.map(config -> toAdminResponse(provider, config))
                .orElseGet(() -> new AdminAiProviderResponse(
                        provider.name(), ProviderDisplayNames.of(provider), false, false, null, null));
    }

    private AdminAiProviderResponse toAdminResponse(GenerationProvider provider, AiProviderConfiguration config) {
        boolean hasApiKey = config.getEncryptedApiKey() != null && !config.getEncryptedApiKey().isBlank();
        return new AdminAiProviderResponse(
                provider.name(),
                ProviderDisplayNames.of(provider),
                config.isEnabled(),
                hasApiKey,
                config.getDefaultModel(),
                config.getBaseUrl()
        );
    }

    private AiProviderConfiguration newConfiguration(GenerationProvider provider) {
        AiProviderConfiguration configuration = new AiProviderConfiguration();
        configuration.setProvider(provider.name());
        configuration.setEnabled(false);
        configuration.setCreatedAt(LocalDateTime.now());
        return configuration;
    }

    private GenerationProvider resolveProviderId(String providerId) {
        try {
            return GenerationProvider.valueOf(providerId);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new AiProviderNotFoundException("Unknown AI provider: " + providerId);
        }
    }

    private String blankToNull(String value) {
        return value.isBlank() ? null : value.trim();
    }

    private void assertAdmin(User requester) {
        if (requester.getRole() != UserRole.ADMIN) {
            throw new AiProviderAccessDeniedException("Only an administrator can manage AI provider settings.");
        }
    }
}
