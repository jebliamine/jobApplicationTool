package de.jeb.japp.model.jobsearch.dto;

import de.jeb.japp.model.jobsearch.ExternalJobSource;

/**
 * Per-source outcome of one aggregated search — lets the frontend show e.g. "Jooble: not
 * configured" or "JSearch: temporarily unavailable" next to the results, instead of silently
 * dropping a source that returned nothing.
 */
public class JobSearchSourceSummary {
    private ExternalJobSource source;
    private boolean configured;
    private boolean succeeded;
    private int resultCount;

    public JobSearchSourceSummary() {
    }

    public JobSearchSourceSummary(ExternalJobSource source, boolean configured, boolean succeeded, int resultCount) {
        this.source = source;
        this.configured = configured;
        this.succeeded = succeeded;
        this.resultCount = resultCount;
    }

    public ExternalJobSource getSource() {
        return source;
    }

    public void setSource(ExternalJobSource source) {
        this.source = source;
    }

    public boolean isConfigured() {
        return configured;
    }

    public void setConfigured(boolean configured) {
        this.configured = configured;
    }

    public boolean isSucceeded() {
        return succeeded;
    }

    public void setSucceeded(boolean succeeded) {
        this.succeeded = succeeded;
    }

    public int getResultCount() {
        return resultCount;
    }

    public void setResultCount(int resultCount) {
        this.resultCount = resultCount;
    }
}
