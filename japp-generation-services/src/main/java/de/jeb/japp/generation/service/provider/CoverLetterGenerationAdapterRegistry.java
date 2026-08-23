package de.jeb.japp.generation.service.provider;

import de.jeb.japp.commons.exceptions.generation.CoverLetterGenerationException;
import de.jeb.japp.model.ai.AdapterType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Resolves an {@link AdapterType} to the {@link CoverLetterGenerationAdapter} bean registered
 * under it. Adapters self-register via {@link CoverLetterGenerationAdapter#type()} — Spring
 * supplies every {@code CoverLetterGenerationAdapter} bean here automatically, so adding another
 * wire protocol later never requires changing this class or GenerationRequestService. Unlike the
 * old provider registry, this is a small, closed set (one entry per protocol, not per admin
 * provider instance).
 */
@Component
public class CoverLetterGenerationAdapterRegistry {

    private final Map<AdapterType, CoverLetterGenerationAdapter> adaptersByType;

    public CoverLetterGenerationAdapterRegistry(List<CoverLetterGenerationAdapter> adapters) {
        this.adaptersByType = adapters.stream()
                .collect(Collectors.toUnmodifiableMap(CoverLetterGenerationAdapter::type, Function.identity()));
    }

    /**
     * @throws CoverLetterGenerationException if no adapter is registered for the given type
     */
    public CoverLetterGenerationAdapter resolve(AdapterType type) {
        CoverLetterGenerationAdapter adapter = adaptersByType.get(type);
        if (adapter == null) {
            throw new CoverLetterGenerationException("The " + type + " adapter is not available.");
        }
        return adapter;
    }
}
