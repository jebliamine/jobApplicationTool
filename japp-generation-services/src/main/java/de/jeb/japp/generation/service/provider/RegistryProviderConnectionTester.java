package de.jeb.japp.generation.service.provider;

import de.jeb.japp.ai.service.ProviderConnectionTester;
import de.jeb.japp.ai.service.ProviderSettingsResolver;
import de.jeb.japp.ai.service.ResolvedProviderConfig;
import de.jeb.japp.commons.exceptions.generation.CoverLetterGenerationException;
import de.jeb.japp.dao.ai.AiProviderConfigurationDao;
import de.jeb.japp.model.ai.AdapterType;
import de.jeb.japp.model.ai.AiProviderConfiguration;
import de.jeb.japp.model.ai.dto.AiProviderTestResult;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Implements the {@link ProviderConnectionTester} contract declared in
 * japp-ai-provider-services — lives here because it needs
 * {@link CoverLetterGenerationAdapterRegistry}, which that module must not
 * depend on. Spring wires this bean in wherever ProviderConnectionTester is
 * injected, since both modules are on the classpath of the assembled app.
 * <p>
 * A "test" is simply a real generate() call with a minimal, throwaway
 * prompt — it goes through the exact same instance/adapter resolution path a
 * real cover-letter generation does, so the result is trustworthy. The raw
 * provider response is never returned; only the existing, already-sanitized
 * CoverLetterGenerationException message (or a generic success message).
 */
@Service
public class RegistryProviderConnectionTester implements ProviderConnectionTester {

    private final AiProviderConfigurationDao providerDao;
    private final ProviderSettingsResolver providerSettingsResolver;
    private final CoverLetterGenerationAdapterRegistry adapterRegistry;

    public RegistryProviderConnectionTester(
            AiProviderConfigurationDao providerDao,
            ProviderSettingsResolver providerSettingsResolver,
            CoverLetterGenerationAdapterRegistry adapterRegistry
    ) {
        this.providerDao = providerDao;
        this.providerSettingsResolver = providerSettingsResolver;
        this.adapterRegistry = adapterRegistry;
    }

    @Override
    public AiProviderTestResult test(UUID instanceId) {
        try {
            AiProviderConfiguration providerInstance = providerDao.getById(instanceId)
                    .orElseThrow(() -> new CoverLetterGenerationException("Unknown AI provider instance."));
            AdapterType adapterType = AdapterType.valueOf(providerInstance.getAdapterType());
            CoverLetterGenerationAdapter adapter = adapterRegistry.resolve(adapterType);
            ResolvedProviderConfig config = providerSettingsResolver.resolve(instanceId);

            adapter.generate(config, testInput());
            return new AiProviderTestResult(true, "Connection successful.");
        } catch (CoverLetterGenerationException e) {
            return new AiProviderTestResult(false, e.getMessage());
        } catch (IllegalArgumentException e) {
            return new AiProviderTestResult(false, "This provider instance has an unknown adapter type.");
        }
    }

    private GenerationInput testInput() {
        return new GenerationInput(
                "Test Position",
                "Test Company",
                "This is a connection test. Reply with a short acknowledgement.",
                null,
                null,
                "Test User"
        );
    }
}
