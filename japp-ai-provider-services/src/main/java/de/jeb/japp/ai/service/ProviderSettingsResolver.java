package de.jeb.japp.ai.service;

import de.jeb.japp.ai.service.encryption.AiCredentialEncryptor;
import de.jeb.japp.ai.service.gemini.GeminiProperties;
import de.jeb.japp.dao.ai.AiProviderConfigurationDao;
import de.jeb.japp.model.ai.AiProviderConfiguration;
import de.jeb.japp.model.generation.GenerationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves the current, effective configuration for one provider at
 * generation-call time (never at Spring startup). Resolution order per
 * provider:
 *
 * <ol>
 *   <li>Database configuration (AiProviderConfiguration), if the row is
 *       enabled and (for credentialed providers) a usable API key is stored.</li>
 *   <li>Otherwise, the environment/application.yml bootstrap configuration
 *       (e.g. {@link GeminiProperties} for GEMINI).</li>
 *   <li>Otherwise, unavailable.</li>
 * </ol>
 *
 * PLACEHOLDER needs no credential at all, so it resolves as available
 * whenever its (seeded, default-enabled) row is enabled — see
 * {@link de.jeb.japp.ai.service.AiProviderConfigurationSeeder}.
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
    private final GeminiProperties geminiProperties;

    private final Map<GenerationProvider, CacheEntry> cache = new ConcurrentHashMap<>();

    public ProviderSettingsResolver(
            AiProviderConfigurationDao dao,
            AiCredentialEncryptor encryptor,
            GeminiProperties geminiProperties
    ) {
        this.dao = dao;
        this.encryptor = encryptor;
        this.geminiProperties = geminiProperties;
    }

    public ResolvedProviderConfig resolve(GenerationProvider provider) {
        CacheEntry cached = cache.get(provider);
        if (cached != null && cached.expiresAt.isAfter(Instant.now())) {
            return cached.config;
        }

        ResolvedProviderConfig resolved = computeResolution(provider);
        cache.put(provider, new CacheEntry(resolved, Instant.now().plusMillis(CACHE_TTL_MILLIS)));
        return resolved;
    }

    /** Called by AdminAiProviderService right after a successful write, so the next call sees fresh configuration immediately. */
    public void invalidate(GenerationProvider provider) {
        cache.remove(provider);
    }

    private ResolvedProviderConfig computeResolution(GenerationProvider provider) {
        Optional<AiProviderConfiguration> row = dao.getByProvider(provider.name());

        if (provider == GenerationProvider.PLACEHOLDER) {
            boolean enabled = row.map(AiProviderConfiguration::isEnabled).orElse(true);
            String model = row.map(AiProviderConfiguration::getDefaultModel).orElse(null);
            return new ResolvedProviderConfig(enabled, null, model, null);
        }

        if (provider == GenerationProvider.GEMINI) {
            if (row.isPresent() && row.get().isEnabled()) {
                AiProviderConfiguration config = row.get();

                if (config.getEncryptedApiKey() == null || config.getEncryptedApiKey().isBlank()) {
                    log.debug("AiProviderConfiguration row for {} is enabled but has no stored API key; "
                            + "falling back to environment configuration.", provider);
                } else if (!encryptor.isAvailable()) {
                    log.warn("AiProviderConfiguration row for {} has a stored API key, but "
                            + "AI_CREDENTIALS_ENCRYPTION_KEY is not configured, so it cannot be decrypted; "
                            + "falling back to environment configuration.", provider);
                }

                String decryptedKey = tryDecrypt(config.getEncryptedApiKey(), provider);
                if (decryptedKey != null) {
                    String model = config.getDefaultModel() != null ? config.getDefaultModel() : geminiProperties.getModel();
                    String baseUrl = config.getBaseUrl() != null ? config.getBaseUrl() : geminiProperties.getBaseUrl();
                    log.debug("Resolved {} from database configuration (model={}, baseUrl={}).", provider, model, baseUrl);
                    return new ResolvedProviderConfig(true, decryptedKey, model, baseUrl);
                }
            } else if (row.isEmpty()) {
                log.debug("No AiProviderConfiguration row found for {}; falling back to environment configuration.", provider);
            } else {
                log.debug("AiProviderConfiguration row for {} is disabled; falling back to environment configuration.", provider);
            }

            if (geminiProperties.isConfigured()) {
                log.debug("Resolved {} from environment/application.yml fallback configuration (model={}).",
                        provider, geminiProperties.getModel());
                return new ResolvedProviderConfig(
                        true, geminiProperties.getApiKey(), geminiProperties.getModel(), geminiProperties.getBaseUrl());
            }

            // Unavailable, but still report the configured model/base URL as informational
            // metadata — matches the prior behavior where model() always reflected the
            // configured model name, even when generation would go on to fail.
            log.warn("{} is unavailable: no usable database configuration and no environment fallback configured.", provider);
            return new ResolvedProviderConfig(false, null, geminiProperties.getModel(), geminiProperties.getBaseUrl());
        }

        return ResolvedProviderConfig.unavailable();
    }

    /** Never logs the encrypted or decrypted value — only that a decrypt attempt failed. */
    private String tryDecrypt(String encryptedApiKey, GenerationProvider provider) {
        if (encryptedApiKey == null || encryptedApiKey.isBlank() || !encryptor.isAvailable()) {
            return null;
        }
        try {
            String decrypted = encryptor.decrypt(encryptedApiKey);
            return (decrypted != null && !decrypted.isBlank()) ? decrypted : null;
        } catch (RuntimeException e) {
            log.warn("Could not decrypt the stored API key for provider {} "
                    + "(the encryption key may have changed) — falling back to environment configuration.", provider);
            return null;
        }
    }

    private record CacheEntry(ResolvedProviderConfig config, Instant expiresAt) {
    }
}
