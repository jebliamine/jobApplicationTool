package de.jeb.japp.model.generation;

import de.jeb.japp.model.cv.CVDocument;
import de.jeb.japp.model.job.Job;
import de.jeb.japp.model.user.User;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "generationrequest", indexes = {
        @Index(name = "idx_generationrequest_user", columnList = "user_id"),
        @Index(name = "idx_generationrequest_job", columnList = "job_id"),
        @Index(name = "idx_generationrequest_cv_document", columnList = "cv_document_id"),
        @Index(name = "idx_generationrequest_status", columnList = "status")
})
public class GenerationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    /**
     * Nullable, ON DELETE SET NULL — same CV-deletion-safety treatment as
     * Application.cvDocument: deleting the CV must never delete generation
     * history. A CVDocument is still required at request-creation time; this
     * only protects existing records afterward.
     */
    @ManyToOne
    @JoinColumn(name = "cv_document_id", nullable = true)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private CVDocument cvDocument;

    /** Copy of the Job's description at request time, so the request stays reproducible even if the Job changes later. */
    @Column(length = 8000)
    private String jobDescriptionSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GenerationStatus status;

    private String provider;
    private String model;

    @Column(length = 2000)
    private String errorMessage;

    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public CVDocument getCvDocument() {
        return cvDocument;
    }

    public void setCvDocument(CVDocument cvDocument) {
        this.cvDocument = cvDocument;
    }

    public String getJobDescriptionSnapshot() {
        return jobDescriptionSnapshot;
    }

    public void setJobDescriptionSnapshot(String jobDescriptionSnapshot) {
        this.jobDescriptionSnapshot = jobDescriptionSnapshot;
    }

    public GenerationStatus getStatus() {
        return status;
    }

    public void setStatus(GenerationStatus status) {
        this.status = status;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
