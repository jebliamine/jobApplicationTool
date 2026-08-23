package de.jeb.japp.model.ai.dto;

/**
 * Request body for POST /api/v1/admin/ai/providers — creates a new provider instance.
 * adapterType must match one of the known AdapterType values (validated server-side).
 */
public class AiProviderCreateRequest {
    private String adapterType;
    private String displayName;
    private Boolean enabled;
    private String defaultModel;
    private String baseUrl;
    private String apiKey;

    public AiProviderCreateRequest() {
    }

    public String getAdapterType() {
        return adapterType;
    }

    public void setAdapterType(String adapterType) {
        this.adapterType = adapterType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getDefaultModel() {
        return defaultModel;
    }

    public void setDefaultModel(String defaultModel) {
        this.defaultModel = defaultModel;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
