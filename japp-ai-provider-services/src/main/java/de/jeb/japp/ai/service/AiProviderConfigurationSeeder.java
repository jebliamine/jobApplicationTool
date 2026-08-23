package de.jeb.japp.ai.service;

import de.jeb.japp.dao.ai.AiProviderConfigurationDao;
import de.jeb.japp.model.ai.AdapterType;
import de.jeb.japp.model.ai.AiProviderConfiguration;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Idempotent: ensures exactly one built-in Placeholder instance exists. Runs once at startup.
 *
 * Every other provider instance is now fully admin-managed (created/edited/deleted through the
 * admin UI) — there is nothing else to seed. Placeholder is seeded enabled=true by default — it
 * requires no external configuration, and defaulting it to disabled would silently break the
 * zero-admin-action "it just works" behavior every existing deployment already relies on.
 */
@Component
public class AiProviderConfigurationSeeder implements ApplicationRunner {

    private final AiProviderConfigurationDao dao;

    public AiProviderConfigurationSeeder(AiProviderConfigurationDao dao) {
        this.dao = dao;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (dao.existsByAdapterType(AdapterType.PLACEHOLDER.name())) {
            return;
        }

        AiProviderConfiguration configuration = new AiProviderConfiguration();
        configuration.setAdapterType(AdapterType.PLACEHOLDER.name());
        configuration.setDisplayName(AdapterTypeDisplayNames.defaultFor(AdapterType.PLACEHOLDER));
        configuration.setEnabled(true);
        LocalDateTime now = LocalDateTime.now();
        configuration.setCreatedAt(now);
        configuration.setUpdatedAt(now);
        dao.save(configuration);
    }
}
