package de.jeb.japp.model.generation.dto;

import java.util.UUID;

/**
 * Request body for POST /api/v1/generation-requests — owner is never
 * accepted from the client. {@code providerId} is the id of an
 * AiProviderConfiguration instance; optional — when omitted the service
 * defaults to the built-in Placeholder instance. {@code useStructuredCv}
 * requests the AI-extracted {@code CVProfile} (name/summary/experience) as
 * the CV context instead of the CV's raw extracted text; the service falls
 * back to the raw text when no COMPLETED profile exists for the CV, so this
 * flag is always safe to send regardless of whether extraction succeeded.
 */
public class GenerationRequestCreateRequest {
    private UUID jobId;
    private UUID cvDocumentId;
    private UUID providerId;
    private boolean useStructuredCv;

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

    public UUID getProviderId() {
        return providerId;
    }

    public void setProviderId(UUID providerId) {
        this.providerId = providerId;
    }

    public boolean isUseStructuredCv() {
        return useStructuredCv;
    }

    public void setUseStructuredCv(boolean useStructuredCv) {
        this.useStructuredCv = useStructuredCv;
    }
}
