package de.jeb.japp.generation.service.provider.gemini;

import de.jeb.japp.ai.service.ProviderSettingsResolver;
import de.jeb.japp.ai.service.ResolvedProviderConfig;
import de.jeb.japp.commons.exceptions.generation.CoverLetterGenerationException;
import de.jeb.japp.generation.service.provider.CoverLetterGenerationProvider;
import de.jeb.japp.generation.service.provider.GenerationInput;
import de.jeb.japp.generation.service.provider.GenerationResult;
import de.jeb.japp.model.generation.GenerationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.client.*;

import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Calls Google's Gemini {@code generateContent} REST API over HTTPS — a
 * small, isolated HTTP integration behind the existing
 * {@link CoverLetterGenerationProvider} abstraction. No SDK, no local model,
 * no streaming/chat/RAG. Every failure mode (missing/disabled configuration,
 * auth, rate limit, server error, timeout, connection failure, malformed/empty
 * response) is converted to a {@link CoverLetterGenerationException} —
 * GenerationRequestService never sees a Gemini-specific exception type or
 * the raw API response.
 * <p>
 * Configuration (api key, model, base URL) is resolved through
 * {@link ProviderSettingsResolver} at call time, on every {@link #generate}
 * — never cached in this class, never taken from Spring startup-time
 * properties directly. This is what lets an admin change/enable/disable
 * Gemini and have it take effect without restarting the application. The
 * RestClient itself only carries the connect/read timeout (env-only, not
 * admin-configurable) — see {@link GeminiConfig}; the base URL is applied
 * per-call as an absolute URI since it can vary at runtime.
 */
public class GeminiCoverLetterGenerationProvider implements CoverLetterGenerationProvider {

    private static final Logger log = LoggerFactory.getLogger(GeminiCoverLetterGenerationProvider.class);
    private static final String GENERATE_CONTENT_PATH = "/v1beta/models/{model}:generateContent";
    private static final int MAX_LOGGED_ERROR_BODY_LENGTH = 300;

    /**
     * 1 initial attempt + up to 2 retries, only for HTTP 429/5xx — see callGemini().
     */
    private static final int MAX_ATTEMPTS = 3;
    private static final long RETRY_1_BASE_DELAY_MILLIS = 500;
    private static final long RETRY_1_JITTER_MILLIS = 250;
    private static final long RETRY_2_BASE_DELAY_MILLIS = 1500;
    private static final long RETRY_2_JITTER_MILLIS = 500;
    private static final Pattern ERROR_STATUS_PATTERN = Pattern.compile("\"status\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern ERROR_MESSAGE_PATTERN = Pattern.compile("\"message\"\\s*:\\s*\"([^\"]*)\"");

    private final ProviderSettingsResolver resolver;
    private final RestClient restClient;

    public GeminiCoverLetterGenerationProvider(ProviderSettingsResolver resolver, RestClient restClient) {
        this.resolver = resolver;
        this.restClient = restClient;
    }

    @Override
    public GenerationProvider id() {
        return GenerationProvider.GEMINI;
    }

    @Override
    public String model() {
        return resolver.resolve(GenerationProvider.GEMINI).getModel();
    }

    @Override
    public GenerationResult generate(GenerationInput input) {
        ResolvedProviderConfig resolved = resolver.resolve(GenerationProvider.GEMINI);

        if (!resolved.isAvailable()) {
            throw new CoverLetterGenerationException("Gemini is not configured or is currently disabled.");
        }
        if (input.jobDescription() == null || input.jobDescription().isBlank()) {
            throw new CoverLetterGenerationException("The selected job has no description to generate from.");
        }

        String prompt = GeminiPromptBuilder.build(input);
        GeminiGenerateContentResponse response = callGemini(resolved, prompt);

        String text = response != null ? response.firstText() : null;
        if (text == null || text.isBlank()) {
            throw new CoverLetterGenerationException("Gemini returned an empty response.");
        }

        return new GenerationResult(text.trim());
    }

    /**
     * The API key is sent as a header (never in the URL), so it can never end
     * up in access/proxy logs. Retries only HTTP 429/5xx (transient) up to
     * {@link #MAX_ATTEMPTS} total attempts with short exponential backoff +
     * jitter; every other failure (4xx, timeout/connection failure, malformed
     * response) fails on the first attempt, unchanged.
     */
    private GeminiGenerateContentResponse callGemini(ResolvedProviderConfig resolved, String prompt) {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return restClient.post()
                        .uri(resolved.getBaseUrl() + GENERATE_CONTENT_PATH, resolved.getModel())
                        .header("x-goog-api-key", resolved.getApiKey())
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(GeminiGenerateContentRequest.ofPrompt(prompt))
                        .retrieve()
                        .body(GeminiGenerateContentResponse.class);
            } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden e) {
                logGeminiFailure(resolved, e, "authentication");
                throw new CoverLetterGenerationException(
                        "Gemini rejected the request due to an authentication problem (HTTP " + e.getStatusCode().value() + ").");
            } catch (HttpClientErrorException.TooManyRequests e) {
                if (attempt >= MAX_ATTEMPTS) {
                    logGeminiFailure(resolved, e, "rate limit");
                    throw new CoverLetterGenerationException(
                            "Gemini rate limit was exceeded (HTTP " + e.getStatusCode().value() + "). Please try again later.");
                }
                waitBeforeRetry(e, "rate limit", attempt);
            } catch (HttpServerErrorException e) {
                if (attempt >= MAX_ATTEMPTS) {
                    logGeminiFailure(resolved, e, "server");
                    throw new CoverLetterGenerationException(
                            "Gemini is currently unavailable (HTTP " + e.getStatusCode().value() + "). Please try again later.");
                }
                waitBeforeRetry(e, "server", attempt);
            } catch (HttpClientErrorException e) {
                logGeminiFailure(resolved, e, "client");
                throw new CoverLetterGenerationException("Gemini rejected the request (HTTP " + e.getStatusCode().value() + ").");
            } catch (ResourceAccessException e) {
                log.warn("Gemini call failed (timeout or connection failure): model={}, baseUrl={}",
                        resolved.getModel(), resolved.getBaseUrl());
                throw new CoverLetterGenerationException("Could not reach Gemini (timeout or connection failure).");
            } catch (RestClientException e) {
                log.warn("Gemini call failed (unexpected response): model={}, baseUrl={}",
                        resolved.getModel(), resolved.getBaseUrl());
                throw new CoverLetterGenerationException("Gemini returned an unexpected response.");
            }
        }
        // Unreachable: every loop iteration above either returns or throws — the final
        // attempt (attempt == MAX_ATTEMPTS) always throws instead of retrying.
        throw new CoverLetterGenerationException("Gemini returned an unexpected response.");
    }

    /**
     * Logs the retry decision (provider, status, attempt, delay) — never the API key or response body — then sleeps.
     */
    private void waitBeforeRetry(HttpStatusCodeException e, String category, int attempt) {
        long delayMillis = retryDelayMillis(attempt);
        log.warn("Gemini {} error (retrying): provider=GEMINI, status={}, attempt={}, retryDelayMs={}",
                category, e.getStatusCode().value(), attempt, delayMillis);
        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new CoverLetterGenerationException("Gemini call was interrupted while retrying.");
        }
    }

    /**
     * attempt 1 (about to become retry 1): ~500-750ms. attempt 2 (about to become retry 2): ~1500-2000ms.
     */
    private long retryDelayMillis(int failedAttempt) {
        long base = failedAttempt == 1 ? RETRY_1_BASE_DELAY_MILLIS : RETRY_2_BASE_DELAY_MILLIS;
        long jitterRange = failedAttempt == 1 ? RETRY_1_JITTER_MILLIS : RETRY_2_JITTER_MILLIS;
        return base + ThreadLocalRandom.current().nextLong(jitterRange + 1);
    }

    /**
     * Logs enough to actually debug a Gemini failure (provider, HTTP status, model,
     * base URL, sanitized provider error category/message) without ever logging the
     * API key, the request body, or the full raw response body — only the
     * error.status/error.message field VALUES from Gemini's own error envelope
     * (which never echo back the caller's credentials), extracted without a JSON
     * parser dependency; falls back to a short, bounded snippet if the body doesn't
     * match Gemini's documented error shape.
     */
    private void logGeminiFailure(ResolvedProviderConfig resolved, HttpStatusCodeException e, String category) {
        log.warn("Gemini {} error: provider=GEMINI, status={}, model={}, baseUrl={}, reason={}",
                category, e.getStatusCode().value(), resolved.getModel(), resolved.getBaseUrl(), sanitizedReason(e));
    }

    private String sanitizedReason(HttpStatusCodeException e) {
        String body = e.getResponseBodyAsString();
        if (body == null || body.isBlank()) {
            return "(no response body)";
        }

        Matcher statusMatcher = ERROR_STATUS_PATTERN.matcher(body);
        Matcher messageMatcher = ERROR_MESSAGE_PATTERN.matcher(body);
        String status = statusMatcher.find() ? statusMatcher.group(1) : null;
        String message = messageMatcher.find() ? messageMatcher.group(1) : null;
        if (status != null || message != null) {
            return (status != null ? status : "?") + ": " + (message != null ? message : "?");
        }

        return body.length() > MAX_LOGGED_ERROR_BODY_LENGTH ? body.substring(0, MAX_LOGGED_ERROR_BODY_LENGTH) + "…" : body;
    }
}
