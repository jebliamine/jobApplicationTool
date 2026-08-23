package de.jeb.japp.ai.service;

import de.jeb.japp.ai.service.encryption.AiCredentialEncryptor;
import de.jeb.japp.dao.ai.AiProviderConfigurationDao;
import de.jeb.japp.model.ai.AdapterType;
import de.jeb.japp.model.ai.AiProviderConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves the current, effective configuration for one provider instance at
 * generation-call time (never at Spring startup) — a plain database lookup
 * by instance id, since every instance's configuration lives entirely in
 * {@link AiProviderConfiguration}. There is deliberately no per-adapter
 * branching here (no environment/application.yml fallback layer): the whole
 * point of admin-managed instances is that configuration lives in one place.
 *
 * PLACEHOLDER instances need no credential at all, so they resolve as
 * available whenever their row is enabled.
 *
 * Results are cached in-process (ConcurrentHashMap) and invalidated
 * immediately whenever an admin write succeeds (see
 * {@link AdminAiProviderService}); a short TTL is a defensive backstop only.
 */
@Component
public class ProviderSettingsResolver {

    private static final Logger log = LoggerFactory.getLogger(ProviderSettingsResolver.class);
    private static final long CACHE_TTL_MILLIS = 30_000L;

    private final AiProviderConfigurationDao dao;
    private final AiCredentialEncryptor encryptor;

    private final Map<UUID, CacheEntry> cache = new ConcurrentHashMap<>();

    public ProviderSettingsResolver(AiProviderConfigurationDao dao, AiCredentialEncryptor encryptor) {
        this.dao = dao;
        this.encryptor = encryptor;
    }

    public ResolvedProviderConfig resolve(UUID instanceId) {
        if (instanceId == null) {
            return ResolvedProviderConfig.unavailable();
        }

        CacheEntry cached = cache.get(instanceId);
        if (cached != null && cached.expiresAt.isAfter(Instant.now())) {
            return cached.config;
        }

        ResolvedProviderConfig resolved = computeResolution(instanceId);
        cache.put(instanceId, new CacheEntry(resolved, Instant.now().plusMillis(CACHE_TTL_MILLIS)));
        return resolved;
    }

    /** Called by AdminAiProviderService right after a successful write, so the next call sees fresh configuration immediately. */
    public void invalidate(UUID instanceId) {
        cache.remove(instanceId);
    }

    private ResolvedProviderConfig computeResolution(UUID instanceId) {
        Optional<AiProviderConfiguration> row = dao.getById(instanceId);
        if (row.isEmpty()) {
            log.debug("No AiProviderConfiguration row found for instance {}.", instanceId);
            return ResolvedProviderConfig.unavailable();
        }

        AiProviderConfiguration config = row.get();
        if (!config.isEnabled()) {
            log.debug("Provider instance {} ({}) is disabled.", instanceId, config.getDisplayName());
            return ResolvedProviderConfig.unavailable();
        }

        if (AdapterType.PLACEHOLDER.name().equals(config.getAdapterType())) {
            return new ResolvedProviderConfig(true, null, config.getDefaultModel(), null);
        }

        String decryptedKey = tryDecrypt(config.getEncryptedApiKey(), instanceId);
        if (decryptedKey == null) {
            log.warn("Provider instance {} ({}) is enabled but has no usable API key.",
                    instanceId, config.getDisplayName());
            return new ResolvedProviderConfig(false, null, config.getDefaultModel(), config.getBaseUrl());
        }

        return new ResolvedProviderConfig(true, decryptedKey, config.getDefaultModel(), config.getBaseUrl());
    }

    /** Never logs the encrypted or decrypted value — only that a decrypt attempt failed. */
    private String tryDecrypt(String encryptedApiKey, UUID instanceId) {
        if (encryptedApiKey == null || encryptedApiKey.isBlank()) {
            return null;
        }
        if (!encryptor.isAvailable()) {
            log.warn("Provider instance {} has a stored API key, but AI_CREDENTIALS_ENCRYPTION_KEY is not "
                    + "configured, so it cannot be decrypted.", instanceId);
            return null;
        }
        try {
            String decrypted = encryptor.decrypt(encryptedApiKey);
            return (decrypted != null && !decrypted.isBlank()) ? decrypted : null;
        } catch (RuntimeException e) {
            log.warn("Could not decrypt the stored API key for provider instance {} "
                    + "(the encryption key may have changed).", instanceId);
            return null;
        }
    }

    private record CacheEntry(ResolvedProviderConfig config, Instant expiresAt) {
    }
}
