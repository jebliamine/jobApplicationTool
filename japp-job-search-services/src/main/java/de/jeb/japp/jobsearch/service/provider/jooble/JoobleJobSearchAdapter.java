package de.jeb.japp.jobsearch.service.provider.jooble;

import de.jeb.japp.jobsearch.service.provider.ExternalJobSearchAdapter;
import de.jeb.japp.model.jobsearch.ExternalJobSource;
import de.jeb.japp.model.jobsearch.dto.ExternalJobListing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

/**
 * Calls Jooble's {@code POST /api/{key}} REST API (<a href="https://jooble.org/api/about">docs</a>).
 * Disabled (returns no results, never called) unless {@code JOOBLE_API_KEY} is set — see
 * {@link #isConfigured()}. Never throws: every failure is logged and swallowed so one bad source
 * doesn't fail the whole aggregated search.
 */
public class JoobleJobSearchAdapter implements ExternalJobSearchAdapter {

    private static final Logger log = LoggerFactory.getLogger(JoobleJobSearchAdapter.class);
    private static final String BASE_URL = "https://jooble.org/api/";

    private final RestClient restClient;
    private final String apiKey;

    public JoobleJobSearchAdapter(RestClient restClient, @Value("${job-search.jooble.api-key:}") String apiKey) {
        this.restClient = restClient;
        this.apiKey = apiKey;
    }

    @Override
    public ExternalJobSource source() {
        return ExternalJobSource.JOOBLE;
    }

    @Override
    public boolean isConfigured() {
        return !apiKey.isBlank();
    }

    @Override
    public List<ExternalJobListing> search(String keyword, String location, int page) {
        if (!isConfigured()) {
            return List.of();
        }

        try {
            JoobleSearchResponse response = restClient.post()
                    .uri(BASE_URL + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new JoobleSearchRequest(keyword, location, Math.max(page, 1)))
                    .retrieve()
                    .body(JoobleSearchResponse.class);

            if (response == null || response.getJobs() == null) {
                return List.of();
            }

            return response.getJobs().stream().map(this::toListing).toList();
        } catch (RestClientException e) {
            log.warn("Jooble search failed (call swallowed, source omitted from results): {}", e.getMessage());
            return List.of();
        }
    }

    private ExternalJobListing toListing(JoobleSearchResponse.Job job) {
        ExternalJobListing listing = new ExternalJobListing();
        listing.setSource(ExternalJobSource.JOOBLE);
        listing.setExternalId(job.getId());
        listing.setTitle(job.getTitle());
        listing.setCompanyName(job.getCompany());
        listing.setDescription(job.getSnippet());
        listing.setLocation(job.getLocation());
        listing.setUrl(job.getLink());
        listing.setSalaryRange(job.getSalary());
        listing.setPostedAt(job.getUpdated());
        // Jooble's "type" is a free-text label (varies per source site, e.g. "Full-time" or a
        // language-localized equivalent) — not a stable enum to map against, so it's left out of
        // employmentType/workMode rather than guessed; the raw snippet still surfaces it to the user.
        return listing;
    }
}
