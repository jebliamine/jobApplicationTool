package de.jeb.japp.model.jobsearch;

/** The external job-search APIs the job-search page can aggregate results from. */
public enum ExternalJobSource {
    ADZUNA("Adzuna"),
    JOOBLE("Jooble"),
    JSEARCH("JSearch");

    private final String displayName;

    ExternalJobSource(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
