package de.jeb.japp.generation.service.provider;

import de.jeb.japp.commons.exceptions.generation.CoverLetterGenerationException;
import de.jeb.japp.model.generation.GenerationProvider;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlaceholderCoverLetterGenerationProviderTest {

    private final PlaceholderCoverLetterGenerationProvider provider = new PlaceholderCoverLetterGenerationProvider();

    @Test
    void isRegisteredUnderThePlaceholderId() {
        assertThat(provider.id()).isEqualTo(GenerationProvider.PLACEHOLDER);
        assertThat(provider.model()).isEqualTo("deterministic-v1");
    }

    private GenerationInput validInput() {
        return new GenerationInput(
                "Backend Engineer",
                "Acme Corp",
                "Build and maintain backend services.",
                "My Resume",
                null,
                "Jane Doe"
        );
    }

    @Test
    void receivesValidInputAndReturnsNonEmptyResult() {
        GenerationResult result = provider.generate(validInput());

        assertThat(result.content()).isNotBlank();
        assertThat(result.content()).contains("Backend Engineer");
        assertThat(result.content()).contains("Acme Corp");
        assertThat(result.content()).contains("My Resume");
        assertThat(result.content()).contains("Jane Doe");
    }

    @Test
    void isDeterministicForTheSameInput() {
        GenerationInput input = validInput();

        GenerationResult first = provider.generate(input);
        GenerationResult second = provider.generate(input);

        assertThat(first.content()).isEqualTo(second.content());
    }

    @Test
    void handlesAMissingCvTitleGracefully() {
        GenerationInput input = new GenerationInput(
                "Backend Engineer", "Acme Corp", "Build and maintain backend services.", null, null, "Jane Doe");

        GenerationResult result = provider.generate(input);

        assertThat(result.content()).isNotBlank();
        assertThat(result.content()).doesNotContain("null");
    }

    @Test
    void referencesTheCvWithoutDumpingItsFullTextWhenExtractedTextIsPresent() {
        GenerationInput input = new GenerationInput(
                "Backend Engineer", "Acme Corp", "Build and maintain backend services.",
                "My Resume", "Full extracted CV text goes here.", "Jane Doe");

        GenerationResult result = provider.generate(input);

        assertThat(result.content()).contains("my CV");
        assertThat(result.content()).doesNotContain("Full extracted CV text goes here.");
    }

    @Test
    void throwsWhenJobDescriptionIsBlank() {
        GenerationInput input = new GenerationInput(
                "Backend Engineer", "Acme Corp", "   ", "My Resume", null, "Jane Doe");

        assertThatThrownBy(() -> provider.generate(input))
                .isInstanceOf(CoverLetterGenerationException.class);
    }

    @Test
    void throwsWhenJobDescriptionIsNull() {
        GenerationInput input = new GenerationInput(
                "Backend Engineer", "Acme Corp", null, "My Resume", null, "Jane Doe");

        assertThatThrownBy(() -> provider.generate(input))
                .isInstanceOf(CoverLetterGenerationException.class);
    }
}
