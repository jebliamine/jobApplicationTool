package de.jeb.japp.jobsearch.service.provider.jsearch;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** Shape of JSearch's (RapidAPI) {@code GET /search} response — only the fields this adapter reads. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class JSearchResponse {
    private List<Job> data;

    public List<Job> getData() {
        return data;
    }

    public void setData(List<Job> data) {
        this.data = data;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Job {
        @JsonProperty("job_id")
        private String jobId;
        @JsonProperty("job_title")
        private String jobTitle;
        @JsonProperty("employer_name")
        private String employerName;
        @JsonProperty("job_description")
        private String jobDescription;
        @JsonProperty("job_city")
        private String jobCity;
        @JsonProperty("job_country")
        private String jobCountry;
        @JsonProperty("job_apply_link")
        private String jobApplyLink;
        @JsonProperty("job_employment_type")
        private String jobEmploymentType;
        @JsonProperty("job_is_remote")
        private Boolean jobIsRemote;
        @JsonProperty("job_min_salary")
        private Double jobMinSalary;
        @JsonProperty("job_max_salary")
        private Double jobMaxSalary;
        @JsonProperty("job_posted_at_datetime_utc")
        private String jobPostedAtDatetimeUtc;

        public String getJobId() {
            return jobId;
        }

        public void setJobId(String jobId) {
            this.jobId = jobId;
        }

        public String getJobTitle() {
            return jobTitle;
        }

        public void setJobTitle(String jobTitle) {
            this.jobTitle = jobTitle;
        }

        public String getEmployerName() {
            return employerName;
        }

        public void setEmployerName(String employerName) {
            this.employerName = employerName;
        }

        public String getJobDescription() {
            return jobDescription;
        }

        public void setJobDescription(String jobDescription) {
            this.jobDescription = jobDescription;
        }

        public String getJobCity() {
            return jobCity;
        }

        public void setJobCity(String jobCity) {
            this.jobCity = jobCity;
        }

        public String getJobCountry() {
            return jobCountry;
        }

        public void setJobCountry(String jobCountry) {
            this.jobCountry = jobCountry;
        }

        public String getJobApplyLink() {
            return jobApplyLink;
        }

        public void setJobApplyLink(String jobApplyLink) {
            this.jobApplyLink = jobApplyLink;
        }

        public String getJobEmploymentType() {
            return jobEmploymentType;
        }

        public void setJobEmploymentType(String jobEmploymentType) {
            this.jobEmploymentType = jobEmploymentType;
        }

        public Boolean getJobIsRemote() {
            return jobIsRemote;
        }

        public void setJobIsRemote(Boolean jobIsRemote) {
            this.jobIsRemote = jobIsRemote;
        }

        public Double getJobMinSalary() {
            return jobMinSalary;
        }

        public void setJobMinSalary(Double jobMinSalary) {
            this.jobMinSalary = jobMinSalary;
        }

        public Double getJobMaxSalary() {
            return jobMaxSalary;
        }

        public void setJobMaxSalary(Double jobMaxSalary) {
            this.jobMaxSalary = jobMaxSalary;
        }

        public String getJobPostedAtDatetimeUtc() {
            return jobPostedAtDatetimeUtc;
        }

        public void setJobPostedAtDatetimeUtc(String jobPostedAtDatetimeUtc) {
            this.jobPostedAtDatetimeUtc = jobPostedAtDatetimeUtc;
        }
    }
}
