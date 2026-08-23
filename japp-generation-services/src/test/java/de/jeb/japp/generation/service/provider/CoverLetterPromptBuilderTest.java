package de.jeb.japp.generation.service.provider;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CoverLetterPromptBuilderTest {

    @Test
    void includesTheFullCvTextWhenPresent() {
        GenerationInput input = new GenerationInput(
                "Backend Engineer", "Acme Corp", "Build things.",
                "My Resume", "Extracted CV content about ten years of Java experience.", "Jane Doe");

        String prompt = CoverLetterPromptBuilder.build(input);

        assertThat(prompt).contains("Extracted CV content about ten years of Java experience.");
        assertThat(prompt).doesNotContain("Titel des hinterlegten Lebenslauf-Dokuments");
    }

    @Test
    void fallsBackToTheCvTitleWhenNoTextWasExtracted() {
        GenerationInput input = new GenerationInput(
                "Backend Engineer", "Acme Corp", "Build things.", "My Resume", null, "Jane Doe");

        String prompt = CoverLetterPromptBuilder.build(input);

        assertThat(prompt).contains("Titel des hinterlegten Lebenslauf-Dokuments: My Resume");
    }

    @Test
    void omitsCvInformationEntirelyWhenNeitherIsAvailable() {
        GenerationInput input = new GenerationInput(
                "Backend Engineer", "Acme Corp", "Build things.", null, null, "Jane Doe");

        String prompt = CoverLetterPromptBuilder.build(input);

        assertThat(prompt).doesNotContain("Lebenslauf");
    }

    @Test
    void blankCvTextFallsBackToTheTitle() {
        GenerationInput input = new GenerationInput(
                "Backend Engineer", "Acme Corp", "Build things.", "My Resume", "   ", "Jane Doe");

        String prompt = CoverLetterPromptBuilder.build(input);

        assertThat(prompt).contains("Titel des hinterlegten Lebenslauf-Dokuments: My Resume");
    }
}
