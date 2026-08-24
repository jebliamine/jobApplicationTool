package de.jeb.japp.generation.service.provider.anthropic;

import de.jeb.japp.ai.service.ResolvedProviderConfig;
import de.jeb.japp.commons.exceptions.generation.CvProfileGenerationException;
import de.jeb.japp.generation.service.provider.CvProfileExtractionAdapter;
import de.jeb.japp.generation.service.provider.CvProfileExtractionInput;
import de.jeb.japp.generation.service.provider.CvProfileExtractionResult;
import de.jeb.japp.generation.service.provider.CvProfilePromptBuilder;
import de.jeb.japp.generation.service.provider.CvProfileResponseParser;
import de.jeb.japp.model.ai.AdapterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.client.*;

/**
 * Calls the same Anthropic /v1/messages endpoint as
 * {@link AnthropicMessagesGenerationAdapter}, reusing its request/response
 * wire types (hence living in this package). See
 * {@link de.jeb.japp.generation.service.provider.openai.OpenAiCvProfileExtractionAdapter}
 * for why the retry policy is simpler here than the cover-letter adapters.
 */
public class AnthropicCvProfileExtractionAdapter implements CvProfileExtractionAdapter {

    private static final Logger log = LoggerFactory.getLogger(AnthropicCvProfileExtractionAdapter.class);
    private static final String MESSAGES_PATH = "/v1/messages";
    private static final String ANTHROPIC_VERSION = "2023-06-01";
    private static final long RETRY_DELAY_MILLIS = 750;

    private final RestClient restClient;

    public AnthropicCvProfileExtractionAdapter(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public AdapterType type() {
        return AdapterType.ANTHROPIC_MESSAGES;
    }

    @Override
    public CvProfileExtractionResult extract(ResolvedProviderConfig config, CvProfileExtractionInput input) {
        if (!config.isAvailable()) {
            throw new CvProfileGenerationException("This Anthropic instance is not configured or is currently disabled.");
        }
        if (input.cvText() == null || input.cvText().isBlank()) {
            throw new CvProfileGenerationException("This CV has no extracted text to generate a profile from.");
        }

        String prompt = CvProfilePromptBuilder.build(input);
        AnthropicMessagesResponse response = call(config, prompt, true);

        String text = response != null ? response.firstText() : null;
        if (text == null || text.isBlank()) {
            throw new CvProfileGenerationException("Anthropic returned an empty response.");
        }

        return CvProfileResponseParser.parse(text);
    }

    private AnthropicMessagesResponse call(ResolvedProviderConfig resolved, String prompt, boolean allowRetry) {
        try {
            return restClient.post()
                    .uri(resolved.getBaseUrl() + MESSAGES_PATH)
                    .header("x-api-key", resolved.getApiKey())
                    .header("anthropic-version", ANTHROPIC_VERSION)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(AnthropicMessagesRequest.ofPrompt(resolved.getModel(), prompt))
                    .retrieve()
                    .body(AnthropicMessagesResponse.class);
        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden e) {
            log.warn("Anthropic CV-profile authentication error: status={}, model={}", e.getStatusCode().value(), resolved.getModel());
            throw new CvProfileGenerationException(
                    "Anthropic rejected the request due to an authentication problem (HTTP " + e.getStatusCode().value() + ").");
        } catch (HttpClientErrorException.TooManyRequests | HttpServerErrorException e) {
            if (!allowRetry) {
                log.warn("Anthropic CV-profile error (giving up): status={}, model={}", e.getStatusCode().value(), resolved.getModel());
                throw new CvProfileGenerationException(
                        "Anthropic is temporarily unavailable (HTTP " + e.getStatusCode().value() + "). Please try again later.");
            }
            log.warn("Anthropic CV-profile error (retrying once): status={}, model={}", e.getStatusCode().value(), resolved.getModel());
            sleep();
            return call(resolved, prompt, false);
        } catch (HttpClientErrorException e) {
            throw new CvProfileGenerationException("Anthropic rejected the request (HTTP " + e.getStatusCode().value() + ").");
        } catch (ResourceAccessException e) {
            throw new CvProfileGenerationException("Could not reach Anthropic (timeout or connection failure).");
        } catch (RestClientException e) {
            throw new CvProfileGenerationException("Anthropic returned an unexpected response.");
        }
    }

    private void sleep() {
        try {
            Thread.sleep(RETRY_DELAY_MILLIS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CvProfileGenerationException("Anthropic call was interrupted while retrying.");
        }
    }
}
