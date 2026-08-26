package de.jeb.japp.generation.service.provider.openai;

import de.jeb.japp.ai.service.ResolvedProviderConfig;
import de.jeb.japp.commons.exceptions.job.JobExtractionException;
import de.jeb.japp.generation.service.provider.job.*;
import de.jeb.japp.model.ai.AdapterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.*;

/**
 * Calls the same OpenAI-compatible /chat/completions endpoint as
 * {@link OpenAiCompatibleGenerationAdapter}, reusing its request/response wire types (hence living
 * in this package — they're package-private by design), for job-posting extraction. Single retry
 * on 429/5xx, same simplified policy as {@link OpenAiCvProfileExtractionAdapter}.
 */
public class OpenAiJobExtractionAdapter implements JobExtractionAdapter {

    private static final Logger log = LoggerFactory.getLogger(OpenAiJobExtractionAdapter.class);
    private static final String CHAT_COMPLETIONS_PATH = "/chat/completions";
    private static final long RETRY_DELAY_MILLIS = 750;

    private final RestClient restClient;

    public OpenAiJobExtractionAdapter(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public AdapterType type() {
        return AdapterType.OPENAI_COMPATIBLE;
    }

    @Override
    public JobExtractionResult extract(ResolvedProviderConfig config, JobExtractionInput input) {
        if (!config.isAvailable()) {
            throw new JobExtractionException("This provider instance is not configured or is currently disabled.");
        }
        if (input.rawText() == null || input.rawText().isBlank()) {
            throw new JobExtractionException("There is no job posting text to extract from.");
        }

        String prompt = JobExtractionPromptBuilder.build(input);
        OpenAiChatCompletionsResponse response = call(config, prompt, true);

        String text = response != null ? response.firstText() : null;
        if (text == null || text.isBlank()) {
            throw new JobExtractionException("The provider returned an empty response.");
        }

        return JobExtractionResponseParser.parse(text);
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
            log.warn("OpenAI-compatible job-extraction authentication error: status={}, model={}", e.getStatusCode().value(), resolved.getModel());
            throw new JobExtractionException(
                    "The provider rejected the request due to an authentication problem (HTTP " + e.getStatusCode().value() + ").");
        } catch (HttpClientErrorException.TooManyRequests | HttpServerErrorException e) {
            if (!allowRetry) {
                log.warn("OpenAI-compatible job-extraction error (giving up): status={}, model={}", e.getStatusCode().value(), resolved.getModel());
                throw new JobExtractionException(
                        "The provider is temporarily unavailable (HTTP " + e.getStatusCode().value() + "). Please try again later.");
            }
            log.warn("OpenAI-compatible job-extraction error (retrying once): status={}, model={}", e.getStatusCode().value(), resolved.getModel());
            sleep();
            return call(resolved, prompt, false);
        } catch (HttpClientErrorException e) {
            throw new JobExtractionException("The provider rejected the request (HTTP " + e.getStatusCode().value() + ").");
        } catch (ResourceAccessException e) {
            throw new JobExtractionException("Could not reach the provider (timeout or connection failure).");
        } catch (RestClientException e) {
            throw new JobExtractionException("The provider returned an unexpected response.");
        }
    }

    private void sleep() {
        try {
            Thread.sleep(RETRY_DELAY_MILLIS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new JobExtractionException("The provider call was interrupted while retrying.");
        }
    }
}
