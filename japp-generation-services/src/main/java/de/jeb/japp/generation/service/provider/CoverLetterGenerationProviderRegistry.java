package de.jeb.japp.generation.service.provider;

import de.jeb.japp.model.generation.GenerationProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Resolves a {@link GenerationProvider} id to the {@link CoverLetterGenerationProvider}
 * bean registered under it. Providers self-register via {@link CoverLetterGenerationProvider#id()} —
 * Spring supplies every {@code CoverLetterGenerationProvider} bean here automatically,
 * so adding another provider later never requires changing this class or
 * GenerationRequestService.
 */
@Component
public class CoverLetterGenerationProviderRegistry {

    private final Map<GenerationProvider, CoverLetterGenerationProvider> providersById;

    public CoverLetterGenerationProviderRegistry(List<CoverLetterGenerationProvider> providers) {
        this.providersById = providers.stream()
                .collect(Collectors.toUnmodifiableMap(CoverLetterGenerationProvider::id, Function.identity()));
    }

    /**
     * @throws CoverLetterGenerationException if no provider is registered for the given id
     */
    public CoverLetterGenerationProvider resolve(GenerationProvider id) {
        CoverLetterGenerationProvider provider = providersById.get(id);
        if (provider == null) {
            throw new CoverLetterGenerationException("The " + id + " generation provider is not available.");
        }
        return provider;
    }
}
