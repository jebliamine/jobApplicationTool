package de.jeb.japp.model.ai;

import de.jeb.japp.model.user.User;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * One admin-managed provider instance — a named, configured connection using one
 * {@link AdapterType}. Unlike the old one-row-per-enum-constant model, admins can create any
 * number of instances of the same adapter type (e.g. several OPENAI_COMPATIBLE instances
 * pointing at different accounts, models, or local servers). adapterType is a plain String
 * matching AdapterType#name() — not a JPA enum mapping, so adding a new adapter type never
 * requires a migration for existing rows. encryptedApiKey is ciphertext only — the encryption
 * key itself never lives in this database; see AiCredentialEncryptor (japp-ai-provider-services).
 */
@Entity
@Table(name = "ai_provider_configuration")
public class AiProviderConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String adapterType;

    @Column(nullable = false)
    private String displayName;

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

    public String getAdapterType() {
        return adapterType;
    }

    public void setAdapterType(String adapterType) {
        this.adapterType = adapterType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
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
