package de.jeb.japp.model.tag;

import de.jeb.japp.model.user.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A per-user label applicable to both {@link de.jeb.japp.model.job.Job} and
 * {@link de.jeb.japp.model.application.Application} (see their {@code tags}
 * collections) — same ownership model as Job/Company/Application: each user
 * manages their own tag vocabulary, never a shared/global one.
 */
@Entity
@Table(name = "tag", uniqueConstraints = @UniqueConstraint(columnNames = {"owner_id", "name"}))
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false, length = 100)
    private String name;

    private LocalDateTime createdAt;

    public UUID getId() {
        return id;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
