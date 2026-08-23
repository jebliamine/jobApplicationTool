package de.jeb.japp.generation.service.provider.gemini;

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

import java.io.IOException;
import java.net.SocketTimeoutException;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

/**
 * Unlike the pre-dynamic-provider version of this test, configuration is passed directly as a
 * {@link ResolvedProviderConfig} rather than resolved through a mocked ProviderSettingsResolver —
 * the adapter no longer resolves its own config, so there's nothing left to mock there.
 */
class GeminiGenerateContentAdapterTest {

    private static final String BASE_URL = "https://gemini.test";
    private static final String MODEL = "gemini-2.0-flash";
    private static final String EXPECTED_URI = BASE_URL + "/v1beta/models/" + MODEL + ":generateContent";
    private static final String API_KEY = "test-api-key";

    private RestClient.Builder restClientBuilder;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        restClientBuilder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
    }

    private GeminiGenerateContentAdapter adapter() {
        return new GeminiGenerateContentAdapter(restClientBuilder.build());
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
                .andExpect(header("x-goog-api-key", API_KEY))
                .andRespond(withSuccess(
                        "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"Sehr geehrte Damen und Herren, ...\"}]}}]}",
                        MediaType.APPLICATION_JSON));
    }

    private void expectStatus(HttpStatus status) {
        mockServer.expect(requestTo(EXPECTED_URI)).andRespond(withStatus(status));
    }

    @Test
    void isRegisteredUnderTheGeminiGenerateContentType() {
        assertThat(adapter().type()).isEqualTo(AdapterType.GEMINI_GENERATE_CONTENT);
    }

    @Test
    void successfulResponseProducesGenerationResult() {
        expectSuccess();

        GenerationResult result = adapter().generate(availableConfig(), validInput());

        assertThat(result.content()).isEqualTo("Sehr geehrte Damen und Herren, ...");
        mockServer.verify();
    }

    @Test
    void emptyCandidatesListFails() {
        mockServer.expect(requestTo(EXPECTED_URI))
                .andRespond(withSuccess("{\"candidates\":[]}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> adapter().generate(availableConfig(), validInput()))
                .isInstanceOf(CoverLetterGenerationException.class);
    }

    @Test
    void blankTextFails() {
        mockServer.expect(requestTo(EXPECTED_URI))
                .andRespond(withSuccess(
                        "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"   \"}]}}]}", MediaType.APPLICATION_JSON));

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
    void unauthorizedIsNotRetriedAndReportsTheStatusCode() {
        expectStatus(HttpStatus.UNAUTHORIZED);

        assertThatThrownBy(() -> adapter().generate(availableConfig(), validInput()))
                .isInstanceOf(CoverLetterGenerationException.class)
                .hasMessageContaining("HTTP 401")
                .hasMessageContaining("authentication");

        mockServer.verify();
    }

    @Test
    void forbiddenIsNotRetried() {
        expectStatus(HttpStatus.FORBIDDEN);

        assertThatThrownBy(() -> adapter().generate(availableConfig(), validInput()))
                .isInstanceOf(CoverLetterGenerationException.class)
                .hasMessageContaining("HTTP 403");

        mockServer.verify();
    }

    @Test
    void badRequestIsNotRetried() {
        expectStatus(HttpStatus.BAD_REQUEST);

        assertThatThrownBy(() -> adapter().generate(availableConfig(), validInput()))
                .isInstanceOf(CoverLetterGenerationException.class)
                .hasMessageContaining("HTTP 400");

        mockServer.verify();
    }

    @Test
    void notFoundIsNotRetried() {
        expectStatus(HttpStatus.NOT_FOUND);

        assertThatThrownBy(() -> adapter().generate(availableConfig(), validInput()))
                .isInstanceOf(CoverLetterGenerationException.class)
                .hasMessageContaining("HTTP 404");

        mockServer.verify();
    }

    @Test
    void connectionFailureIsNotRetried() {
        mockServer.expect(requestTo(EXPECTED_URI)).andRespond(request -> {
            throw new IOException("simulated connection failure");
        });

        assertThatThrownBy(() -> adapter().generate(availableConfig(), validInput()))
                .isInstanceOf(CoverLetterGenerationException.class);

        mockServer.verify();
    }

    @Test
    void timeoutIsNotRetried() {
        mockServer.expect(requestTo(EXPECTED_URI)).andRespond(request -> {
            throw new SocketTimeoutException("simulated read timeout");
        });

        assertThatThrownBy(() -> adapter().generate(availableConfig(), validInput()))
                .isInstanceOf(CoverLetterGenerationException.class);

        mockServer.verify();
    }

    @Test
    void serverErrorOnFirstAttemptIsRetriedAndSucceedsOnTheSecond() {
        expectStatus(HttpStatus.SERVICE_UNAVAILABLE);
        expectSuccess();

        GenerationResult result = adapter().generate(availableConfig(), validInput());

        assertThat(result.content()).isEqualTo("Sehr geehrte Damen und Herren, ...");
        mockServer.verify();
    }

    @Test
    void serverErrorOnFirstTwoAttemptsIsRetriedAndSucceedsOnTheThird() {
        expectStatus(HttpStatus.SERVICE_UNAVAILABLE);
        expectStatus(HttpStatus.SERVICE_UNAVAILABLE);
        expectSuccess();

        GenerationResult result = adapter().generate(availableConfig(), validInput());

        assertThat(result.content()).isEqualTo("Sehr geehrte Damen und Herren, ...");
        mockServer.verify();
    }

    @Test
    void serverErrorOnAllAttemptsFailsWithTheStatusCodeAndNeverLeaksTheApiKey() {
        expectStatus(HttpStatus.SERVICE_UNAVAILABLE);
        expectStatus(HttpStatus.SERVICE_UNAVAILABLE);
        expectStatus(HttpStatus.SERVICE_UNAVAILABLE);

        Throwable thrown = catchThrowable(() -> adapter().generate(availableConfig(), validInput()));

        assertThat(thrown).isInstanceOf(CoverLetterGenerationException.class);
        assertThat(thrown.getMessage()).contains("HTTP 503").contains("unavailable");
        assertThat(thrown.getMessage()).doesNotContain(API_KEY);
        mockServer.verify();
    }

    @Test
    void tooManyRequestsOnFirstTwoAttemptsIsRetriedAndSucceedsOnTheThird() {
        expectStatus(HttpStatus.TOO_MANY_REQUESTS);
        expectStatus(HttpStatus.TOO_MANY_REQUESTS);
        expectSuccess();

        GenerationResult result = adapter().generate(availableConfig(), validInput());

        assertThat(result.content()).isEqualTo("Sehr geehrte Damen und Herren, ...");
        mockServer.verify();
    }

    @Test
    void tooManyRequestsOnAllAttemptsFailsWithTheStatusCode() {
        expectStatus(HttpStatus.TOO_MANY_REQUESTS);
        expectStatus(HttpStatus.TOO_MANY_REQUESTS);
        expectStatus(HttpStatus.TOO_MANY_REQUESTS);

        assertThatThrownBy(() -> adapter().generate(availableConfig(), validInput()))
                .isInstanceOf(CoverLetterGenerationException.class)
                .hasMessageContaining("HTTP 429")
                .hasMessageContaining("rate limit");

        mockServer.verify();
    }

    @Test
    void genericServerErrorReportsTheStatusCode() {
        mockServer.expect(requestTo(EXPECTED_URI)).andRespond(withServerError());
        mockServer.expect(requestTo(EXPECTED_URI)).andRespond(withServerError());
        mockServer.expect(requestTo(EXPECTED_URI)).andRespond(withServerError());

        assertThatThrownBy(() -> adapter().generate(availableConfig(), validInput()))
                .isInstanceOf(CoverLetterGenerationException.class)
                .hasMessageContaining("HTTP 500");

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
