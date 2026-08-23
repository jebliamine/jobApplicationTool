package de.jeb.japp.generation.service.provider.anthropic;

import de.jeb.japp.ai.service.ResolvedProviderConfig;
import de.jeb.japp.commons.exceptions.generation.CoverLetterGenerationException;
import de.jeb.japp.generation.service.provider.GenerationInput;
import de.jeb.japp.generation.service.provider.GenerationResult;
import de.jeb.japp.model.ai.AdapterType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class AnthropicMessagesGenerationAdapterTest {

    private static final String BASE_URL = "https://anthropic.test";
    private static final String MODEL = "claude-sonnet-4";
    private static final String EXPECTED_URI = BASE_URL + "/v1/messages";
    private static final String API_KEY = "test-api-key";

    private RestClient.Builder restClientBuilder;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        restClientBuilder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
    }

    private AnthropicMessagesGenerationAdapter adapter() {
        return new AnthropicMessagesGenerationAdapter(restClientBuilder.build());
    }

    private ResolvedProviderConfig availableConfig() {
        return new ResolvedProviderConfig(true, API_KEY, MODEL, BASE_URL);
    }

    private GenerationInput validInput() {
        return new GenerationInput(
                "Backend Engineer", "Acme Corp", "Build and maintain backend services.",
                "My Resume", null, "Jane Doe");
    }

    private void expectSuccess() {
        mockServer.expect(requestTo(EXPECTED_URI))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("x-api-key", API_KEY))
                .andExpect(header("anthropic-version", "2023-06-01"))
                .andRespond(withSuccess(
                        "{\"content\":[{\"type\":\"text\",\"text\":\"Dear Hiring Team...\"}]}",
                        MediaType.APPLICATION_JSON));
    }

    @Test
    void isRegisteredUnderTheAnthropicMessagesType() {
        assertThat(adapter().type()).isEqualTo(AdapterType.ANTHROPIC_MESSAGES);
    }

    @Test
    void successfulResponseProducesGenerationResult() {
        expectSuccess();

        GenerationResult result = adapter().generate(availableConfig(), validInput());

        assertThat(result.content()).isEqualTo("Dear Hiring Team...");
        mockServer.verify();
    }

    @Test
    void emptyContentListFails() {
        mockServer.expect(requestTo(EXPECTED_URI))
                .andRespond(withSuccess("{\"content\":[]}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> adapter().generate(availableConfig(), validInput()))
                .isInstanceOf(CoverLetterGenerationException.class);
    }

    @Test
    void malformedJsonResponseFails() {
        mockServer.expect(requestTo(EXPECTED_URI))
                .andRespond(withSuccess("not json at all", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> adapter().generate(availableConfig(), validInput()))
                .isInstanceOf(CoverLetterGenerationException.class);
    }

    @Test
    void unauthorizedIsNotRetried() {
        mockServer.expect(requestTo(EXPECTED_URI)).andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        assertThatThrownBy(() -> adapter().generate(availableConfig(), validInput()))
                .isInstanceOf(CoverLetterGenerationException.class)
                .hasMessageContaining("HTTP 401");

        mockServer.verify();
    }

    @Test
    void serverErrorIsRetriedAndSucceeds() {
        mockServer.expect(requestTo(EXPECTED_URI)).andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));
        expectSuccess();

        GenerationResult result = adapter().generate(availableConfig(), validInput());

        assertThat(result.content()).isEqualTo("Dear Hiring Team...");
        mockServer.verify();
    }

    @Test
    void serverErrorOnAllAttemptsFailsWithoutLeakingTheApiKey() {
        mockServer.expect(requestTo(EXPECTED_URI)).andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));
        mockServer.expect(requestTo(EXPECTED_URI)).andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));
        mockServer.expect(requestTo(EXPECTED_URI)).andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        Throwable thrown = org.assertj.core.api.Assertions.catchThrowable(
                () -> adapter().generate(availableConfig(), validInput()));

        assertThat(thrown).isInstanceOf(CoverLetterGenerationException.class);
        assertThat(thrown.getMessage()).doesNotContain(API_KEY);
        mockServer.verify();
    }

    @Test
    void unavailableConfigurationFailsCleanlyWithoutMakingARequest() {
        ResolvedProviderConfig unavailable = new ResolvedProviderConfig(false, null, MODEL, BASE_URL);

        assertThatThrownBy(() -> adapter().generate(unavailable, validInput()))
                .isInstanceOf(CoverLetterGenerationException.class);

        mockServer.verify();
    }

    @Test
    void blankJobDescriptionFailsCleanlyWithoutMakingARequest() {
        GenerationInput input = new GenerationInput("Backend Engineer", "Acme Corp", "  ", "My Resume", null, "Jane Doe");

        assertThatThrownBy(() -> adapter().generate(availableConfig(), input))
                .isInstanceOf(CoverLetterGenerationException.class);

        mockServer.verify();
    }
}
