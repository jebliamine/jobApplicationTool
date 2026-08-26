package de.jeb.japp.generation.service.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.jeb.japp.commons.exceptions.job.JobExtractionException;

import java.util.regex.Pattern;

/**
 * Parses a model's raw text response into a {@link JobExtractionResult}. Shared by every
 * JobExtractionAdapter — same defensive code-fence stripping as {@link CvProfileResponseParser}.
 */
public final class JobExtractionResponseParser {

    private static final Pattern CODE_FENCE = Pattern.compile("^```(?:json)?\\s*|\\s*```$");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JobExtractionResponseParser() {
    }

    /**
     * @throws JobExtractionException if the text isn't valid JSON matching the expected shape
     */
    public static JobExtractionResult parse(String rawText) {
        String json = CODE_FENCE.matcher(rawText.trim()).replaceAll("").trim();
        try {
            return MAPPER.readValue(json, JobExtractionResult.class);
        } catch (Exception e) {
            throw new JobExtractionException("The provider's response could not be parsed as a job posting.");
        }
    }
}
