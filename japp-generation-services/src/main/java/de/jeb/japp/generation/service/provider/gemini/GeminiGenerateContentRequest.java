package de.jeb.japp.generation.service.provider.gemini;

import java.util.List;

/** Minimal request body for Gemini's {@code generateContent} endpoint. Never exposed outside this package. */
record GeminiGenerateContentRequest(List<Content> contents) {

    record Content(List<Part> parts) {
    }

    record Part(String text) {
    }

    static GeminiGenerateContentRequest ofPrompt(String prompt) {
        return new GeminiGenerateContentRequest(List.of(new Content(List.of(new Part(prompt)))));
    }
}
