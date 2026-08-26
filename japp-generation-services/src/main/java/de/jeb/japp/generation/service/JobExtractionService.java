package de.jeb.japp.generation.service;

import de.jeb.japp.ai.service.ProviderSettingsResolver;
import de.jeb.japp.ai.service.ResolvedProviderConfig;
import de.jeb.japp.commons.exceptions.ai.AiProviderNotFoundException;
import de.jeb.japp.commons.exceptions.job.JobExtractionException;
import de.jeb.japp.commons.exceptions.job.JobValidationException;
import de.jeb.japp.dao.ai.AiProviderConfigurationDao;
import de.jeb.japp.generation.service.provider.job.JobExtractionAdapter;
import de.jeb.japp.generation.service.provider.job.JobExtractionAdapterRegistry;
import de.jeb.japp.generation.service.provider.job.JobExtractionInput;
import de.jeb.japp.generation.service.provider.job.JobExtractionResult;
import de.jeb.japp.model.ai.AdapterType;
import de.jeb.japp.model.ai.AiProviderConfiguration;
import de.jeb.japp.model.job.EmploymentType;
import de.jeb.japp.model.job.WorkMode;
import de.jeb.japp.model.job.dto.JobExtractionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Runs the job-posting-extraction workflow: resolves the requested (or default Placeholder)
 * provider, calls the matching {@link JobExtractionAdapter}, and maps the result into a
 * {@link JobExtractionResponse} for the frontend to pre-fill the job creation form with. Unlike
 * {@link CvProfileExtractionService}, nothing is persisted here — this is a stateless, one-shot
 * preview — so a JobExtractionException propagates straight to the REST layer instead of being
 * captured as a FAILED status.
 */
@Service
public class JobExtractionService {

    private static final Logger log = LoggerFactory.getLogger(JobExtractionService.class);

    private final AiProviderConfigurationDao providerDao;
    private final ProviderSettingsResolver providerSettingsResolver;
    private final JobExtractionAdapterRegistry adapterRegistry;

    public JobExtractionService(
            AiProviderConfigurationDao providerDao,
            ProviderSettingsResolver providerSettingsResolver,
            JobExtractionAdapterRegistry adapterRegistry
    ) {
        this.providerDao = providerDao;
        this.providerSettingsResolver = providerSettingsResolver;
        this.adapterRegistry = adapterRegistry;
    }

    public JobExtractionResponse extract(String rawText, UUID providerId) {
        if (rawText == null || rawText.isBlank()) {
            throw new JobValidationException("Paste the job posting text to extract from.");
        }

        AiProviderConfiguration providerInstance = resolveProviderInstance(providerId);
        JobExtractionAdapter adapter = adapterRegistry.resolve(resolveAdapterType(providerInstance));
        ResolvedProviderConfig resolvedConfig = providerSettingsResolver.resolve(providerInstance.getId());

        JobExtractionResult result = adapter.extract(resolvedConfig, new JobExtractionInput(rawText));
        return toResponse(result);
    }

    private JobExtractionResponse toResponse(JobExtractionResult result) {
        JobExtractionResponse response = new JobExtractionResponse();
        response.setTitle(blankToNull(result.title()));
        response.setCompanyName(blankToNull(result.companyName()));
        response.setDescription(blankToNull(result.description()));
        response.setLocation(blankToNull(result.location()));
        response.setEmploymentType(parseEmploymentType(result.employmentType()));
        response.setWorkMode(parseWorkMode(result.workMode()));
        response.setSalaryRange(blankToNull(result.salaryRange()));
        response.setUrl(blankToNull(result.url()));
        return response;
    }

    private EmploymentType parseEmploymentType(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return EmploymentType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Job extraction returned an unrecognized employment type, ignoring it: {}", value);
            return null;
        }
    }

    private WorkMode parseWorkMode(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return WorkMode.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Job extraction returned an unrecognized work mode, ignoring it: {}", value);
            return null;
        }
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    /**
     * No providerId means "use the built-in Placeholder instance" — same convention as CvProfileExtractionService.
     */
    private AiProviderConfiguration resolveProviderInstance(UUID providerId) {
        if (providerId == null) {
            return providerDao.getFirstByAdapterType(AdapterType.PLACEHOLDER.name())
                    .orElseThrow(() -> new AiProviderNotFoundException("The built-in Placeholder provider is not available."));
        }
        return providerDao.getById(providerId)
                .orElseThrow(() -> new AiProviderNotFoundException("Unknown AI provider instance: " + providerId));
    }

    private AdapterType resolveAdapterType(AiProviderConfiguration providerInstance) {
        try {
            return AdapterType.valueOf(providerInstance.getAdapterType());
        } catch (IllegalArgumentException e) {
            throw new JobExtractionException("This provider instance has an unknown adapter type.");
        }
    }
}
