package de.jeb.japp.generation.service.provider.anthropic;

import java.util.List;

/** Minimal response body for Anthropic's /v1/messages endpoint. Never exposed outside this package. */
record AnthropicMessagesResponse(List<ContentBlock> content) {

    record ContentBlock(String type, String text) {
    }

    /** The first text content block, or null if the response has no usable text (empty/malformed). */
    String firstText() {
        if (content == null || content.isEmpty()) {
            return null;
        }
        return content.get(0).text();
    }
}
