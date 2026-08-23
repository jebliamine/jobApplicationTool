package de.jeb.japp.generation.service.provider.openai;

import java.util.List;

/** Minimal request body for the OpenAI-compatible /chat/completions endpoint. Never exposed outside this package. */
record OpenAiChatCompletionsRequest(String model, List<Message> messages) {

    record Message(String role, String content) {
    }

    static OpenAiChatCompletionsRequest ofPrompt(String model, String prompt) {
        return new OpenAiChatCompletionsRequest(model, List.of(new Message("user", prompt)));
    }
}
