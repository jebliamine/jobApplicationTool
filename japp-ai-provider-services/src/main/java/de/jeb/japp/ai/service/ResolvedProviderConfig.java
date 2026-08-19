package de.jeb.japp.ai.service;

/**
 * Provider-neutral, fully-resolved configuration for one generation call —
 * returned by {@link ProviderSettingsResolver}. Immutable; the apiKey is the
 * decrypted value held only for the duration of one call. toString() is
 * overridden so an accidental log/debug dump never prints the key.
 */
public final class ResolvedProviderConfig {

    private final boolean available;
    private final String apiKey;
    private final String model;
    private final String baseUrl;

    public ResolvedProviderConfig(boolean available, String apiKey, String model, String baseUrl) {
        this.available = available;
        this.apiKey = apiKey;
        this.model = model;
        this.baseUrl = baseUrl;
    }

    public static ResolvedProviderConfig unavailable() {
        return new ResolvedProviderConfig(false, null, null, null);
    }

    public boolean isAvailable() {
        return available;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getModel() {
        return model;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    @Override
    public String toString() {
        return "ResolvedProviderConfig[available=" + available
                + ", model=" + model
                + ", baseUrl=" + baseUrl
                + ", apiKey=" + (apiKey != null && !apiKey.isBlank() ? "[REDACTED]" : "null")
                + "]";
    }
}
