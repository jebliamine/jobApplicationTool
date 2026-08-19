package de.jeb.japp.model.ai.dto;

/** Result of POST /api/v1/admin/ai/providers/{provider}/test — safe, sanitized, never the raw provider response. */
public class AiProviderTestResult {
    private boolean success;
    private String message;

    public AiProviderTestResult() {
    }

    public AiProviderTestResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
