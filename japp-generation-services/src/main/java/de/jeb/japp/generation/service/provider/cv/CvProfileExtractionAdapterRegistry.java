package de.jeb.japp.generation.service.provider.cv;

import de.jeb.japp.commons.exceptions.generation.CvProfileGenerationException;
import de.jeb.japp.generation.service.provider.CoverLetterGenerationAdapterRegistry;
import de.jeb.japp.model.ai.AdapterType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Resolves an {@link AdapterType} to the {@link CvProfileExtractionAdapter}
 * bean registered under it — the CV-profile counterpart of
 * {@link CoverLetterGenerationAdapterRegistry}, same self-registration
 * mechanism.
 */
@Component
public class CvProfileExtractionAdapterRegistry {

    private final Map<AdapterType, CvProfileExtractionAdapter> adaptersByType;

    public CvProfileExtractionAdapterRegistry(List<CvProfileExtractionAdapter> adapters) {
        this.adaptersByType = adapters.stream()
                .collect(Collectors.toUnmodifiableMap(CvProfileExtractionAdapter::type, Function.identity()));
    }

    /**
     * @throws CvProfileGenerationException if no adapter is registered for the given type
     */
    public CvProfileExtractionAdapter resolve(AdapterType type) {
        CvProfileExtractionAdapter adapter = adaptersByType.get(type);
        if (adapter == null) {
            throw new CvProfileGenerationException("The " + type + " adapter is not available.");
        }
        return adapter;
    }
}
