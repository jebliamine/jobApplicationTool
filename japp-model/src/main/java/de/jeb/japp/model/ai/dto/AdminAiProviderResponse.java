package de.jeb.japp.model.ai.dto;

/**
 * Admin response for GET /api/v1/admin/ai/providers — never the API key or
 * its ciphertext, only whether one is present (hasApiKey).
 */
public class AdminAiProviderResponse {
    private String provider;
    private String displayName;
    private boolean enabled;
    private boolean hasApiKey;
    private String defaultModel;
    private String baseUrl;

    public AdminAiProviderResponse() {
    }

    public AdminAiProviderResponse(
            String provider, String displayName, boolean enabled, boolean hasApiKey, String defaultModel, String baseUrl
    ) {
        this.provider = provider;
        this.displayName = displayName;
        this.enabled = enabled;
        this.hasApiKey = hasApiKey;
        this.defaultModel = defaultModel;
        this.baseUrl = baseUrl;
    }

    public String getProvider() {
        return provider;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isHasApiKey() {
        return hasApiKey;
    }

    public String getDefaultModel() {
        return defaultModel;
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}
