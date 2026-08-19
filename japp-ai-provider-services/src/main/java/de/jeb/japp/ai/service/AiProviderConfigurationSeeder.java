package de.jeb.japp.ai.service;

import de.jeb.japp.dao.ai.AiProviderConfigurationDao;
import de.jeb.japp.model.ai.AiProviderConfiguration;
import de.jeb.japp.model.generation.GenerationProvider;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Idempotent: ensures one AiProviderConfiguration row exists for every
 * {@link GenerationProvider} value, without ever overwriting an existing
 * row's configuration. Runs once at startup.
 *
 * PLACEHOLDER is seeded enabled=true by default — it requires no external
 * configuration, and defaulting it to disabled (like every other provider)
 * would silently break the zero-admin-action "it just works" behavior every
 * existing deployment already relies on. Every other provider (GEMINI and
 * any future one) seeds enabled=false, matching the approved default —
 * their availability is separately covered by the environment/application.yml
 * fallback (see ProviderSettingsResolver), so this does not regress existing
 * Gemini functionality either.
 */
@Component
public class AiProviderConfigurationSeeder implements ApplicationRunner {

    private final AiProviderConfigurationDao dao;

    public AiProviderConfigurationSeeder(AiProviderConfigurationDao dao) {
        this.dao = dao;
    }

    @Override
    public void run(ApplicationArguments args) {
        for (GenerationProvider provider : GenerationProvider.values()) {
            if (dao.existsByProvider(provider.name())) {
                continue;
            }

            AiProviderConfiguration configuration = new AiProviderConfiguration();
            configuration.setProvider(provider.name());
            configuration.setEnabled(provider == GenerationProvider.PLACEHOLDER);
            LocalDateTime now = LocalDateTime.now();
            configuration.setCreatedAt(now);
            configuration.setUpdatedAt(now);
            dao.save(configuration);
        }
    }
}
