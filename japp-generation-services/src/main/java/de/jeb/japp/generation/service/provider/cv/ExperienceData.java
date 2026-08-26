package de.jeb.japp.generation.service.provider.cv;

/**
 * One work-experience entry as extracted by an LLM — dates are plain
 * ISO-8601 strings (or null) here since the model may return an unparsable
 * value; CvProfileExtractionService is responsible for turning these into
 * LocalDate, tolerating a bad value rather than failing the whole extraction.
 */
public record ExperienceData(String company, String title, String startDate, String endDate, String description) {
}
