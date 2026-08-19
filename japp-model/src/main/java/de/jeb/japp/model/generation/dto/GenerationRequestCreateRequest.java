package de.jeb.japp.model.generation.dto;

import de.jeb.japp.model.generation.GenerationProvider;

import java.util.UUID;

/**
 * Request body for POST /api/v1/generation-requests — owner is never
 * accepted from the client. {@code provider} is optional; when omitted the
 * service defaults to {@link GenerationProvider#PLACEHOLDER}.
 */
public class GenerationRequestCreateRequest {
    private UUID jobId;
    private UUID cvDocumentId;
    private GenerationProvider provider;

    public GenerationRequestCreateRequest() {
    }

    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }

    public UUID getCvDocumentId() {
        return cvDocumentId;
    }

    public void setCvDocumentId(UUID cvDocumentId) {
        this.cvDocumentId = cvDocumentId;
    }

    public GenerationProvider getProvider() {
        return provider;
    }

    public void setProvider(GenerationProvider provider) {
        this.provider = provider;
    }
}
