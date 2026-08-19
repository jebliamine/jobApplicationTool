package de.jeb.japp.ai.service.gemini;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Binds {@code ai.gemini.*} configuration (see application.yml — backed by
 * GEMINI_API_KEY / AI_GEMINI_MODEL / AI_GEMINI_BASE_URL / AI_GEMINI_TIMEOUT
 * environment variables). apiKey defaults to blank so the application starts
 * fine with Gemini unconfigured.
 *
 * This is now the environment/bootstrap fallback layer only — used by
 * {@link de.jeb.japp.ai.service.ProviderSettingsResolver} when there is no
 * usable database configuration for GEMINI. Database configuration, once an
 * admin enables and configures it, takes precedence over these values.
 */
@Component
@ConfigurationProperties(prefix = "ai.gemini")
public class GeminiProperties {

    private String apiKey = "";
    // gemini-2.0-flash was shut down 2026-06-01 (per ai.google.dev/gemini-api/docs/deprecations);
    // gemini-3.7-flash is the current recommended general-purpose default.
    private String model = "gemini-3.7-flash";
    private String baseUrl = "https://generativelanguage.googleapis.com";
    private Duration timeout = Duration.ofSeconds(30);

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }
}
