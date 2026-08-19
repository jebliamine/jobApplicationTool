package de.jeb.japp.model.generation.dto;

import java.util.UUID;

/** Request body for POST /api/v1/generation-requests — owner is never accepted from the client. */
public class GenerationRequestCreateRequest {
    private UUID jobId;
    private UUID cvDocumentId;

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
}
