package de.jeb.japp.generation.service.provider;

import de.jeb.japp.ai.service.ResolvedProviderConfig;
import de.jeb.japp.commons.exceptions.job.JobExtractionException;
import de.jeb.japp.model.ai.AdapterType;

/**
 * Extracts a structured job posting (title/company/description/location/...) for one wire
 * protocol — the job-posting counterpart of {@link CvProfileExtractionAdapter}. Same constraints
 * apply: no authorization, no repository access, no knowledge of HTTP/DTOs.
 */
public interface JobExtractionAdapter {

    AdapterType type();

    /**
     * @throws JobExtractionException if extraction could not be completed
     */
    JobExtractionResult extract(ResolvedProviderConfig config, JobExtractionInput input);
}
