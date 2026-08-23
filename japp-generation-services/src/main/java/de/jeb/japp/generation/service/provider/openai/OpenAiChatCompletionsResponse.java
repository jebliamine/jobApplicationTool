package de.jeb.japp.generation.service.provider.openai;

import java.util.List;

/** Minimal response body for the OpenAI-compatible /chat/completions endpoint. Never exposed outside this package. */
record OpenAiChatCompletionsResponse(List<Choice> choices) {

    record Choice(Message message) {
    }

    record Message(String role, String content) {
    }

    /** The first choice's message content, or null if the response has no usable text (empty/malformed). */
    String firstText() {
        if (choices == null || choices.isEmpty()) {
            return null;
        }
        Message message = choices.get(0).message();
        return message != null ? message.content() : null;
    }
}
