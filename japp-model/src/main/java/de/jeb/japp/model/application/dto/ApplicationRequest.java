package de.jeb.japp.model.application.dto;

import de.jeb.japp.model.application.ApplicationStatus;

import java.time.LocalDate;
import java.util.UUID;

/** Request body for POST/PUT /api/v1/applications — the owner is never accepted from the client. */
public class ApplicationRequest {
    private UUID jobId;
    private UUID cvDocumentId;
    private UUID coverLetterId;
    private ApplicationStatus status;
    private LocalDate appliedAt;
    private String notes;

    public ApplicationRequest() {
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

    public UUID getCoverLetterId() {
        return coverLetterId;
    }

    public void setCoverLetterId(UUID coverLetterId) {
        this.coverLetterId = coverLetterId;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public LocalDate getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(LocalDate appliedAt) {
        this.appliedAt = appliedAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
