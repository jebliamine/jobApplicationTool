package de.jeb.japp.generation.service.provider.openai;

import de.jeb.japp.ai.service.ResolvedProviderConfig;
import de.jeb.japp.commons.exceptions.generation.CvProfileGenerationException;
import de.jeb.japp.generation.service.provider.cv.*;
import de.jeb.japp.model.ai.AdapterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.*;

/**
 * Calls the same OpenAI-compatible /chat/completions endpoint as
 * {@link OpenAiCompatibleGenerationAdapter}, reusing its request/response
 * wire types (hence living in this package — they're package-private by
 * design), but for CV-profile extraction instead of a cover letter. A single
 * retry on 429/5xx is enough here: this is a user-triggered, on-demand
 * action (unlike cover-letter generation there's no multi-step workflow
 * riding on it), so the simpler policy is a deliberate scope reduction, not
 * an oversight.
 */
public class OpenAiCvProfileExtractionAdapter implements CvProfileExtractionAdapter {

    private static final Logger log = LoggerFactory.getLogger(OpenAiCvProfileExtractionAdapter.class);
    private static final String CHAT_COMPLETIONS_PATH = "/chat/completions";
    private static final long RETRY_DELAY_MILLIS = 750;

    private final RestClient restClient;

    public OpenAiCvProfileExtractionAdapter(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public AdapterType type() {
        return AdapterType.OPENAI_COMPATIBLE;
    }

    @Override
    public CvProfileExtractionResult extract(ResolvedProviderConfig config, CvProfileExtractionInput input) {
        if (!config.isAvailable()) {
            throw new CvProfileGenerationException("This provider instance is not configured or is currently disabled.");
        }
        if (input.cvText() == null || input.cvText().isBlank()) {
            throw new CvProfileGenerationException("This CV has no extracted text to generate a profile from.");
        }

        String prompt = CvProfilePromptBuilder.build(input);
        OpenAiChatCompletionsResponse response = call(config, prompt, true);

        String text = response != null ? response.firstText() : null;
        if (text == null || text.isBlank()) {
            throw new CvProfileGenerationException("The provider returned an empty response.");
        }

        return CvProfileResponseParser.parse(text);
    }

    private OpenAiChatCompletionsResponse call(ResolvedProviderConfig resolved, String prompt, boolean allowRetry) {
        try {
            RestClient.RequestBodySpec request = restClient.post()
                    .uri(resolved.getBaseUrl() + CHAT_COMPLETIONS_PATH)
                    .contentType(MediaType.APPLICATION_JSON);
            if (resolved.getApiKey() != null && !resolved.getApiKey().isBlank()) {
                request = request.header(HttpHeaders.AUTHORIZATION, "Bearer " + resolved.getApiKey());
            }
            return request
                    .body(OpenAiChatCompletionsRequest.ofPrompt(resolved.getModel(), prompt))
                    .retrieve()
                    .body(OpenAiChatCompletionsResponse.class);
        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden e) {
            log.warn("OpenAI-compatible CV-profile authentication error: status={}, model={}", e.getStatusCode().value(), resolved.getModel());
            throw new CvProfileGenerationException(
                    "The provider rejected the request due to an authentication problem (HTTP " + e.getStatusCode().value() + ").");
        } catch (HttpClientErrorException.TooManyRequests | HttpServerErrorException e) {
            if (!allowRetry) {
                log.warn("OpenAI-compatible CV-profile error (giving up): status={}, model={}", e.getStatusCode().value(), resolved.getModel());
                throw new CvProfileGenerationException(
                        "The provider is temporarily unavailable (HTTP " + e.getStatusCode().value() + "). Please try again later.");
            }
            log.warn("OpenAI-compatible CV-profile error (retrying once): status={}, model={}", e.getStatusCode().value(), resolved.getModel());
            sleep();
            return call(resolved, prompt, false);
        } catch (HttpClientErrorException e) {
            throw new CvProfileGenerationException("The provider rejected the request (HTTP " + e.getStatusCode().value() + ").");
        } catch (ResourceAccessException e) {
            throw new CvProfileGenerationException("Could not reach the provider (timeout or connection failure).");
        } catch (RestClientException e) {
            throw new CvProfileGenerationException("The provider returned an unexpected response.");
        }
    }

    private void sleep() {
        try {
            Thread.sleep(RETRY_DELAY_MILLIS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CvProfileGenerationException("The provider call was interrupted while retrying.");
        }
    }
}
