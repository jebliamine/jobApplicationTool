package de.jeb.japp.generation.service.provider;

/**
 * Everything a JobExtractionAdapter needs to extract a structured job posting from pasted text —
 * plain values only, same decoupling rationale as {@link CvProfileExtractionInput}.
 */
public record JobExtractionInput(String rawText) {
}
