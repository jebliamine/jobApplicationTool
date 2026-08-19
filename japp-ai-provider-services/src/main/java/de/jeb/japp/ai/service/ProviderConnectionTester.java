package de.jeb.japp.ai.service;

import de.jeb.japp.model.ai.dto.AiProviderTestResult;
import de.jeb.japp.model.generation.GenerationProvider;

/**
 * Performs a minimal real call to a provider to verify its current
 * configuration actually works. Implemented in japp-generation-services
 * (which owns CoverLetterGenerationProviderRegistry / the actual provider
 * beans) — this module only declares the contract, so the approved
 * dependency direction (japp-generation-services -> japp-ai-provider-services,
 * never the reverse) is preserved. Spring wires the implementation in at
 * runtime since both modules are on the classpath of the assembled application.
 */
public interface ProviderConnectionTester {

    /** Must resolve configuration through the exact same path real generation uses. Never throws for a failed test. */
    AiProviderTestResult test(GenerationProvider provider);
}
