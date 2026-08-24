package de.jeb.japp.generation.service.provider;

import java.util.List;

/**
 * The outcome of a successful CvProfileExtractionAdapter call — mirrors the
 * JSON shape requested by {@link CvProfilePromptBuilder}, so an adapter can
 * deserialize the provider's raw text response directly into this type.
 */
public record CvProfileExtractionResult(String fullName, String summary, List<ExperienceData> experiences) {
}
