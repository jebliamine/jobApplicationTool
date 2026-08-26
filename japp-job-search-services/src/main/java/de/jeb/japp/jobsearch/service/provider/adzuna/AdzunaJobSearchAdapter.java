package de.jeb.japp.jobsearch.service.provider.adzuna;

import de.jeb.japp.jobsearch.service.provider.ExternalJobSearchAdapter;
import de.jeb.japp.model.job.EmploymentType;
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
import java.util.Optional;

/**
 * Calls Adzuna's {@code GET /v1/api/jobs/{country}/search/{page}} REST API
 * (<a href="https://developer.adzuna.com/docs/search">docs</a>). Disabled (returns no results,
 * never called) unless both {@code ADZUNA_APP_ID} and {@code ADZUNA_APP_KEY} are set — see
 * {@link #isConfigured()}. Never throws: every failure is logged and swallowed so one bad source
 * doesn't fail the whole aggregated search.
 */
public class AdzunaJobSearchAdapter implements ExternalJobSearchAdapter {

    private static final Logger log = LoggerFactory.getLogger(AdzunaJobSearchAdapter.class);
    private static final String BASE_URL = "https://api.adzuna.com/v1/api/jobs";
    private static final int RESULTS_PER_PAGE = 20;

    private final RestClient restClient;
    private final String appId;
    private final String appKey;
    private final String country;

    public AdzunaJobSearchAdapter(
            RestClient restClient,
            @Value("${job-search.adzuna.app-id:}") String appId,
            @Value("${job-search.adzuna.app-key:}") String appKey,
            @Value("${job-search.adzuna.country:de}") String country
    ) {
        this.restClient = restClient;
        this.appId = appId;
        this.appKey = appKey;
        this.country = country;
    }

    @Override
    public ExternalJobSource source() {
        return ExternalJobSource.ADZUNA;
    }

    @Override
    public boolean isConfigured() {
        return !appId.isBlank() && !appKey.isBlank();
    }

    @Override
    public List<ExternalJobListing> search(String keyword, String location, int page) {
        if (!isConfigured()) {
            return List.of();
        }

        try {
            String uri = UriComponentsBuilder
                    .fromUriString(BASE_URL + "/{country}/search/{page}")
                    .queryParam("app_id", appId)
                    .queryParam("app_key", appKey)
                    .queryParam("results_per_page", RESULTS_PER_PAGE)
                    .queryParamIfPresent("what", Optional.ofNullable(blankToNull(keyword)))
                    .queryParamIfPresent("where", Optional.ofNullable(blankToNull(location)))
                    .buildAndExpand(country, Math.max(page, 1))
                    .toUriString();

            AdzunaSearchResponse response = restClient.get().uri(uri).retrieve().body(AdzunaSearchResponse.class);
            if (response == null || response.getResults() == null) {
                return List.of();
            }

            return response.getResults().stream().map(this::toListing).toList();
        } catch (RestClientException e) {
            log.warn("Adzuna search failed (call swallowed, source omitted from results): {}", e.getMessage());
            return List.of();
        }
    }

    private ExternalJobListing toListing(AdzunaSearchResponse.Result result) {
        ExternalJobListing listing = new ExternalJobListing();
        listing.setSource(ExternalJobSource.ADZUNA);
        listing.setExternalId(result.getId());
        listing.setTitle(result.getTitle());
        listing.setCompanyName(result.getCompany() != null ? result.getCompany().getDisplayName() : null);
        listing.setDescription(result.getDescription());
        listing.setLocation(result.getLocation() != null ? result.getLocation().getDisplayName() : null);
        listing.setUrl(result.getRedirectUrl());
        listing.setSalaryRange(formatSalaryRange(result.getSalaryMin(), result.getSalaryMax()));
        listing.setEmploymentType(mapEmploymentType(result.getContractType(), result.getContractTime()));
        listing.setPostedAt(result.getCreated());
        return listing;
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
     * Adzuna's contract_type/contract_time are free-text-ish enums observed as
     * permanent/contract/part_time and full_time/part_time — mapped on a best-effort basis;
     * anything unrecognized is left null rather than guessed.
     */
    private EmploymentType mapEmploymentType(String contractType, String contractTime) {
        if ("contract".equalsIgnoreCase(contractType)) {
            return EmploymentType.CONTRACT;
        }
        if ("part_time".equalsIgnoreCase(contractTime)) {
            return EmploymentType.PART_TIME;
        }
        if ("full_time".equalsIgnoreCase(contractTime)) {
            return EmploymentType.FULL_TIME;
        }
        return null;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
