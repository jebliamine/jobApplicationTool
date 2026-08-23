package de.jeb.japp.ai.service;

import de.jeb.japp.ai.service.encryption.AiCredentialEncryptor;
import de.jeb.japp.commons.exceptions.ai.AiProviderAccessDeniedException;
import de.jeb.japp.commons.exceptions.ai.AiProviderNotFoundException;
import de.jeb.japp.commons.exceptions.ai.AiProviderValidationException;
import de.jeb.japp.dao.ai.AiProviderConfigurationDao;
import de.jeb.japp.model.ai.AdapterType;
import de.jeb.japp.model.ai.AiProviderConfiguration;
import de.jeb.japp.model.ai.dto.AdminAiProviderResponse;
import de.jeb.japp.model.ai.dto.AiProviderCreateRequest;
import de.jeb.japp.model.ai.dto.AiProviderTestResult;
import de.jeb.japp.model.ai.dto.AiProviderUpdateRequest;
import de.jeb.japp.model.user.User;
import de.jeb.japp.model.user.UserRole;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Admin-only AI provider instance management: list, create, update, delete, test connection.
 * Authorization follows the existing project-wide convention — a manual
 * requester.getRole() == ADMIN check in the service layer, never
 * Spring Method Security. Controllers only pass the authenticated User in.
 *
 * Any number of instances may share one {@link AdapterType}; PLACEHOLDER is the one
 * built-in exception — it is seeded once (see AiProviderConfigurationSeeder) and is
 * never admin-creatable or admin-deletable, only enable/disable-able like any other row.
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
        return dao.getAll().stream().map(this::toAdminResponse).toList();
    }

    public AdminAiProviderResponse createProvider(AiProviderCreateRequest request, User requester) {
        assertAdmin(requester);
        AdapterType adapterType = resolveAdapterType(request.getAdapterType());
        if (adapterType == AdapterType.PLACEHOLDER) {
            throw new AiProviderValidationException("A new Placeholder instance cannot be created.");
        }
        if (request.getDisplayName() == null || request.getDisplayName().isBlank()) {
            throw new AiProviderValidationException("A display name is required.");
        }

        AiProviderConfiguration configuration = new AiProviderConfiguration();
        configuration.setAdapterType(adapterType.name());
        configuration.setDisplayName(request.getDisplayName().trim());
        configuration.setEnabled(request.getEnabled() != null && request.getEnabled());
        configuration.setDefaultModel(blankToNull(request.getDefaultModel()));
        configuration.setBaseUrl(blankToNull(request.getBaseUrl()));
        applyApiKey(configuration, request.getApiKey());

        LocalDateTime now = LocalDateTime.now();
        configuration.setCreatedAt(now);
        configuration.setUpdatedAt(now);
        configuration.setUpdatedBy(requester);

        return toAdminResponse(dao.save(configuration));
    }

    public AdminAiProviderResponse updateProvider(UUID id, AiProviderUpdateRequest request, User requester) {
        assertAdmin(requester);
        AiProviderConfiguration configuration = find(id);

        if (request.getDisplayName() != null && !request.getDisplayName().isBlank()) {
            configuration.setDisplayName(request.getDisplayName().trim());
        }
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
            applyApiKey(configuration, request.getApiKey());
        }

        configuration.setUpdatedBy(requester);
        configuration.setUpdatedAt(LocalDateTime.now());
        AiProviderConfiguration saved = dao.save(configuration);

        resolver.invalidate(id);

        return toAdminResponse(saved);
    }

    public void deleteProvider(UUID id, User requester) {
        assertAdmin(requester);
        AiProviderConfiguration configuration = find(id);
        if (AdapterType.PLACEHOLDER.name().equals(configuration.getAdapterType())) {
            throw new AiProviderValidationException("The built-in Placeholder instance cannot be deleted.");
        }
        dao.deleteById(id);
        resolver.invalidate(id);
    }

    public AiProviderTestResult testConnection(UUID id, User requester) {
        assertAdmin(requester);
        find(id);
        return connectionTester.test(id);
    }

    private AdminAiProviderResponse toAdminResponse(AiProviderConfiguration config) {
        boolean hasApiKey = config.getEncryptedApiKey() != null && !config.getEncryptedApiKey().isBlank();
        return new AdminAiProviderResponse(
                config.getId(),
                config.getAdapterType(),
                config.getDisplayName(),
                config.isEnabled(),
                hasApiKey,
                config.getDefaultModel(),
                config.getBaseUrl()
        );
    }

    private void applyApiKey(AiProviderConfiguration configuration, String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return;
        }
        if (!encryptor.isAvailable()) {
            throw new AiProviderValidationException(
                    "Cannot save an API key: encryption is not configured (AI_CREDENTIALS_ENCRYPTION_KEY is missing).");
        }
        configuration.setEncryptedApiKey(encryptor.encrypt(apiKey));
    }

    private AiProviderConfiguration find(UUID id) {
        return dao.getById(id).orElseThrow(() -> new AiProviderNotFoundException("Unknown AI provider instance: " + id));
    }

    private AdapterType resolveAdapterType(String adapterType) {
        try {
            return AdapterType.valueOf(adapterType);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new AiProviderValidationException("Unknown adapter type: " + adapterType);
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private void assertAdmin(User requester) {
        if (requester.getRole() != UserRole.ADMIN) {
            throw new AiProviderAccessDeniedException("Only an administrator can manage AI provider settings.");
        }
    }
}
