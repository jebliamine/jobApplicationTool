package de.jeb.japp.generation.service.provider;

/**
 * Everything a {@link CoverLetterGenerationProvider} needs to generate a
 * cover letter — plain values only, deliberately decoupled from the
 * GenerationRequest/Job/CVDocument/User JPA entities so a provider
 * implementation never depends on persistence or domain types.
 *
 * {@code cvText} is the CV's extracted, normalized text (see japp-cv-parser's
 * extraction pipeline) — nullable, since extraction may have failed or the CV
 * may predate the extraction feature. {@code cvTitle} remains available
 * independently as a fallback label for providers to reference when
 * {@code cvText} is absent.
 */
public record GenerationInput(
        String jobTitle,
        String companyName,
        String jobDescription,
        String cvTitle,
        String cvText,
        String applicantName
) {
}
