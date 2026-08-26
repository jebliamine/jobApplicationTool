package de.jeb.japp.generation.service.provider.cv;

import de.jeb.japp.generation.service.provider.GenerationInput;

/**
 * Everything a CvProfileExtractionAdapter needs to extract a structured
 * profile from a CV — plain values only, same decoupling rationale as
 * {@link GenerationInput}.
 */
public record CvProfileExtractionInput(String cvText) {
}
