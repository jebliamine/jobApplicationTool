package de.jeb.japp.generation.service.provider.cv;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.jeb.japp.commons.exceptions.generation.CvProfileGenerationException;

import java.util.regex.Pattern;

/**
 * Parses a model's raw text response into a {@link CvProfileExtractionResult}.
 * Shared by every CvProfileExtractionAdapter — despite the prompt asking for
 * JSON only, models sometimes wrap it in a markdown code fence anyway, so
 * that's stripped defensively before parsing.
 */
public final class CvProfileResponseParser {

    // No MULTILINE: anchors the whole (already-trimmed) response, not individual lines —
    // this only ever strips a single fence wrapping the entire JSON body, never touches
    // triple-backtick text that might legitimately appear inside a field value.
    private static final Pattern CODE_FENCE = Pattern.compile("^```(?:json)?\\s*|\\s*```$");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private CvProfileResponseParser() {
    }

    /**
     * @throws CvProfileGenerationException if the text isn't valid JSON matching the expected shape
     */
    public static CvProfileExtractionResult parse(String rawText) {
        String json = CODE_FENCE.matcher(rawText.trim()).replaceAll("").trim();
        try {
            return MAPPER.readValue(json, CvProfileExtractionResult.class);
        } catch (Exception e) {
            throw new CvProfileGenerationException("The provider's response could not be parsed as a CV profile.");
        }
    }
}
