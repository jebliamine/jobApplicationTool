package de.jeb.japp.generation.service.provider.gemini;

import de.jeb.japp.ai.service.ProviderSettingsResolver;
import de.jeb.japp.ai.service.encryption.AiCredentialEncryptor;
import de.jeb.japp.ai.service.encryption.SpringSecurityAiCredentialEncryptor;
import de.jeb.japp.ai.service.gemini.GeminiProperties;
import de.jeb.japp.dao.ai.AiProviderConfigurationDao;
import de.jeb.japp.generation.service.provider.GenerationInput;
import de.jeb.japp.generation.service.provider.GenerationResult;
import de.jeb.japp.model.ai.AiProviderConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Regression test for the reported bug: prove the provider actually sends
 * the database-configured api key/model/base URL, not stale
 * environment/startup configuration — using the REAL ProviderSettingsResolver
 * and REAL encryption round-trip (only the DAO and the HTTP transport are
 * test doubles). A resolver mock could not catch a regression in this wiring;
 * this test exercises the exact production chain the reported bug lives in.
 */
@ExtendWith(MockitoExtension.class)
class GeminiCoverLetterGenerationProviderDbConfigurationTest {

    @Mock
    private AiProviderConfigurationDao dao;

    @Test
    void usesTheDatabaseConfiguredApiKeyModelAndBaseUrlNotEnvironmentDefaults() {
        String dbApiKey = "db-configured-real-key";
        String dbModel = "gemini-3.7-flash";
        String dbBaseUrl = "https://gemini-db-configured.example";

        AiCredentialEncryptor realEncryptor = new SpringSecurityAiCredentialEncryptor("test-encryption-key");
        AiProviderConfiguration row = new AiProviderConfiguration();
        row.setProvider("GEMINI");
        row.setEnabled(true);
        row.setEncryptedApiKey(realEncryptor.encrypt(dbApiKey));
        row.setDefaultModel(dbModel);
        row.setBaseUrl(dbBaseUrl);
        when(dao.getByProvider("GEMINI")).thenReturn(Optional.of(row));

        // Deliberately different from the DB values, to prove DB precedence rather
        // than an accidental match.
        GeminiProperties envProperties = new GeminiProperties();
        envProperties.setApiKey("env-key-should-not-be-used");
        envProperties.setModel("gemini-2.0-flash");
        envProperties.setBaseUrl("https://env-should-not-be-used.example");
        envProperties.setTimeout(Duration.ofSeconds(5));

        ProviderSettingsResolver resolver = new ProviderSettingsResolver(dao, realEncryptor, envProperties);

        RestClient.Builder restClientBuilder = RestClient.builder();
        MockRestServiceServer mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        GeminiCoverLetterGenerationProvider provider =
                new GeminiCoverLetterGenerationProvider(resolver, restClientBuilder.build());

        mockServer.expect(requestTo(dbBaseUrl + "/v1beta/models/" + dbModel + ":generateContent"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("x-goog-api-key", dbApiKey))
                .andRespond(withSuccess(
                        "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"Generated via DB config.\"}]}}]}",
                        MediaType.APPLICATION_JSON));

        GenerationInput input =
                new GenerationInput("Backend Engineer", "Acme Corp", "Build things.", "My Resume", "Jane Doe");
        GenerationResult result = provider.generate(input);

        assertThat(result.content()).isEqualTo("Generated via DB config.");
        assertThat(provider.model()).isEqualTo(dbModel);
        mockServer.verify();
    }
}
