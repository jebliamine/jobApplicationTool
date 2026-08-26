package de.jeb.japp.model.job.dto;

import de.jeb.japp.model.job.EmploymentType;
import de.jeb.japp.model.job.WorkMode;

/**
 * Response body for POST /api/v1/jobs/extract — suggested field values to pre-fill the job
 * creation form. Never persisted directly: the user reviews/edits these before submitting the
 * normal POST /api/v1/jobs request. companyName is a plain string (not a companyId) since the
 * extracted company may or may not already exist as one of the user's Company records.
 */
public class JobExtractionResponse {
    private String title;
    private String companyName;
    private String description;
    private String location;
    private EmploymentType employmentType;
    private WorkMode workMode;
    private String salaryRange;
    private String url;

    public JobExtractionResponse() {
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
}
