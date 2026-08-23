package de.jeb.japp.model.ai;

/**
 * The wire-protocol implementation a {@link AiProviderConfiguration} instance uses — a small,
 * closed set of Java-implemented adapters. Distinct from the instance itself: many instances can
 * share one adapter type (e.g. several OPENAI_COMPATIBLE instances pointing at different
 * accounts/models/local servers). Adding a new adapter type is a code change (a new adapter
 * class); admins can then create unlimited instances of it without any further code change.
 */
public enum AdapterType {
    /** Built-in, deterministic, no external call — not admin-creatable/deletable. */
    PLACEHOLDER,
    /** OpenAI's /v1/chat/completions shape — also covers OpenAI-compatible local LLM servers (Ollama, LM Studio, vLLM, etc.). */
    OPENAI_COMPATIBLE,
    /** Anthropic's /v1/messages shape. */
    ANTHROPIC_MESSAGES,
    /** Google Gemini's generateContent shape. */
    GEMINI_GENERATE_CONTENT
}
