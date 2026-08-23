package de.jeb.japp.generation.service.provider;

import de.jeb.japp.ai.service.ResolvedProviderConfig;
import de.jeb.japp.commons.exceptions.generation.CoverLetterGenerationException;
import de.jeb.japp.model.ai.AdapterType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlaceholderCoverLetterGenerationAdapterTest {

    private final PlaceholderCoverLetterGenerationAdapter adapter = new PlaceholderCoverLetterGenerationAdapter();
    private final ResolvedProviderConfig config = new ResolvedProviderConfig(true, null, "deterministic-v1", null);

    @Test
    void isRegisteredUnderThePlaceholderType() {
        assertThat(adapter.type()).isEqualTo(AdapterType.PLACEHOLDER);
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
        GenerationResult result = adapter.generate(config, validInput());

        assertThat(result.content()).isNotBlank();
        assertThat(result.content()).contains("Backend Engineer");
        assertThat(result.content()).contains("Acme Corp");
        assertThat(result.content()).contains("My Resume");
        assertThat(result.content()).contains("Jane Doe");
    }

    @Test
    void isDeterministicForTheSameInput() {
        GenerationInput input = validInput();

        GenerationResult first = adapter.generate(config, input);
        GenerationResult second = adapter.generate(config, input);

        assertThat(first.content()).isEqualTo(second.content());
    }

    @Test
    void handlesAMissingCvTitleGracefully() {
        GenerationInput input = new GenerationInput(
                "Backend Engineer", "Acme Corp", "Build and maintain backend services.", null, null, "Jane Doe");

        GenerationResult result = adapter.generate(config, input);

        assertThat(result.content()).isNotBlank();
        assertThat(result.content()).doesNotContain("null");
    }

    @Test
    void referencesTheCvWithoutDumpingItsFullTextWhenExtractedTextIsPresent() {
        GenerationInput input = new GenerationInput(
                "Backend Engineer", "Acme Corp", "Build and maintain backend services.",
                "My Resume", "Full extracted CV text goes here.", "Jane Doe");

        GenerationResult result = adapter.generate(config, input);

        assertThat(result.content()).contains("my CV");
        assertThat(result.content()).doesNotContain("Full extracted CV text goes here.");
    }

    @Test
    void throwsWhenJobDescriptionIsBlank() {
        GenerationInput input = new GenerationInput(
                "Backend Engineer", "Acme Corp", "   ", "My Resume", null, "Jane Doe");

        assertThatThrownBy(() -> adapter.generate(config, input))
                .isInstanceOf(CoverLetterGenerationException.class);
    }

    @Test
    void throwsWhenJobDescriptionIsNull() {
        GenerationInput input = new GenerationInput(
                "Backend Engineer", "Acme Corp", null, "My Resume", null, "Jane Doe");

        assertThatThrownBy(() -> adapter.generate(config, input))
                .isInstanceOf(CoverLetterGenerationException.class);
    }
}
