package de.jeb.japp.generation.service.provider;

import de.jeb.japp.model.generation.GenerationProvider;

/**
 * Generates cover-letter content. Implementations must not perform
 * authorization, touch repositories, know about HTTP status codes/DTOs, or
 * mutate GenerationRequest/CoverLetter — that orchestration stays in
 * GenerationRequestService. Each implementation self-registers under a
 * {@link GenerationProvider} id (see {@link CoverLetterGenerationProviderRegistry}),
 * so adding another provider later (e.g. GROQ, OPENROUTER, OPENAI) never
 * requires changing GenerationRequestService, the registry, the REST API, or
 * Angular — just a new {@code @Service} bean implementing this interface.
 */
public interface CoverLetterGenerationProvider {

    /** The provider id this implementation is registered under. */
    GenerationProvider id();

    /** The model identifier to record on the GenerationRequest for requests handled by this provider. */
    String model();

    /**
     * @throws CoverLetterGenerationException if generation could not be completed
     */
    GenerationResult generate(GenerationInput input);
}
