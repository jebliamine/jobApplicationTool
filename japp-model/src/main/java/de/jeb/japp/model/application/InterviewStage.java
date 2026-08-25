package de.jeb.japp.model.application;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * One round of a multi-round interview pipeline for an {@link Application} — replaces the old
 * single {@code interviewDate} column (see V10 migration) so "phone screen → onsite → offer"
 * is representable as several independently dated, noted, and completable rows instead of one
 * date. Ordering in the UI is by {@code scheduledDate} (see ApplicationResponse#from); there is
 * no separate explicit position field.
 */
@Entity
@Table(name = "interview_stage", indexes = {
        @Index(name = "idx_interview_stage_application", columnList = "application_id")
})
public class InterviewStage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Application application;

    @Column(nullable = false)
    private String title;

    private LocalDate scheduledDate;

    @Column(length = 2000)
    private String notes;

    @Column(nullable = false)
    private boolean completed;

    private LocalDateTime createdAt;

    public UUID getId() {
        return id;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(LocalDate scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
