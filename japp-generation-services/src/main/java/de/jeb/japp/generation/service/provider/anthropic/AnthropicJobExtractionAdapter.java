package de.jeb.japp.generation.service.provider.anthropic;

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
 * Calls the same Anthropic /v1/messages endpoint as {@link AnthropicMessagesGenerationAdapter},
 * reusing its request/response wire types (hence living in this package), for job-posting
 * extraction. See {@link de.jeb.japp.generation.service.provider.openai.OpenAiJobExtractionAdapter}
 * for the retry policy rationale.
 */
public class AnthropicJobExtractionAdapter implements JobExtractionAdapter {

    private static final Logger log = LoggerFactory.getLogger(AnthropicJobExtractionAdapter.class);
    private static final String MESSAGES_PATH = "/v1/messages";
    private static final String ANTHROPIC_VERSION = "2023-06-01";
    private static final long RETRY_DELAY_MILLIS = 750;

    private final RestClient restClient;

    public AnthropicJobExtractionAdapter(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public AdapterType type() {
        return AdapterType.ANTHROPIC_MESSAGES;
    }

    @Override
    public JobExtractionResult extract(ResolvedProviderConfig config, JobExtractionInput input) {
        if (!config.isAvailable()) {
            throw new JobExtractionException("This Anthropic instance is not configured or is currently disabled.");
        }
        if (input.rawText() == null || input.rawText().isBlank()) {
            throw new JobExtractionException("There is no job posting text to extract from.");
        }

        String prompt = JobExtractionPromptBuilder.build(input);
        AnthropicMessagesResponse response = call(config, prompt, true);

        String text = response != null ? response.firstText() : null;
        if (text == null || text.isBlank()) {
            throw new JobExtractionException("Anthropic returned an empty response.");
        }

        return JobExtractionResponseParser.parse(text);
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
            log.warn("Anthropic job-extraction authentication error: status={}, model={}", e.getStatusCode().value(), resolved.getModel());
            throw new JobExtractionException(
                    "Anthropic rejected the request due to an authentication problem (HTTP " + e.getStatusCode().value() + ").");
        } catch (HttpClientErrorException.TooManyRequests | HttpServerErrorException e) {
            if (!allowRetry) {
                log.warn("Anthropic job-extraction error (giving up): status={}, model={}", e.getStatusCode().value(), resolved.getModel());
                throw new JobExtractionException(
                        "Anthropic is temporarily unavailable (HTTP " + e.getStatusCode().value() + "). Please try again later.");
            }
            log.warn("Anthropic job-extraction error (retrying once): status={}, model={}", e.getStatusCode().value(), resolved.getModel());
            sleep();
            return call(resolved, prompt, false);
        } catch (HttpClientErrorException e) {
            throw new JobExtractionException("Anthropic rejected the request (HTTP " + e.getStatusCode().value() + ").");
        } catch (ResourceAccessException e) {
            throw new JobExtractionException("Could not reach Anthropic (timeout or connection failure).");
        } catch (RestClientException e) {
            throw new JobExtractionException("Anthropic returned an unexpected response.");
        }
    }

    private void sleep() {
        try {
            Thread.sleep(RETRY_DELAY_MILLIS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new JobExtractionException("Anthropic call was interrupted while retrying.");
        }
    }
}
