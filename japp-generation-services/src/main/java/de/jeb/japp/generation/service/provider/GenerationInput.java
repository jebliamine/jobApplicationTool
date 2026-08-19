package de.jeb.japp.generation.service.provider;

/**
 * Everything a {@link CoverLetterGenerationProvider} needs to generate a
 * cover letter — plain values only, deliberately decoupled from the
 * GenerationRequest/Job/CVDocument/User JPA entities so a provider
 * implementation never depends on persistence or domain types.
 *
 * {@code cvTitle} is nullable: the current system does not extract CV text,
 * so a CV's title is the only CV information available to generate from
 * (matching the previous placeholder behavior); {@code cvTitle} is absent
 * whenever no CV was resolved for the request.
 */
public record GenerationInput(
        String jobTitle,
        String companyName,
        String jobDescription,
        String cvTitle,
        String applicantName
) {
}
