package de.jeb.japp.generation.service.provider.gemini;

import de.jeb.japp.ai.service.ProviderSettingsResolver;
import de.jeb.japp.ai.service.gemini.GeminiProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Wires the Gemini provider bean with a RestClient configured for its
 * connect/read timeout only. Timeout stays environment/application.yml-only
 * (not part of the admin-configurable schema); base URL is resolved per-call
 * by GeminiCoverLetterGenerationProvider via ProviderSettingsResolver, so it
 * is intentionally not set here.
 */
@Configuration
class GeminiConfig {

    @Bean
    GeminiCoverLetterGenerationProvider geminiCoverLetterGenerationProvider(
            ProviderSettingsResolver resolver, GeminiProperties properties
    ) {
        return new GeminiCoverLetterGenerationProvider(resolver, buildRestClient(properties));
    }

    private RestClient buildRestClient(GeminiProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        int timeoutMillis = (int) properties.getTimeout().toMillis();
        requestFactory.setConnectTimeout(timeoutMillis);
        requestFactory.setReadTimeout(timeoutMillis);

        return RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }
}
