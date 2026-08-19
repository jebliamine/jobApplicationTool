package de.jeb.japp.model.coverLetter.dto;

/** Request body for PUT /api/v1/cover-letters/{id} — only the edited text is accepted from the client. */
public class CoverLetterUpdateRequest {
    private String resultText;

    public CoverLetterUpdateRequest() {
    }

    public String getResultText() {
        return resultText;
    }

    public void setResultText(String resultText) {
        this.resultText = resultText;
    }
}
