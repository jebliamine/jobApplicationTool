package de.jeb.japp.model.application;

import de.jeb.japp.model.coverLetter.CoverLetter;
import de.jeb.japp.model.cv.CVDocument;
import de.jeb.japp.model.job.Job;
import de.jeb.japp.model.user.User;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "application", indexes = {
        @Index(name = "idx_application_user", columnList = "user_id"),
        @Index(name = "idx_application_job", columnList = "job_id"),
        @Index(name = "idx_application_cv_document", columnList = "cv_document_id"),
        @Index(name = "idx_application_cover_letter", columnList = "cover_letter_id"),
        @Index(name = "idx_application_status", columnList = "status")
})
public class Application {

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
     * Nullable: an application may be created before a CV is selected.
     * ON DELETE SET NULL — deleting the referenced CV must never delete the
     * Application (application history is preserved with cvDocument = null).
     */
    @ManyToOne
    @JoinColumn(name = "cv_document_id", nullable = true)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private CVDocument cvDocument;

    /**
     * Nullable: a CoverLetter is independently owned by the User and remains
     * usable in their CoverLetter library whether or not any Application
     * references it — this is never the owning side of that relationship.
     * ON DELETE SET NULL — deleting the CoverLetter must never delete the
     * Application (application history is preserved with coverLetter = null).
     */
    @ManyToOne
    @JoinColumn(name = "cover_letter_id", nullable = true)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private CoverLetter coverLetter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    private LocalDate appliedAt;

    @Column(length = 4000)
    private String notes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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

    public CoverLetter getCoverLetter() {
        return coverLetter;
    }

    public void setCoverLetter(CoverLetter coverLetter) {
        this.coverLetter = coverLetter;
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
