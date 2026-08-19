package de.jeb.japp.model.ai.dto;

/**
 * Public, non-admin response for GET /api/v1/ai/providers — deliberately has
 * no field capable of holding a key or ciphertext. `available` reflects
 * whether the provider can actually be used right now (database
 * configuration, or environment fallback, or — for PLACEHOLDER — no
 * configuration needed at all).
 */
public class AiProviderResponse {
    private String id;
    private String displayName;
    private boolean available;
    private String model;

    public AiProviderResponse() {
    }

    public AiProviderResponse(String id, String displayName, boolean available, String model) {
        this.id = id;
        this.displayName = displayName;
        this.available = available;
        this.model = model;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isAvailable() {
        return available;
    }

    public String getModel() {
        return model;
    }
}
