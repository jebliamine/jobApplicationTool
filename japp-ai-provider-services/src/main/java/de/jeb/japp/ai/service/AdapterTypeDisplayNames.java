package de.jeb.japp.ai.service;

import de.jeb.japp.model.ai.AdapterType;

/** Default display-name suggestions offered when an admin creates a new provider instance. */
final class AdapterTypeDisplayNames {

    private AdapterTypeDisplayNames() {
    }

    static String defaultFor(AdapterType adapterType) {
        return switch (adapterType) {
            case PLACEHOLDER -> "Placeholder";
            case OPENAI_COMPATIBLE -> "OpenAI-Compatible Provider";
            case ANTHROPIC_MESSAGES -> "Anthropic";
            case GEMINI_GENERATE_CONTENT -> "Google Gemini";
        };
    }
}
