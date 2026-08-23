package de.jeb.japp.ai.service;

import de.jeb.japp.dao.ai.AiProviderConfigurationDao;
import de.jeb.japp.model.ai.AiProviderConfiguration;
import de.jeb.japp.model.ai.dto.AiProviderResponse;
import org.springframework.stereotype.Service;

import java.util.List;

/** Backs the public GET /api/v1/ai/providers — authenticated, not ADMIN-only, never exposes credentials. */
@Service
public class AiProviderCatalogService {

    private final AiProviderConfigurationDao dao;
    private final ProviderSettingsResolver resolver;

    public AiProviderCatalogService(AiProviderConfigurationDao dao, ProviderSettingsResolver resolver) {
        this.dao = dao;
        this.resolver = resolver;
    }

    public List<AiProviderResponse> listProviders() {
        return dao.getAll().stream().map(this::toResponse).toList();
    }

    private AiProviderResponse toResponse(AiProviderConfiguration config) {
        ResolvedProviderConfig resolved = resolver.resolve(config.getId());
        return new AiProviderResponse(
                config.getId().toString(),
                config.getAdapterType(),
                config.getDisplayName(),
                resolved.isAvailable(),
                resolved.isAvailable() ? resolved.getModel() : null
        );
    }
}
