package de.jeb.japp.generation.service.provider;

import de.jeb.japp.ai.service.ResolvedProviderConfig;
import de.jeb.japp.commons.exceptions.generation.CvProfileGenerationException;
import de.jeb.japp.model.ai.AdapterType;

/**
 * Extracts a structured CV profile (name/summary/experience) for one wire
 * protocol — the CV-profile counterpart of {@link CoverLetterGenerationAdapter}.
 * Same constraints apply: no authorization, no repository access, no
 * knowledge of HTTP/DTOs, no mutation of CVProfile — that orchestration
 * stays in CvProfileExtractionService.
 */
public interface CvProfileExtractionAdapter {

    AdapterType type();

    /**
     * @throws CvProfileGenerationException if extraction could not be completed
     */
    CvProfileExtractionResult extract(ResolvedProviderConfig config, CvProfileExtractionInput input);
}
