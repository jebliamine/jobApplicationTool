package de.jeb.japp.ai.service;

import de.jeb.japp.model.generation.GenerationProvider;

final class ProviderDisplayNames {

    private ProviderDisplayNames() {
    }

    static String of(GenerationProvider provider) {
        return switch (provider) {
            case PLACEHOLDER -> "Placeholder";
            case GEMINI -> "Google Gemini";
        };
    }
}
