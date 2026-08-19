package de.jeb.japp.model.coverLetter;

import de.jeb.japp.model.generation.GenerationRequest;
import de.jeb.japp.model.user.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "coverletter", indexes = {
        @Index(name = "idx_coverletter_owner", columnList = "owner_id"),
        @Index(name = "idx_coverletter_generation_request", columnList = "generation_request_id")
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
