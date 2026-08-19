package de.jeb.japp.model.coverLetter;

import de.jeb.japp.model.generation.GenerationRequest;
import de.jeb.japp.model.user.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "coverletter", indexes = {
        @Index(name = "idx_coverletter_owner", columnList = "owner_id"),
        @Index(name = "idx_coverletter_generation_request", columnList = "generation_request_id"),
        @Index(name = "idx_coverletter_archived", columnList = "archived")
})
public class CoverLetter {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    /** Generation provenance — the request that produced this CoverLetter. One CoverLetter per GenerationRequest. */
    @OneToOne(optional = false)
    @JoinColumn(name = "generation_request_id", nullable = false, unique = true)
    private GenerationRequest generationRequest;

    @Column(length = 8000)
    private String resultText;

    /**
     * Archived CoverLetters are excluded from the default list but never
     * deleted — a USER cannot permanently delete a CoverLetter, only archive
     * it. Defaults to false; {@code columnDefinition} gives existing rows a
     * safe backfill value when this column is first added to the table.
     */
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean archived;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UUID getId() {
        return id;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public GenerationRequest getGenerationRequest() {
        return generationRequest;
    }

    public void setGenerationRequest(GenerationRequest generationRequest) {
        this.generationRequest = generationRequest;
    }

    public String getResultText() {
        return resultText;
    }

    public void setResultText(String resultText) {
        this.resultText = resultText;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
