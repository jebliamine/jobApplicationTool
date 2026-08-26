package de.jeb.japp.model.jobsearch.dto;

import de.jeb.japp.model.job.EmploymentType;
import de.jeb.japp.model.job.WorkMode;
import de.jeb.japp.model.jobsearch.ExternalJobSource;

/**
 * One listing returned by an external job-search API (Adzuna/Jooble/JSearch), normalized to a
 * common shape. Never persisted directly — mirrors {@code JobExtractionResponse} field-for-field
 * (title/companyName/description/location/employmentType/workMode/salaryRange/url) so the
 * frontend can reuse the exact same "review and save into a tracked job" form as paste-to-import,
 * plus a couple of fields specific to a live external result (source, externalId, postedAt).
 */
public class ExternalJobListing {
    private ExternalJobSource source;
    private String externalId;
    private String title;
    private String companyName;
    private String description;
    private String location;
    private EmploymentType employmentType;
    private WorkMode workMode;
    private String salaryRange;
    private String url;
    private String postedAt;

    public ExternalJobListing() {
    }

    public ExternalJobSource getSource() {
        return source;
    }

    public void setSource(ExternalJobSource source) {
        this.source = source;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public EmploymentType getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(EmploymentType employmentType) {
        this.employmentType = employmentType;
    }

    public WorkMode getWorkMode() {
        return workMode;
    }

    public void setWorkMode(WorkMode workMode) {
        this.workMode = workMode;
    }

    public String getSalaryRange() {
        return salaryRange;
    }

    public void setSalaryRange(String salaryRange) {
        this.salaryRange = salaryRange;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPostedAt() {
        return postedAt;
    }

    public void setPostedAt(String postedAt) {
        this.postedAt = postedAt;
    }
}
