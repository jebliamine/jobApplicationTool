package de.jeb.japp.generation.service.provider;

import de.jeb.japp.ai.service.ResolvedProviderConfig;
import de.jeb.japp.commons.exceptions.generation.CoverLetterGenerationException;
import de.jeb.japp.model.ai.AdapterType;

/**
 * Generates cover-letter content for one wire protocol. Implementations must
 * not perform authorization, touch repositories, know about HTTP status
 * codes/DTOs, or mutate GenerationRequest/CoverLetter — that orchestration
 * stays in GenerationRequestService. Each implementation self-registers
 * under an {@link AdapterType} (see {@link CoverLetterGenerationAdapterRegistry}).
 * <p>
 * Unlike the old one-class-per-real-world-provider model, an adapter is a
 * protocol implementation, not a single admin-facing "provider" — many
 * differently-configured {@code AiProviderConfiguration} instances (different
 * accounts, models, base URLs) can share one adapter, since the resolved
 * configuration for the specific instance is passed in at call time rather
 * than looked up internally. Adding a new admin-configurable provider
 * instance therefore never requires a new adapter class; a new adapter class
 * is only needed for a genuinely new wire protocol.
 */
public interface CoverLetterGenerationAdapter {

    /**
     * The adapter type this implementation handles.
     */
    AdapterType type();

    /**
     * @throws CoverLetterGenerationException if generation could not be completed
     */
    GenerationResult generate(ResolvedProviderConfig config, GenerationInput input);
}
