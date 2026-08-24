package de.jeb.japp.model.cv;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * AI-extracted structured profile for one {@link CVDocument} (1:1) — name,
 * summary, and work experience, produced from the document's extractedText
 * by a CvProfileExtractionAdapter (see japp-generation-services). Distinct
 * from CVDocument's own extraction bookkeeping: that tracks raw text
 * extraction (Tika/OCR), this tracks the later AI structuring step.
 */
@Entity
public class CVProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "cv_document_id", nullable = false, unique = true)
    private CVDocument cvDocument;

    private String fullName;

    @Column(length = 3000)
    private String summary;

    @OneToMany(mappedBy = "cvProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Experience> experiences;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProfileGenerationStatus status = ProfileGenerationStatus.NOT_ATTEMPTED;

    @Column(length = 2000)
    private String errorMessage;

    private LocalDateTime generatedAt;

    public UUID getId() {
        return id;
    }

    public CVDocument getCvDocument() {
        return cvDocument;
    }

    public void setCvDocument(CVDocument cvDocument) {
        this.cvDocument = cvDocument;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<Experience> getExperiences() {
        return experiences;
    }

    public void setExperiences(List<Experience> experiences) {
        this.experiences = experiences;
    }

    public ProfileGenerationStatus getStatus() {
        return status;
    }

    public void setStatus(ProfileGenerationStatus status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
}
