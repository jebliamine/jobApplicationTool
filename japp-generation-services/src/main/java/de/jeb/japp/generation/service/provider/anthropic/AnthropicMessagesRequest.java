package de.jeb.japp.generation.service.provider.anthropic;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** Minimal request body for Anthropic's /v1/messages endpoint. Never exposed outside this package. */
record AnthropicMessagesRequest(String model, @JsonProperty("max_tokens") int maxTokens, List<Message> messages) {

    /** Anthropic requires max_tokens; a cover letter comfortably fits well under this. */
    static final int DEFAULT_MAX_TOKENS = 4096;

    record Message(String role, String content) {
    }

    static AnthropicMessagesRequest ofPrompt(String model, String prompt) {
        return new AnthropicMessagesRequest(model, DEFAULT_MAX_TOKENS, List.of(new Message("user", prompt)));
    }
}
