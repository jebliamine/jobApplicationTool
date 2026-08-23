package de.jeb.japp.generation.service.provider.openai;

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

class OpenAiCompatibleGenerationAdapterTest {

    private static final String BASE_URL = "https://openai.test/v1";
    private static final String MODEL = "gpt-4o-mini";
    private static final String EXPECTED_URI = BASE_URL + "/chat/completions";
    private static final String API_KEY = "test-api-key";

    private RestClient.Builder restClientBuilder;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        restClientBuilder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
    }

    private OpenAiCompatibleGenerationAdapter adapter() {
        return new OpenAiCompatibleGenerationAdapter(restClientBuilder.build());
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
                .andExpect(header("Authorization", "Bearer " + API_KEY))
                .andRespond(withSuccess(
                        "{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":\"Dear Hiring Team...\"}}]}",
                        MediaType.APPLICATION_JSON));
    }

    @Test
    void isRegisteredUnderTheOpenAiCompatibleType() {
        assertThat(adapter().type()).isEqualTo(AdapterType.OPENAI_COMPATIBLE);
    }

    @Test
    void successfulResponseProducesGenerationResult() {
        expectSuccess();

        GenerationResult result = adapter().generate(availableConfig(), validInput());

        assertThat(result.content()).isEqualTo("Dear Hiring Team...");
        mockServer.verify();
    }

    @Test
    void omitsTheAuthorizationHeaderWhenNoApiKeyIsConfigured() {
        mockServer.expect(requestTo(EXPECTED_URI))
                .andExpect(headerDoesNotExist("Authorization"))
                .andRespond(withSuccess(
                        "{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":\"Local response\"}}]}",
                        MediaType.APPLICATION_JSON));

        ResolvedProviderConfig localNoAuth = new ResolvedProviderConfig(true, null, MODEL, BASE_URL);
        GenerationResult result = adapter().generate(localNoAuth, validInput());

        assertThat(result.content()).isEqualTo("Local response");
        mockServer.verify();
    }

    @Test
    void emptyChoicesListFails() {
        mockServer.expect(requestTo(EXPECTED_URI))
                .andRespond(withSuccess("{\"choices\":[]}", MediaType.APPLICATION_JSON));

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
