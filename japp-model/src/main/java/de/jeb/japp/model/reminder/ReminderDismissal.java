package de.jeb.japp.model.reminder;

import de.jeb.japp.model.application.Application;
import de.jeb.japp.model.user.User;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Persists that a user has dismissed or snoozed one specific reminder instance — identified by
 * (user, application, kind, dueDate), since the reminder itself is never stored (it's always
 * re-derived from the Application's own deadline/followUpDate/interviewDate at read time; see
 * ReminderService). {@code snoozedUntil} null means dismissed permanently for this exact due
 * date; non-null means it resurfaces once that date arrives. If the Application's underlying
 * date later changes, a dismissal keyed to the old date simply stops matching — the new date
 * surfaces as a fresh, undismissed reminder.
 */
@Entity
@Table(name = "reminder_dismissal", indexes = {
        @Index(name = "idx_reminder_dismissal_user", columnList = "user_id")
}, uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "application_id", "kind", "due_date"}))
public class ReminderDismissal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Application application;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReminderKind kind;

    @Column(nullable = false)
    private LocalDate dueDate;

    private LocalDate snoozedUntil;

    private LocalDateTime createdAt;

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public ReminderKind getKind() {
        return kind;
    }

    public void setKind(ReminderKind kind) {
        this.kind = kind;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getSnoozedUntil() {
        return snoozedUntil;
    }

    public void setSnoozedUntil(LocalDate snoozedUntil) {
        this.snoozedUntil = snoozedUntil;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
