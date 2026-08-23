package de.jeb.japp.generation.service.provider;

import de.jeb.japp.ai.service.ProviderConnectionTester;
import de.jeb.japp.commons.exceptions.generation.CoverLetterGenerationException;
import de.jeb.japp.model.ai.dto.AiProviderTestResult;
import de.jeb.japp.model.generation.GenerationProvider;
import org.springframework.stereotype.Service;

/**
 * Implements the {@link ProviderConnectionTester} contract declared in
 * japp-ai-provider-services — lives here because it needs
 * {@link CoverLetterGenerationProviderRegistry}, which that module must not
 * depend on. Spring wires this bean in wherever ProviderConnectionTester is
 * injected, since both modules are on the classpath of the assembled app.
 * <p>
 * A "test" is simply a real generate() call with a minimal, throwaway
 * prompt — it goes through the exact same provider/resolver path a real
 * cover-letter generation does, so the result is trustworthy. The raw
 * provider response is never returned; only the existing, already-sanitized
 * CoverLetterGenerationException message (or a generic success message).
 */
@Service
public class RegistryProviderConnectionTester implements ProviderConnectionTester {

    private final CoverLetterGenerationProviderRegistry registry;

    public RegistryProviderConnectionTester(CoverLetterGenerationProviderRegistry registry) {
        this.registry = registry;
    }

    @Override
    public AiProviderTestResult test(GenerationProvider provider) {
        try {
            CoverLetterGenerationProvider resolved = registry.resolve(provider);
            resolved.generate(testInput());
            return new AiProviderTestResult(true, "Connection successful.");
        } catch (CoverLetterGenerationException e) {
            return new AiProviderTestResult(false, e.getMessage());
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
