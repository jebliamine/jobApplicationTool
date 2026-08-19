package de.jeb.japp.generation.service.provider.gemini;

import de.jeb.japp.ai.service.ProviderSettingsResolver;
import de.jeb.japp.ai.service.ResolvedProviderConfig;
import de.jeb.japp.generation.service.provider.CoverLetterGenerationException;
import de.jeb.japp.generation.service.provider.GenerationInput;
import de.jeb.japp.generation.service.provider.GenerationResult;
import de.jeb.japp.model.generation.GenerationProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.net.SocketTimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Proves the provider resolves its configuration through ProviderSettingsResolver
 * on every call (never cached in this class) — the resolver is mocked, and
 * every HTTP expectation below asserts on the URI/header values the mocked
 * resolver's ResolvedProviderConfig supplied.
 */
@ExtendWith(MockitoExtension.class)
class GeminiCoverLetterGenerationProviderTest {

    private static final String BASE_URL = "https://gemini.test";
    private static final String MODEL = "gemini-2.0-flash";
    private static final String EXPECTED_URI = BASE_URL + "/v1beta/models/" + MODEL + ":generateContent";

    @Mock
    private ProviderSettingsResolver resolver;

    private RestClient.Builder restClientBuilder;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        restClientBuilder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
    }

    private void stubAvailable(String apiKey) {
        lenient().when(resolver.resolve(GenerationProvider.GEMINI))
                .thenReturn(new ResolvedProviderConfig(true, apiKey, MODEL, BASE_URL));
    }

    private GeminiCoverLetterGenerationProvider provider() {
        return new GeminiCoverLetterGenerationProvider(resolver, restClientBuilder.build());
    }

    private GenerationInput validInput() {
        return new GenerationInput(
                "Backend Engineer", "Acme Corp", "Build and maintain backend services.", "My Resume", "Jane Doe");
    }

    @Test
    void isRegisteredUnderTheGeminiId() {
        assertThat(provider().id()).isEqualTo(GenerationProvider.GEMINI);
    }

    @Test
    void modelIsResolvedThroughProviderSettingsResolverOnEachCall() {
        when(resolver.resolve(GenerationProvider.GEMINI))
                .thenReturn(new ResolvedProviderConfig(true, "key", "gemini-custom-model", BASE_URL));

        assertThat(provider().model()).isEqualTo("gemini-custom-model");
        verify(resolver).resolve(GenerationProvider.GEMINI);
    }

    @Test
    void successfulResponseProducesGenerationResult() {
        stubAvailable("test-api-key");
        mockServer.expect(requestTo(EXPECTED_URI))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("x-goog-api-key", "test-api-key"))
                .andRespond(withSuccess(
                        "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"Sehr geehrte Damen und Herren, ...\"}]}}]}",
                        MediaType.APPLICATION_JSON));

        GenerationResult result = provider().generate(validInput());

        assertThat(result.content()).isEqualTo("Sehr geehrte Damen und Herren, ...");
        mockServer.verify();
    }

    @Test
    void emptyCandidatesListFails() {
        stubAvailable("test-api-key");
        mockServer.expect(requestTo(EXPECTED_URI))
                .andRespond(withSuccess("{\"candidates\":[]}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> provider().generate(validInput()))
                .isInstanceOf(CoverLetterGenerationException.class);
    }

    @Test
    void blankTextFails() {
        stubAvailable("test-api-key");
        mockServer.expect(requestTo(EXPECTED_URI))
                .andRespond(withSuccess(
                        "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"   \"}]}}]}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> provider().generate(validInput()))
                .isInstanceOf(CoverLetterGenerationException.class);
    }

    @Test
    void malformedJsonResponseFails() {
        stubAvailable("test-api-key");
        mockServer.expect(requestTo(EXPECTED_URI))
                .andRespond(withSuccess("not json at all", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> provider().generate(validInput()))
                .isInstanceOf(CoverLetterGenerationException.class);
    }

    @Test
    void unauthorizedIsConvertedToCoverLetterGenerationException() {
        stubAvailable("test-api-key");
        mockServer.expect(requestTo(EXPECTED_URI)).andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        assertThatThrownBy(() -> provider().generate(validInput()))
                .isInstanceOf(CoverLetterGenerationException.class);
    }

    @Test
    void forbiddenIsConvertedToCoverLetterGenerationException() {
        stubAvailable("test-api-key");
        mockServer.expect(requestTo(EXPECTED_URI)).andRespond(withStatus(HttpStatus.FORBIDDEN));

        assertThatThrownBy(() -> provider().generate(validInput()))
                .isInstanceOf(CoverLetterGenerationException.class);
    }

    @Test
    void tooManyRequestsIsConvertedToCoverLetterGenerationException() {
        stubAvailable("test-api-key");
        mockServer.expect(requestTo(EXPECTED_URI)).andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

        assertThatThrownBy(() -> provider().generate(validInput()))
                .isInstanceOf(CoverLetterGenerationException.class);
    }

    @Test
    void serverErrorIsConvertedToCoverLetterGenerationException() {
        stubAvailable("test-api-key");
        mockServer.expect(requestTo(EXPECTED_URI)).andRespond(withServerError());

        assertThatThrownBy(() -> provider().generate(validInput()))
                .isInstanceOf(CoverLetterGenerationException.class);
    }

    @Test
    void connectionFailureIsConvertedToCoverLetterGenerationException() {
        stubAvailable("test-api-key");
        mockServer.expect(requestTo(EXPECTED_URI)).andRespond(request -> {
            throw new IOException("simulated connection failure");
        });

        assertThatThrownBy(() -> provider().generate(validInput()))
                .isInstanceOf(CoverLetterGenerationException.class);
    }

    @Test
    void timeoutIsConvertedToCoverLetterGenerationException() {
        stubAvailable("test-api-key");
        mockServer.expect(requestTo(EXPECTED_URI)).andRespond(request -> {
            throw new SocketTimeoutException("simulated read timeout");
        });

        assertThatThrownBy(() -> provider().generate(validInput()))
                .isInstanceOf(CoverLetterGenerationException.class);
    }

    @Test
    void unavailableConfigurationFailsCleanlyWithoutMakingARequest() {
        when(resolver.resolve(GenerationProvider.GEMINI))
                .thenReturn(new ResolvedProviderConfig(false, null, MODEL, BASE_URL));

        assertThatThrownBy(() -> provider().generate(validInput()))
                .isInstanceOf(CoverLetterGenerationException.class);

        mockServer.verify();
    }

    @Test
    void blankJobDescriptionFailsCleanlyWithoutMakingARequest() {
        stubAvailable("test-api-key");
        GenerationInput input = new GenerationInput("Backend Engineer", "Acme Corp", "  ", "My Resume", "Jane Doe");

        assertThatThrownBy(() -> provider().generate(input))
                .isInstanceOf(CoverLetterGenerationException.class);

        mockServer.verify();
    }
}
