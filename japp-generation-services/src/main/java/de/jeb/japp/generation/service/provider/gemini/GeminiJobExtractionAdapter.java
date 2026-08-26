package de.jeb.japp.generation.service.provider.gemini;

import de.jeb.japp.ai.service.ResolvedProviderConfig;
import de.jeb.japp.commons.exceptions.job.JobExtractionException;
import de.jeb.japp.generation.service.provider.JobExtractionAdapter;
import de.jeb.japp.generation.service.provider.JobExtractionInput;
import de.jeb.japp.generation.service.provider.JobExtractionPromptBuilder;
import de.jeb.japp.generation.service.provider.JobExtractionResponseParser;
import de.jeb.japp.generation.service.provider.JobExtractionResult;
import de.jeb.japp.model.ai.AdapterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.client.*;

/**
 * Calls the same Gemini generateContent endpoint as {@link GeminiGenerateContentAdapter}, reusing
 * its request/response wire types (hence living in this package), for job-posting extraction. See
 * {@link de.jeb.japp.generation.service.provider.openai.OpenAiJobExtractionAdapter} for the retry
 * policy rationale.
 */
public class GeminiJobExtractionAdapter implements JobExtractionAdapter {

    private static final Logger log = LoggerFactory.getLogger(GeminiJobExtractionAdapter.class);
    private static final String GENERATE_CONTENT_PATH = "/v1beta/models/{model}:generateContent";
    private static final long RETRY_DELAY_MILLIS = 750;

    private final RestClient restClient;

    public GeminiJobExtractionAdapter(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public AdapterType type() {
        return AdapterType.GEMINI_GENERATE_CONTENT;
    }

    @Override
    public JobExtractionResult extract(ResolvedProviderConfig config, JobExtractionInput input) {
        if (!config.isAvailable()) {
            throw new JobExtractionException("This Gemini instance is not configured or is currently disabled.");
        }
        if (input.rawText() == null || input.rawText().isBlank()) {
            throw new JobExtractionException("There is no job posting text to extract from.");
        }

        String prompt = JobExtractionPromptBuilder.build(input);
        GeminiGenerateContentResponse response = call(config, prompt, true);

        String text = response != null ? response.firstText() : null;
        if (text == null || text.isBlank()) {
            throw new JobExtractionException("Gemini returned an empty response.");
        }

        return JobExtractionResponseParser.parse(text);
    }

    private GeminiGenerateContentResponse call(ResolvedProviderConfig resolved, String prompt, boolean allowRetry) {
        try {
            return restClient.post()
                    .uri(resolved.getBaseUrl() + GENERATE_CONTENT_PATH, resolved.getModel())
                    .header("x-goog-api-key", resolved.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(GeminiGenerateContentRequest.ofPrompt(prompt))
                    .retrieve()
                    .body(GeminiGenerateContentResponse.class);
        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden e) {
            log.warn("Gemini job-extraction authentication error: status={}, model={}", e.getStatusCode().value(), resolved.getModel());
            throw new JobExtractionException(
                    "Gemini rejected the request due to an authentication problem (HTTP " + e.getStatusCode().value() + ").");
        } catch (HttpClientErrorException.TooManyRequests | HttpServerErrorException e) {
            if (!allowRetry) {
                log.warn("Gemini job-extraction error (giving up): status={}, model={}", e.getStatusCode().value(), resolved.getModel());
                throw new JobExtractionException(
                        "Gemini is temporarily unavailable (HTTP " + e.getStatusCode().value() + "). Please try again later.");
            }
            log.warn("Gemini job-extraction error (retrying once): status={}, model={}", e.getStatusCode().value(), resolved.getModel());
            sleep();
            return call(resolved, prompt, false);
        } catch (HttpClientErrorException e) {
            throw new JobExtractionException("Gemini rejected the request (HTTP " + e.getStatusCode().value() + ").");
        } catch (ResourceAccessException e) {
            throw new JobExtractionException("Could not reach Gemini (timeout or connection failure).");
        } catch (RestClientException e) {
            throw new JobExtractionException("Gemini returned an unexpected response.");
        }
    }

    private void sleep() {
        try {
            Thread.sleep(RETRY_DELAY_MILLIS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new JobExtractionException("Gemini call was interrupted while retrying.");
        }
    }
}
