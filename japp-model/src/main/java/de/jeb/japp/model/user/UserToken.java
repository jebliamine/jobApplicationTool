package de.jeb.japp.model.user;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A one-time token backing either a password-reset or an email-verification link. One table with
 * a {@link UserTokenType} discriminator rather than two near-identical tables, since the shape and
 * lifecycle (issued, optionally consumed once, expires) are identical for both.
 */
@Entity
@Table(name = "user_token", indexes = {
        @Index(name = "idx_user_token_user", columnList = "user_id"),
        @Index(name = "idx_user_token_token", columnList = "token")
})
public class UserToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserTokenType type;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private LocalDateTime usedAt;

    @Column(nullable = false)
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserTokenType getType() {
        return type;
    }

    public void setType(UserTokenType type) {
        this.type = type;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getUsedAt() {
        return usedAt;
    }

    public void setUsedAt(LocalDateTime usedAt) {
        this.usedAt = usedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isUsed() {
        return usedAt != null;
    }
}
