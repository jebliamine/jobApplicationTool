package de.jeb.japp.generation.service.provider.gemini;

import java.util.List;

/** Minimal response body for Gemini's {@code generateContent} endpoint. Never exposed outside this package. */
record GeminiGenerateContentResponse(List<Candidate> candidates) {

    record Candidate(Content content) {
    }

    record Content(List<Part> parts) {
    }

    record Part(String text) {
    }

    /** The first candidate's text, or null if the response has no usable text (empty/malformed). */
    String firstText() {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }
        Content content = candidates.get(0).content();
        if (content == null || content.parts() == null || content.parts().isEmpty()) {
            return null;
        }
        return content.parts().get(0).text();
    }
}
