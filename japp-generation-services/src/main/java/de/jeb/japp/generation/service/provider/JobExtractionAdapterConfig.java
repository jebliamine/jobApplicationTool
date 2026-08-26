package de.jeb.japp.generation.service.provider;

import de.jeb.japp.generation.service.provider.anthropic.AnthropicJobExtractionAdapter;
import de.jeb.japp.generation.service.provider.gemini.GeminiJobExtractionAdapter;
import de.jeb.japp.generation.service.provider.openai.OpenAiJobExtractionAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Wires the job-extraction adapter beans onto the RestClient already defined in
 * {@link GenerationAdapterConfig} (autowired here by type — kept as a separate config class, same
 * pattern as {@link CvProfileAdapterConfig}). PlaceholderJobExtractionAdapter needs no HTTP client
 * and is a plain {@code @Service} instead.
 */
@Configuration
class JobExtractionAdapterConfig {

    @Bean
    OpenAiJobExtractionAdapter openAiJobExtractionAdapter(RestClient generationAdapterRestClient) {
        return new OpenAiJobExtractionAdapter(generationAdapterRestClient);
    }

    @Bean
    AnthropicJobExtractionAdapter anthropicJobExtractionAdapter(RestClient generationAdapterRestClient) {
        return new AnthropicJobExtractionAdapter(generationAdapterRestClient);
    }

    @Bean
    GeminiJobExtractionAdapter geminiJobExtractionAdapter(RestClient generationAdapterRestClient) {
        return new GeminiJobExtractionAdapter(generationAdapterRestClient);
    }
}
