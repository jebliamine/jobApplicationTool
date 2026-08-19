package de.jeb.japp.model.dashboard.dto;

import de.jeb.japp.model.generation.GenerationStatus;

import java.util.Map;

/**
 * Single role-aware dashboard response — matches how every other list
 * endpoint in the app already behaves (ADMIN sees global counts, USER sees
 * only their own; see JobService/ApplicationService/CoverLetterService/
 * GenerationRequestService#list). totalUsers is populated only for ADMIN
 * requesters, since Users have no owner to scope a personal count by.
 */
public class DashboardResponse {
    private long cvCount;
    private long jobCount;
    private long applicationCount;
    private long activeCoverLetterCount;
    private long archivedCoverLetterCount;
    private long generationRequestCount;
    private Map<GenerationStatus, Long> generationStatusCounts;
    private Long totalUsers;

    public DashboardResponse() {
    }

    public long getCvCount() {
        return cvCount;
    }

    public void setCvCount(long cvCount) {
        this.cvCount = cvCount;
    }

    public long getJobCount() {
        return jobCount;
    }

    public void setJobCount(long jobCount) {
        this.jobCount = jobCount;
    }

    public long getApplicationCount() {
        return applicationCount;
    }

    public void setApplicationCount(long applicationCount) {
        this.applicationCount = applicationCount;
    }

    public long getActiveCoverLetterCount() {
        return activeCoverLetterCount;
    }

    public void setActiveCoverLetterCount(long activeCoverLetterCount) {
        this.activeCoverLetterCount = activeCoverLetterCount;
    }

    public long getArchivedCoverLetterCount() {
        return archivedCoverLetterCount;
    }

    public void setArchivedCoverLetterCount(long archivedCoverLetterCount) {
        this.archivedCoverLetterCount = archivedCoverLetterCount;
    }

    public long getGenerationRequestCount() {
        return generationRequestCount;
    }

    public void setGenerationRequestCount(long generationRequestCount) {
        this.generationRequestCount = generationRequestCount;
    }

    public Map<GenerationStatus, Long> getGenerationStatusCounts() {
        return generationStatusCounts;
    }

    public void setGenerationStatusCounts(Map<GenerationStatus, Long> generationStatusCounts) {
        this.generationStatusCounts = generationStatusCounts;
    }

    public Long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(Long totalUsers) {
        this.totalUsers = totalUsers;
    }
}
