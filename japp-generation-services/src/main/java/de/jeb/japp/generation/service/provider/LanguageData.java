package de.jeb.japp.generation.service.provider;

/**
 * One language entry as extracted by an LLM — {@code level} is whatever free-text proficiency the
 * CV states ("native", "C1", "fluent", ...), never normalized to a fixed scale.
 */
public record LanguageData(String name, String level) {
}
