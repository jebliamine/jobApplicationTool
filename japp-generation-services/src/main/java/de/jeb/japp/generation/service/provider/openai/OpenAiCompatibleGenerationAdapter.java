package de.jeb.japp.generation.service.provider.openai;

import de.jeb.japp.ai.service.ResolvedProviderConfig;
import de.jeb.japp.commons.exceptions.generation.CoverLetterGenerationException;
import de.jeb.japp.generation.service.provider.CoverLetterGenerationAdapter;
import de.jeb.japp.generation.service.provider.CoverLetterPromptBuilder;
import de.jeb.japp.generation.service.provider.GenerationInput;
import de.jeb.japp.generation.service.provider.GenerationResult;
import de.jeb.japp.model.ai.AdapterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.*;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Calls an OpenAI-compatible {@code /chat/completions} REST API over HTTP(S) — the same wire
 * format used by OpenAI itself and by virtually every self-hosted/local LLM server (Ollama, LM
 * Studio, vLLM, text-generation-webui, llama.cpp server), since they all implement this as their
 * standard compatibility layer. One adapter, unlimited admin-configured instances — a local
 * server and a real OpenAI account both go through this exact class.
 * <p>
 * Same defensive posture as the Gemini adapter: every failure mode is converted to a
 * {@link CoverLetterGenerationException}, the API key (when present) is never logged, and
 * HTTP 429/5xx are retried with short exponential backoff + jitter.
 */
public class OpenAiCompatibleGenerationAdapter implements CoverLetterGenerationAdapter {

    private static final Logger log = LoggerFactory.getLogger(OpenAiCompatibleGenerationAdapter.class);
    private static final String CHAT_COMPLETIONS_PATH = "/chat/completions";
    private static final int MAX_LOGGED_ERROR_BODY_LENGTH = 300;

    private static final int MAX_ATTEMPTS = 3;
    private static final long RETRY_1_BASE_DELAY_MILLIS = 500;
    private static final long RETRY_1_JITTER_MILLIS = 250;
    private static final long RETRY_2_BASE_DELAY_MILLIS = 1500;
    private static final long RETRY_2_JITTER_MILLIS = 500;

    private final RestClient restClient;

    public OpenAiCompatibleGenerationAdapter(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public AdapterType type() {
        return AdapterType.OPENAI_COMPATIBLE;
    }

    @Override
    public GenerationResult generate(ResolvedProviderConfig config, GenerationInput input) {
        if (!config.isAvailable()) {
            throw new CoverLetterGenerationException("This provider instance is not configured or is currently disabled.");
        }
        if (input.jobDescription() == null || input.jobDescription().isBlank()) {
            throw new CoverLetterGenerationException("The selected job has no description to generate from.");
        }

        String prompt = CoverLetterPromptBuilder.build(input);
        OpenAiChatCompletionsResponse response = call(config, prompt);

        String text = response != null ? response.firstText() : null;
        if (text == null || text.isBlank()) {
            throw new CoverLetterGenerationException("The provider returned an empty response.");
        }

        return new GenerationResult(text.trim());
    }

    private OpenAiChatCompletionsResponse call(ResolvedProviderConfig resolved, String prompt) {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
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
                logFailure(resolved, e, "authentication");
                throw new CoverLetterGenerationException(
                        "The provider rejected the request due to an authentication problem (HTTP " + e.getStatusCode().value() + ").");
            } catch (HttpClientErrorException.TooManyRequests e) {
                if (attempt >= MAX_ATTEMPTS) {
                    logFailure(resolved, e, "rate limit");
                    throw new CoverLetterGenerationException(
                            "The provider's rate limit was exceeded (HTTP " + e.getStatusCode().value() + "). Please try again later.");
                }
                waitBeforeRetry(e, "rate limit", attempt);
            } catch (HttpServerErrorException e) {
                if (attempt >= MAX_ATTEMPTS) {
                    logFailure(resolved, e, "server");
                    throw new CoverLetterGenerationException(
                            "The provider is currently unavailable (HTTP " + e.getStatusCode().value() + "). Please try again later.");
                }
                waitBeforeRetry(e, "server", attempt);
            } catch (HttpClientErrorException e) {
                logFailure(resolved, e, "client");
                throw new CoverLetterGenerationException("The provider rejected the request (HTTP " + e.getStatusCode().value() + ").");
            } catch (ResourceAccessException e) {
                log.warn("OpenAI-compatible call failed (timeout or connection failure): model={}, baseUrl={}",
                        resolved.getModel(), resolved.getBaseUrl());
                throw new CoverLetterGenerationException("Could not reach the provider (timeout or connection failure).");
            } catch (RestClientException e) {
                log.warn("OpenAI-compatible call failed (unexpected response): model={}, baseUrl={}",
                        resolved.getModel(), resolved.getBaseUrl());
                throw new CoverLetterGenerationException("The provider returned an unexpected response.");
            }
        }
        throw new CoverLetterGenerationException("The provider returned an unexpected response.");
    }

    private void waitBeforeRetry(HttpStatusCodeException e, String category, int attempt) {
        long delayMillis = retryDelayMillis(attempt);
        log.warn("OpenAI-compatible {} error (retrying): status={}, attempt={}, retryDelayMs={}",
                category, e.getStatusCode().value(), attempt, delayMillis);
        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new CoverLetterGenerationException("The provider call was interrupted while retrying.");
        }
    }

    private long retryDelayMillis(int failedAttempt) {
        long base = failedAttempt == 1 ? RETRY_1_BASE_DELAY_MILLIS : RETRY_2_BASE_DELAY_MILLIS;
        long jitterRange = failedAttempt == 1 ? RETRY_1_JITTER_MILLIS : RETRY_2_JITTER_MILLIS;
        return base + ThreadLocalRandom.current().nextLong(jitterRange + 1);
    }

    private void logFailure(ResolvedProviderConfig resolved, HttpStatusCodeException e, String category) {
        log.warn("OpenAI-compatible {} error: status={}, model={}, baseUrl={}, reason={}",
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
