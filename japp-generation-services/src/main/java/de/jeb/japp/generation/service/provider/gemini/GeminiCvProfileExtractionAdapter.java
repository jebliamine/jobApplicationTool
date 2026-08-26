package de.jeb.japp.generation.service.provider.gemini;

import de.jeb.japp.ai.service.ResolvedProviderConfig;
import de.jeb.japp.commons.exceptions.generation.CvProfileGenerationException;
import de.jeb.japp.generation.service.provider.cv.*;
import de.jeb.japp.model.ai.AdapterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.client.*;

/**
 * Calls the same Gemini generateContent endpoint as
 * {@link GeminiGenerateContentAdapter}, reusing its request/response wire
 * types (hence living in this package). See
 * {@link de.jeb.japp.generation.service.provider.openai.OpenAiCvProfileExtractionAdapter}
 * for why the retry policy is simpler here than the cover-letter adapters.
 */
public class GeminiCvProfileExtractionAdapter implements CvProfileExtractionAdapter {

    private static final Logger log = LoggerFactory.getLogger(GeminiCvProfileExtractionAdapter.class);
    private static final String GENERATE_CONTENT_PATH = "/v1beta/models/{model}:generateContent";
    private static final long RETRY_DELAY_MILLIS = 750;

    private final RestClient restClient;

    public GeminiCvProfileExtractionAdapter(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public AdapterType type() {
        return AdapterType.GEMINI_GENERATE_CONTENT;
    }

    @Override
    public CvProfileExtractionResult extract(ResolvedProviderConfig config, CvProfileExtractionInput input) {
        if (!config.isAvailable()) {
            throw new CvProfileGenerationException("This Gemini instance is not configured or is currently disabled.");
        }
        if (input.cvText() == null || input.cvText().isBlank()) {
            throw new CvProfileGenerationException("This CV has no extracted text to generate a profile from.");
        }

        String prompt = CvProfilePromptBuilder.build(input);
        GeminiGenerateContentResponse response = call(config, prompt, true);

        String text = response != null ? response.firstText() : null;
        if (text == null || text.isBlank()) {
            throw new CvProfileGenerationException("Gemini returned an empty response.");
        }

        return CvProfileResponseParser.parse(text);
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
            log.warn("Gemini CV-profile authentication error: status={}, model={}", e.getStatusCode().value(), resolved.getModel());
            throw new CvProfileGenerationException(
                    "Gemini rejected the request due to an authentication problem (HTTP " + e.getStatusCode().value() + ").");
        } catch (HttpClientErrorException.TooManyRequests | HttpServerErrorException e) {
            if (!allowRetry) {
                log.warn("Gemini CV-profile error (giving up): status={}, model={}", e.getStatusCode().value(), resolved.getModel());
                throw new CvProfileGenerationException(
                        "Gemini is temporarily unavailable (HTTP " + e.getStatusCode().value() + "). Please try again later.");
            }
            log.warn("Gemini CV-profile error (retrying once): status={}, model={}", e.getStatusCode().value(), resolved.getModel());
            sleep();
            return call(resolved, prompt, false);
        } catch (HttpClientErrorException e) {
            throw new CvProfileGenerationException("Gemini rejected the request (HTTP " + e.getStatusCode().value() + ").");
        } catch (ResourceAccessException e) {
            throw new CvProfileGenerationException("Could not reach Gemini (timeout or connection failure).");
        } catch (RestClientException e) {
            throw new CvProfileGenerationException("Gemini returned an unexpected response.");
        }
    }

    private void sleep() {
        try {
            Thread.sleep(RETRY_DELAY_MILLIS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CvProfileGenerationException("Gemini call was interrupted while retrying.");
        }
    }
}
