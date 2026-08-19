package de.jeb.japp.model.ai.dto;

/**
 * Request body for PUT /api/v1/admin/ai/providers/{provider}. apiKey is
 * optional — omitted/null means "keep the existing key". clearApiKey removes
 * the stored key entirely (takes precedence over a supplied apiKey if both
 * are somehow set, though the client should only ever send one or the other).
 */
public class AiProviderUpdateRequest {
    private Boolean enabled;
    private String defaultModel;
    private String baseUrl;
    private String apiKey;
    private boolean clearApiKey;

    public AiProviderUpdateRequest() {
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

    public boolean isClearApiKey() {
        return clearApiKey;
    }

    public void setClearApiKey(boolean clearApiKey) {
        this.clearApiKey = clearApiKey;
    }
}
