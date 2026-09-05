package de.jeb.japp.jobsearch.service.provider;

import de.jeb.japp.jobsearch.service.provider.adzuna.AdzunaJobSearchAdapter;
import de.jeb.japp.jobsearch.service.provider.jooble.JoobleJobSearchAdapter;
import de.jeb.japp.jobsearch.service.provider.jsearch.JSearchJobSearchAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Wires one shared RestClient (connect/read timeout only) and the three external job-search
 * adapter beans — same shape as {@code GenerationAdapterConfig} in japp-generation-services.
 * Each adapter bean is always created; whether it actually calls out is decided per-request by
 * its own {@code isConfigured()} (true once its env-var-backed key(s) are set).
 */
@Configuration
class JobSearchAdapterConfig {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(15);

    @Bean
    RestClient jobSearchAdapterRestClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        int timeoutMillis = (int) DEFAULT_TIMEOUT.toMillis();
        requestFactory.setConnectTimeout(timeoutMillis);
        requestFactory.setReadTimeout(timeoutMillis);

        return RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    @Bean
    AdzunaJobSearchAdapter adzunaJobSearchAdapter(
            RestClient jobSearchAdapterRestClient,
            @Value("${job-search.adzuna.app-id:}") String appId,
            @Value("${job-search.adzuna.app-key:}") String appKey,
            @Value("${job-search.adzuna.country:de}") String country,
            @Value("${job-search.adzuna.results-per-page:20}") int resultsPerPage
    ) {
        return new AdzunaJobSearchAdapter(jobSearchAdapterRestClient, appId, appKey, country, resultsPerPage);
    }

    @Bean
    JoobleJobSearchAdapter joobleJobSearchAdapter(
            RestClient jobSearchAdapterRestClient,
            @Value("${job-search.jooble.api-key:}") String apiKey
    ) {
        return new JoobleJobSearchAdapter(jobSearchAdapterRestClient, apiKey);
    }

    @Bean
    JSearchJobSearchAdapter jSearchJobSearchAdapter(
            RestClient jobSearchAdapterRestClient,
            @Value("${job-search.jsearch.rapidapi-key:}") String rapidApiKey
    ) {
        return new JSearchJobSearchAdapter(jobSearchAdapterRestClient, rapidApiKey);
    }
}
