package de.jeb.japp.ai.service;

import de.jeb.japp.model.ai.dto.AiProviderResponse;
import de.jeb.japp.model.generation.GenerationProvider;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/** Backs the public GET /api/v1/ai/providers — authenticated, not ADMIN-only, never exposes credentials. */
@Service
public class AiProviderCatalogService {

    private final ProviderSettingsResolver resolver;

    public AiProviderCatalogService(ProviderSettingsResolver resolver) {
        this.resolver = resolver;
    }

    public List<AiProviderResponse> listProviders() {
        return Arrays.stream(GenerationProvider.values())
                .map(this::toResponse)
                .toList();
    }

    private AiProviderResponse toResponse(GenerationProvider provider) {
        ResolvedProviderConfig resolved = resolver.resolve(provider);
        return new AiProviderResponse(
                provider.name(),
                ProviderDisplayNames.of(provider),
                resolved.isAvailable(),
                resolved.isAvailable() ? resolved.getModel() : null
        );
    }
}
