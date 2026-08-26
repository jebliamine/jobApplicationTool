package de.jeb.japp.model.jobsearch.dto;

import java.util.List;

/** Response body for GET /api/v1/job-search — combined, de-duplicated results plus a per-source status summary. */
public class ExternalJobSearchResponse {
    private List<ExternalJobListing> results;
    private List<JobSearchSourceSummary> sources;

    public ExternalJobSearchResponse() {
    }

    public ExternalJobSearchResponse(List<ExternalJobListing> results, List<JobSearchSourceSummary> sources) {
        this.results = results;
        this.sources = sources;
    }

    public List<ExternalJobListing> getResults() {
        return results;
    }

    public void setResults(List<ExternalJobListing> results) {
        this.results = results;
    }

    public List<JobSearchSourceSummary> getSources() {
        return sources;
    }

    public void setSources(List<JobSearchSourceSummary> sources) {
        this.sources = sources;
    }
}
