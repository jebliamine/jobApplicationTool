package de.jeb.japp.generation.service.provider.job;

import de.jeb.japp.commons.exceptions.job.JobExtractionException;
import de.jeb.japp.generation.service.provider.cv.CvProfileExtractionAdapterRegistry;
import de.jeb.japp.model.ai.AdapterType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Resolves an {@link AdapterType} to the {@link JobExtractionAdapter} bean registered under it —
 * same self-registration mechanism as {@link CvProfileExtractionAdapterRegistry}.
 */
@Component
public class JobExtractionAdapterRegistry {

    private final Map<AdapterType, JobExtractionAdapter> adaptersByType;

    public JobExtractionAdapterRegistry(List<JobExtractionAdapter> adapters) {
        this.adaptersByType = adapters.stream()
                .collect(Collectors.toUnmodifiableMap(JobExtractionAdapter::type, Function.identity()));
    }

    /**
     * @throws JobExtractionException if no adapter is registered for the given type
     */
    public JobExtractionAdapter resolve(AdapterType type) {
        JobExtractionAdapter adapter = adaptersByType.get(type);
        if (adapter == null) {
            throw new JobExtractionException("The " + type + " adapter is not available.");
        }
        return adapter;
    }
}
