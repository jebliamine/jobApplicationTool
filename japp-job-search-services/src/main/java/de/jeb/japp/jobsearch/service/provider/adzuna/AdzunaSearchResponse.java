package de.jeb.japp.jobsearch.service.provider.adzuna;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** Shape of Adzuna's {@code GET /v1/api/jobs/{country}/search/{page}} response — only the fields this adapter reads. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdzunaSearchResponse {
    private List<Result> results;

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        private String id;
        private String title;
        private String description;
        @JsonProperty("redirect_url")
        private String redirectUrl;
        private String created;
        @JsonProperty("salary_min")
        private Double salaryMin;
        @JsonProperty("salary_max")
        private Double salaryMax;
        @JsonProperty("contract_type")
        private String contractType;
        @JsonProperty("contract_time")
        private String contractTime;
        private Company company;
        private Location location;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getRedirectUrl() {
            return redirectUrl;
        }

        public void setRedirectUrl(String redirectUrl) {
            this.redirectUrl = redirectUrl;
        }

        public String getCreated() {
            return created;
        }

        public void setCreated(String created) {
            this.created = created;
        }

        public Double getSalaryMin() {
            return salaryMin;
        }

        public void setSalaryMin(Double salaryMin) {
            this.salaryMin = salaryMin;
        }

        public Double getSalaryMax() {
            return salaryMax;
        }

        public void setSalaryMax(Double salaryMax) {
            this.salaryMax = salaryMax;
        }

        public String getContractType() {
            return contractType;
        }

        public void setContractType(String contractType) {
            this.contractType = contractType;
        }

        public String getContractTime() {
            return contractTime;
        }

        public void setContractTime(String contractTime) {
            this.contractTime = contractTime;
        }

        public Company getCompany() {
            return company;
        }

        public void setCompany(Company company) {
            this.company = company;
        }

        public Location getLocation() {
            return location;
        }

        public void setLocation(Location location) {
            this.location = location;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Company {
        @JsonProperty("display_name")
        private String displayName;

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Location {
        @JsonProperty("display_name")
        private String displayName;

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }
    }
}
