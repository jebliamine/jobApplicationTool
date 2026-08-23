package de.jeb.japp.generation.service.provider;

import de.jeb.japp.generation.service.provider.anthropic.AnthropicMessagesGenerationAdapter;
import de.jeb.japp.generation.service.provider.gemini.GeminiGenerateContentAdapter;
import de.jeb.japp.generation.service.provider.openai.OpenAiCompatibleGenerationAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Wires one shared RestClient (connect/read timeout only — every adapter sets its base URL
 * per-call from the resolved instance's configuration, since it can vary per admin-configured
 * instance) and the HTTP-based adapter beans that depend on it. PlaceholderCoverLetterGenerationAdapter
 * needs no HTTP client and is a plain {@code @Service} instead.
 */
@Configuration
class GenerationAdapterConfig {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    @Bean
    RestClient generationAdapterRestClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        int timeoutMillis = (int) DEFAULT_TIMEOUT.toMillis();
        requestFactory.setConnectTimeout(timeoutMillis);
        requestFactory.setReadTimeout(timeoutMillis);

        return RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    @Bean
    GeminiGenerateContentAdapter geminiGenerateContentAdapter(RestClient generationAdapterRestClient) {
        return new GeminiGenerateContentAdapter(generationAdapterRestClient);
    }

    @Bean
    OpenAiCompatibleGenerationAdapter openAiCompatibleGenerationAdapter(RestClient generationAdapterRestClient) {
        return new OpenAiCompatibleGenerationAdapter(generationAdapterRestClient);
    }

    @Bean
    AnthropicMessagesGenerationAdapter anthropicMessagesGenerationAdapter(RestClient generationAdapterRestClient) {
        return new AnthropicMessagesGenerationAdapter(generationAdapterRestClient);
    }
}
