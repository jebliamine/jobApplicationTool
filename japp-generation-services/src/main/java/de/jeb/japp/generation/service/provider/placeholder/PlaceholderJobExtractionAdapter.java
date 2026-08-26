package de.jeb.japp.generation.service.provider.placeholder;

import de.jeb.japp.ai.service.ResolvedProviderConfig;
import de.jeb.japp.commons.exceptions.job.JobExtractionException;
import de.jeb.japp.generation.service.provider.job.JobExtractionAdapter;
import de.jeb.japp.generation.service.provider.job.JobExtractionInput;
import de.jeb.japp.generation.service.provider.job.JobExtractionResult;
import de.jeb.japp.model.ai.AdapterType;
import org.springframework.stereotype.Service;

/**
 * Deterministic, no external AI call: there is no real structured extraction to perform without a
 * model, so this is used whenever the built-in Placeholder instance is selected — same rationale
 * as {@link PlaceholderCvProfileExtractionAdapter}.
 */
@Service
public class PlaceholderJobExtractionAdapter implements JobExtractionAdapter {

    @Override
    public AdapterType type() {
        return AdapterType.PLACEHOLDER;
    }

    @Override
    public JobExtractionResult extract(ResolvedProviderConfig config, JobExtractionInput input) {
        if (input.rawText() == null || input.rawText().isBlank()) {
            throw new JobExtractionException("There is no job posting text to extract from.");
        }

        return new JobExtractionResult(
                null, null,
                "Configure a real AI provider to extract this job posting automatically. "
                        + "[This is a placeholder result generated without an AI provider.]",
                null, null, null, null, null);
    }
}
