package de.jeb.japp.jobsearch.service.provider.jsearch;

import de.jeb.japp.jobsearch.service.provider.ExternalJobSearchAdapter;
import de.jeb.japp.model.job.EmploymentType;
import de.jeb.japp.model.job.WorkMode;
import de.jeb.japp.model.jobsearch.ExternalJobSource;
import de.jeb.japp.model.jobsearch.dto.ExternalJobListing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Locale;

/**
 * Calls JSearch (<a href="https://rapidapi.com/letscrape-6bRBa3QguO5/api/jsearch">RapidAPI</a>)'s
 * {@code GET /search} endpoint — an aggregator over LinkedIn/Indeed/Glassdoor and more. Disabled
 * (returns no results, never called) unless {@code JSEARCH_RAPIDAPI_KEY} is set — see
 * {@link #isConfigured()}. Never throws: every failure is logged and swallowed so one bad source
 * doesn't fail the whole aggregated search.
 */
public class JSearchJobSearchAdapter implements ExternalJobSearchAdapter {

    private static final Logger log = LoggerFactory.getLogger(JSearchJobSearchAdapter.class);
    private static final String HOST = "jsearch.p.rapidapi.com";
    private static final String BASE_URL = "https://" + HOST + "/search";

    private final RestClient restClient;
    private final String rapidApiKey;

    public JSearchJobSearchAdapter(RestClient restClient, @Value("${job-search.jsearch.rapidapi-key:}") String rapidApiKey) {
        this.restClient = restClient;
        this.rapidApiKey = rapidApiKey;
    }

    @Override
    public ExternalJobSource source() {
        return ExternalJobSource.JSEARCH;
    }

    @Override
    public boolean isConfigured() {
        return !rapidApiKey.isBlank();
    }

    @Override
    public List<ExternalJobListing> search(String keyword, String location, int page) {
        if (!isConfigured()) {
            return List.of();
        }

        try {
            String query = String.join(" ", nonBlank(keyword), nonBlank(location)).trim();
            String uri = UriComponentsBuilder
                    .fromUriString(BASE_URL)
                    .queryParam("query", query.isBlank() ? "jobs" : query)
                    .queryParam("page", Math.max(page, 1))
                    .queryParam("num_pages", 1)
                    .build()
                    .toUriString();

            // Split out of the fluent chain on purpose: chaining .uri(String) straight into
            // .header(...)/.retrieve() on RestClient.get()'s wildcard-typed return confuses IDE
            // type inference (resolves the next call against raw Object) even though it compiles
            // fine with javac. Capturing the wildcard into an explicitly-typed local first avoids it.
            RestClient.RequestHeadersSpec<?> request = restClient.get().uri(uri);
            request.headers(httpHeaders -> {
                httpHeaders.set("X-RapidAPI-Key", rapidApiKey);
                httpHeaders.set("X-RapidAPI-Host", HOST);
            });

            JSearchResponse response = request.retrieve().body(JSearchResponse.class);

            if (response == null || response.getData() == null) {
                return List.of();
            }

            return response.getData().stream().map(this::toListing).toList();
        } catch (RestClientException e) {
            log.warn("JSearch search failed (call swallowed, source omitted from results): {}", e.getMessage());
            return List.of();
        }
    }

    private ExternalJobListing toListing(JSearchResponse.Job job) {
        ExternalJobListing listing = new ExternalJobListing();
        listing.setSource(ExternalJobSource.JSEARCH);
        listing.setExternalId(job.getJobId());
        listing.setTitle(job.getJobTitle());
        listing.setCompanyName(job.getEmployerName());
        listing.setDescription(job.getJobDescription());
        listing.setLocation(joinLocation(job.getJobCity(), job.getJobCountry()));
        listing.setUrl(job.getJobApplyLink());
        listing.setSalaryRange(formatSalaryRange(job.getJobMinSalary(), job.getJobMaxSalary()));
        listing.setEmploymentType(mapEmploymentType(job.getJobEmploymentType()));
        listing.setWorkMode(Boolean.TRUE.equals(job.getJobIsRemote()) ? WorkMode.REMOTE : null);
        listing.setPostedAt(job.getJobPostedAtDatetimeUtc());
        return listing;
    }

    private String joinLocation(String city, String country) {
        if (city == null || city.isBlank()) {
            return country;
        }
        if (country == null || country.isBlank()) {
            return city;
        }
        return city + ", " + country;
    }

    private String formatSalaryRange(Double min, Double max) {
        if (min == null && max == null) {
            return null;
        }
        if (min != null && max != null && !min.equals(max)) {
            return formatAmount(min) + " - " + formatAmount(max);
        }
        return formatAmount(min != null ? min : max);
    }

    private String formatAmount(Double amount) {
        return String.format(Locale.ROOT, "%,.0f", amount);
    }

    /**
     * JSearch's job_employment_type is documented as FULLTIME/PARTTIME/CONTRACTOR/INTERN.
     */
    private EmploymentType mapEmploymentType(String jobEmploymentType) {
        if (jobEmploymentType == null) {
            return null;
        }
        return switch (jobEmploymentType.toUpperCase(Locale.ROOT)) {
            case "FULLTIME" -> EmploymentType.FULL_TIME;
            case "PARTTIME" -> EmploymentType.PART_TIME;
            case "CONTRACTOR" -> EmploymentType.CONTRACT;
            case "INTERN" -> EmploymentType.INTERNSHIP;
            default -> null;
        };
    }

    private String nonBlank(String value) {
        return value == null ? "" : value;
    }
}
