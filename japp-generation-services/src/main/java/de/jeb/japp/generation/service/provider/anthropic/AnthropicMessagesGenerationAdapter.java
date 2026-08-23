package de.jeb.japp.generation.service.provider.anthropic;

import de.jeb.japp.ai.service.ResolvedProviderConfig;
import de.jeb.japp.commons.exceptions.generation.CoverLetterGenerationException;
import de.jeb.japp.generation.service.provider.CoverLetterGenerationAdapter;
import de.jeb.japp.generation.service.provider.CoverLetterPromptBuilder;
import de.jeb.japp.generation.service.provider.GenerationInput;
import de.jeb.japp.generation.service.provider.GenerationResult;
import de.jeb.japp.model.ai.AdapterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.client.*;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Calls Anthropic's {@code /v1/messages} REST API over HTTPS — auth via {@code x-api-key} +
 * {@code anthropic-version} headers (Anthropic's own scheme, distinct from OpenAI's Bearer token
 * and Gemini's {@code x-goog-api-key}). Same defensive posture as the other adapters: every
 * failure mode is converted to a {@link CoverLetterGenerationException}, the API key is never
 * logged, and HTTP 429/5xx are retried with short exponential backoff + jitter.
 */
public class AnthropicMessagesGenerationAdapter implements CoverLetterGenerationAdapter {

    private static final Logger log = LoggerFactory.getLogger(AnthropicMessagesGenerationAdapter.class);
    private static final String MESSAGES_PATH = "/v1/messages";
    private static final String ANTHROPIC_VERSION = "2023-06-01";
    private static final int MAX_LOGGED_ERROR_BODY_LENGTH = 300;

    private static final int MAX_ATTEMPTS = 3;
    private static final long RETRY_1_BASE_DELAY_MILLIS = 500;
    private static final long RETRY_1_JITTER_MILLIS = 250;
    private static final long RETRY_2_BASE_DELAY_MILLIS = 1500;
    private static final long RETRY_2_JITTER_MILLIS = 500;

    private final RestClient restClient;

    public AnthropicMessagesGenerationAdapter(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public AdapterType type() {
        return AdapterType.ANTHROPIC_MESSAGES;
    }

    @Override
    public GenerationResult generate(ResolvedProviderConfig config, GenerationInput input) {
        if (!config.isAvailable()) {
            throw new CoverLetterGenerationException("This Anthropic instance is not configured or is currently disabled.");
        }
        if (input.jobDescription() == null || input.jobDescription().isBlank()) {
            throw new CoverLetterGenerationException("The selected job has no description to generate from.");
        }

        String prompt = CoverLetterPromptBuilder.build(input);
        AnthropicMessagesResponse response = call(config, prompt);

        String text = response != null ? response.firstText() : null;
        if (text == null || text.isBlank()) {
            throw new CoverLetterGenerationException("Anthropic returned an empty response.");
        }

        return new GenerationResult(text.trim());
    }

    private AnthropicMessagesResponse call(ResolvedProviderConfig resolved, String prompt) {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
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
                logFailure(resolved, e, "authentication");
                throw new CoverLetterGenerationException(
                        "Anthropic rejected the request due to an authentication problem (HTTP " + e.getStatusCode().value() + ").");
            } catch (HttpClientErrorException.TooManyRequests e) {
                if (attempt >= MAX_ATTEMPTS) {
                    logFailure(resolved, e, "rate limit");
                    throw new CoverLetterGenerationException(
                            "Anthropic's rate limit was exceeded (HTTP " + e.getStatusCode().value() + "). Please try again later.");
                }
                waitBeforeRetry(e, "rate limit", attempt);
            } catch (HttpServerErrorException e) {
                if (attempt >= MAX_ATTEMPTS) {
                    logFailure(resolved, e, "server");
                    throw new CoverLetterGenerationException(
                            "Anthropic is currently unavailable (HTTP " + e.getStatusCode().value() + "). Please try again later.");
                }
                waitBeforeRetry(e, "server", attempt);
            } catch (HttpClientErrorException e) {
                logFailure(resolved, e, "client");
                throw new CoverLetterGenerationException("Anthropic rejected the request (HTTP " + e.getStatusCode().value() + ").");
            } catch (ResourceAccessException e) {
                log.warn("Anthropic call failed (timeout or connection failure): model={}, baseUrl={}",
                        resolved.getModel(), resolved.getBaseUrl());
                throw new CoverLetterGenerationException("Could not reach Anthropic (timeout or connection failure).");
            } catch (RestClientException e) {
                log.warn("Anthropic call failed (unexpected response): model={}, baseUrl={}",
                        resolved.getModel(), resolved.getBaseUrl());
                throw new CoverLetterGenerationException("Anthropic returned an unexpected response.");
            }
        }
        throw new CoverLetterGenerationException("Anthropic returned an unexpected response.");
    }

    private void waitBeforeRetry(HttpStatusCodeException e, String category, int attempt) {
        long delayMillis = retryDelayMillis(attempt);
        log.warn("Anthropic {} error (retrying): status={}, attempt={}, retryDelayMs={}",
                category, e.getStatusCode().value(), attempt, delayMillis);
        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new CoverLetterGenerationException("Anthropic call was interrupted while retrying.");
        }
    }

    private long retryDelayMillis(int failedAttempt) {
        long base = failedAttempt == 1 ? RETRY_1_BASE_DELAY_MILLIS : RETRY_2_BASE_DELAY_MILLIS;
        long jitterRange = failedAttempt == 1 ? RETRY_1_JITTER_MILLIS : RETRY_2_JITTER_MILLIS;
        return base + ThreadLocalRandom.current().nextLong(jitterRange + 1);
    }

    private void logFailure(ResolvedProviderConfig resolved, HttpStatusCodeException e, String category) {
        log.warn("Anthropic {} error: status={}, model={}, baseUrl={}, reason={}",
                category, e.getStatusCode().value(), resolved.getModel(), resolved.getBaseUrl(), sanitizedReason(e));
    }

    private String sanitizedReason(HttpStatusCodeException e) {
        String body = e.getResponseBodyAsString();
        if (body == null || body.isBlank()) {
            return "(no response body)";
        }
        return body.length() > MAX_LOGGED_ERROR_BODY_LENGTH ? body.substring(0, MAX_LOGGED_ERROR_BODY_LENGTH) + "…" : body;
    }
}
