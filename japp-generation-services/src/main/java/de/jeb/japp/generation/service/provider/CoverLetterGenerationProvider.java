package de.jeb.japp.generation.service.provider;

/**
 * Generates cover-letter content. Implementations must not perform
 * authorization, touch repositories, know about HTTP, or mutate
 * GenerationRequest/CoverLetter — that orchestration stays in
 * GenerationRequestService. Exactly one implementation is registered as a
 * Spring bean today ({@code PlaceholderCoverLetterGenerationProvider}); a
 * real AI-backed implementation can be added later without changing this
 * interface, its caller, the REST API, or Angular.
 */
public interface CoverLetterGenerationProvider {

    /**
     * @throws CoverLetterGenerationException if generation could not be completed
     */
    GenerationResult generate(GenerationInput input);
}
