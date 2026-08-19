package de.jeb.japp.model.ai;

import de.jeb.japp.model.user.User;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * One row per known AI provider (see GenerationProvider), seeded at startup.
 * provider is a plain String matching GenerationProvider#name() — not a JPA
 * enum mapping, so adding a provider later never requires a migration (see
 * GenerationRequest.provider/model for the same, already-established
 * pattern). encryptedApiKey is ciphertext only — the encryption key itself
 * never lives in this database; see AiCredentialEncryptor (japp-ai-provider-services).
 */
@Entity
@Table(name = "ai_provider_configuration", uniqueConstraints = @UniqueConstraint(columnNames = "provider"))
public class AiProviderConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String provider;

    @Column(nullable = false)
    private boolean enabled;

    @Column(length = 2000)
    private String encryptedApiKey;

    private String defaultModel;

    private String baseUrl;

    @ManyToOne
    @JoinColumn(name = "updated_by")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private User updatedBy;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UUID getId() {
        return id;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getEncryptedApiKey() {
        return encryptedApiKey;
    }

    public void setEncryptedApiKey(String encryptedApiKey) {
        this.encryptedApiKey = encryptedApiKey;
    }

    public String getDefaultModel() {
        return defaultModel;
    }

    public void setDefaultModel(String defaultModel) {
        this.defaultModel = defaultModel;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public User getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(User updatedBy) {
        this.updatedBy = updatedBy;
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
