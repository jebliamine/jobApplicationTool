package de.jeb.japp.cv.service.parser.generator;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Resolves the format-specific fallback generator for a given document type.
 * Deliberately excludes TikaContentGenerator (the primary strategy) and OCRContentGenerator
 * (the final fallback) — both are wired directly into the orchestrator instead, since they
 * apply at fixed points in the pipeline rather than being resolved by document type alone.
 */
@Component
public class ContentGeneratorRegistry {

    private final List<ContentGenerator> fallbackGenerators;

    public ContentGeneratorRegistry(List<ContentGenerator> fallbackGenerators) {
        this.fallbackGenerators = fallbackGenerators;
    }

    public Optional<ContentGenerator> resolveFallback(DocumentType type) {
        return fallbackGenerators.stream()
                .filter(generator -> generator.supports(type))
                .findFirst();
    }
}
