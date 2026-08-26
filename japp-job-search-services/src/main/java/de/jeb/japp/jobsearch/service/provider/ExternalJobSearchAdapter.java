package de.jeb.japp.jobsearch.service.provider;

import de.jeb.japp.model.jobsearch.ExternalJobSource;
import de.jeb.japp.model.jobsearch.dto.ExternalJobListing;

import java.util.List;

/**
 * One external job-search API integration (Adzuna/Jooble/JSearch). Unlike the AI-generation
 * adapters in {@code japp-generation-services}, an implementation never throws for a call
 * failure —  JobSearchAggregatorService combines whichever sources succeed, so one
 * misbehaving or unconfigured source degrades gracefully instead of failing the whole search.
 */
public interface ExternalJobSearchAdapter {

    ExternalJobSource source();

    /**
     * True once this source's required credentials are present — checked before every call.
     */
    boolean isConfigured();

    /**
     * Never throws. Returns an empty list if not configured, and also if the call itself fails
     * (network/auth/rate-limit/malformed response) — implementations log the failure themselves.
     */
    List<ExternalJobListing> search(String keyword, String location, int page);
}
