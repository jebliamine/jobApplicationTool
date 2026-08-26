package de.jeb.japp.generation.service.provider;

/**
 * The outcome of a successful JobExtractionAdapter call — mirrors the JSON shape requested by
 * {@link JobExtractionPromptBuilder}, so an adapter can deserialize the provider's raw text
 * response directly into this type. employmentType/workMode are raw strings here (an adapter has
 * no knowledge of enums) — JobExtractionService maps them to the real enums, falling back to null
 * on anything unparsable.
 */
public record JobExtractionResult(
        String title,
        String companyName,
        String description,
        String location,
        String employmentType,
        String workMode,
        String salaryRange,
        String url
) {
}
