package de.jeb.japp.model.job.dto;

/** Request body for POST /api/v1/jobs/extract. */
public class JobExtractionRequest {
    private String rawText;

    public JobExtractionRequest() {
    }

    public String getRawText() {
        return rawText;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
    }
}
