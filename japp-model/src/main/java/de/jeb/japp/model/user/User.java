package de.jeb.japp.model.user;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "japuser")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String fullName;
    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    /** Defaults true so every existing new User(...) call site (registration, AdminSeeder) keeps working unchanged. */
    @Column(nullable = false)
    private boolean enabled = true;

    /**
     * Defaults true for the same reason {@link #enabled} does — every existing call site keeps
     * working unchanged. AuthController explicitly sets this false right after a new self-registration
     * and only flips it once the user follows their emailed verification link; nothing currently
     * gates login or feature access on this flag (see EmailVerificationService for the full rationale).
     */
    @Column(nullable = false)
    private boolean emailVerified = true;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    /** Null when the user has no uploaded avatar — the frontend falls back to an initials avatar. */
    private String avatarStorageKey;

    private String avatarContentType;


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getAvatarStorageKey() {
        return avatarStorageKey;
    }

    public void setAvatarStorageKey(String avatarStorageKey) {
        this.avatarStorageKey = avatarStorageKey;
    }

    public String getAvatarContentType() {
        return avatarContentType;
    }

    public void setAvatarContentType(String avatarContentType) {
        this.avatarContentType = avatarContentType;
    }
}
