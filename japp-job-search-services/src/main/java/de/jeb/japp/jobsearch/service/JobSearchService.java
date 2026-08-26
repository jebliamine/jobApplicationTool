package de.jeb.japp.jobsearch.service;

import de.jeb.japp.jobsearch.service.provider.ExternalJobSearchAdapter;
import de.jeb.japp.model.jobsearch.dto.ExternalJobListing;
import de.jeb.japp.model.jobsearch.dto.ExternalJobSearchResponse;
import de.jeb.japp.model.jobsearch.dto.JobSearchSourceSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Fans a search out to every registered {@link ExternalJobSearchAdapter} (Adzuna/Jooble/JSearch)
 * in parallel, combines whatever comes back, and de-duplicates near-identical listings across
 * sources by (title, company, location). Adapters never throw (see the interface contract), so a
 * disabled or failing source simply contributes zero results and shows up in
 * {@link JobSearchSourceSummary} rather than failing the whole search.
 */
@Service
public class JobSearchService {

    private static final Logger log = LoggerFactory.getLogger(JobSearchService.class);

    private final List<ExternalJobSearchAdapter> adapters;

    public JobSearchService(List<ExternalJobSearchAdapter> adapters) {
        this.adapters = adapters;
    }

    public ExternalJobSearchResponse search(String keyword, String location, int page) {
        List<CompletableFuture<AdapterOutcome>> futures = adapters.stream()
                .map(adapter -> CompletableFuture.supplyAsync(() -> runAdapter(adapter, keyword, location, page)))
                .toList();

        List<AdapterOutcome> outcomes = futures.stream().map(CompletableFuture::join).toList();

        List<ExternalJobListing> merged = dedupe(outcomes.stream().flatMap(o -> o.listings.stream()).toList());
        List<JobSearchSourceSummary> summaries = outcomes.stream()
                .map(o -> new JobSearchSourceSummary(o.adapter.source(), o.adapter.isConfigured(), o.succeeded, o.listings.size()))
                .toList();

        return new ExternalJobSearchResponse(merged, summaries);
    }

    /** Belt-and-suspenders: adapters already never throw, but a search must never fail outright because one source misbehaves. */
    private AdapterOutcome runAdapter(ExternalJobSearchAdapter adapter, String keyword, String location, int page) {
        try {
            return new AdapterOutcome(adapter, adapter.search(keyword, location, page), true);
        } catch (RuntimeException e) {
            log.warn("Job search adapter {} threw unexpectedly (source omitted from results): {}", adapter.source(), e.getMessage());
            return new AdapterOutcome(adapter, List.of(), false);
        }
    }

    private List<ExternalJobListing> dedupe(List<ExternalJobListing> listings) {
        Map<String, ExternalJobListing> byKey = new LinkedHashMap<>();
        for (ExternalJobListing listing : listings) {
            byKey.putIfAbsent(dedupeKey(listing), listing);
        }
        return List.copyOf(byKey.values());
    }

    private String dedupeKey(ExternalJobListing listing) {
        return normalize(listing.getTitle()) + "|" + normalize(listing.getCompanyName()) + "|" + normalize(listing.getLocation());
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private record AdapterOutcome(ExternalJobSearchAdapter adapter, List<ExternalJobListing> listings, boolean succeeded) {
    }
}
