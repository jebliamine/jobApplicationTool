package de.jeb.japp.generation.service.provider.cv;

import de.jeb.japp.generation.service.provider.anthropic.AnthropicCvProfileExtractionAdapter;
import de.jeb.japp.generation.service.provider.gemini.GeminiCvProfileExtractionAdapter;
import de.jeb.japp.generation.service.provider.openai.OpenAiCvProfileExtractionAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Wires the CV-profile-extraction adapter beans onto the RestClient already
 * defined in GenerationAdapterConfig (autowired here by type — kept
 * as a separate config class so that file, and its existing cover-letter
 * beans/tests, are untouched by this feature).
 * PlaceholderCvProfileExtractionAdapter needs no HTTP client and is a plain
 * {@code @Service} instead, same as its cover-letter counterpart.
 */
@Configuration
class CvProfileAdapterConfig {

    @Bean
    OpenAiCvProfileExtractionAdapter openAiCvProfileExtractionAdapter(RestClient generationAdapterRestClient) {
        return new OpenAiCvProfileExtractionAdapter(generationAdapterRestClient);
    }

    @Bean
    AnthropicCvProfileExtractionAdapter anthropicCvProfileExtractionAdapter(RestClient generationAdapterRestClient) {
        return new AnthropicCvProfileExtractionAdapter(generationAdapterRestClient);
    }

    @Bean
    GeminiCvProfileExtractionAdapter geminiCvProfileExtractionAdapter(RestClient generationAdapterRestClient) {
        return new GeminiCvProfileExtractionAdapter(generationAdapterRestClient);
    }
}
