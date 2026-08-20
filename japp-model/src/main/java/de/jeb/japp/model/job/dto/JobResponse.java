package de.jeb.japp.model.job.dto;

import de.jeb.japp.model.company.dto.CompanyResponse;
import de.jeb.japp.model.job.EmploymentType;
import de.jeb.japp.model.job.Job;
import de.jeb.japp.model.job.WorkMode;
import de.jeb.japp.model.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.UUID;

/** Safe Job response DTO — Job must never be serialized directly (its owner is a full User entity). */
public class JobResponse {
    private UUID id;
    private String title;
    private String description;
    private String location;
    private EmploymentType employmentType;
    private WorkMode workMode;
    private String url;
    private String source;
    private String salaryRange;
    private CompanyResponse company;
    private UserDto owner;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public JobResponse() {
    }

    public static JobResponse from(Job job) {
        JobResponse response = new JobResponse();
        response.id = job.getId();
        response.title = job.getTitle();
        response.description = job.getDescription();
        response.location = job.getLocation();
        response.employmentType = job.getEmploymentType();
        response.workMode = job.getWorkMode();
        response.url = job.getUrl();
        response.source = job.getSource();
        response.salaryRange = job.getSalaryRange();
        response.company = CompanyResponse.from(job.getCompany());
        response.owner = UserDto.from(job.getOwner());
        response.createdAt = job.getCreatedAt();
        response.updatedAt = job.getUpdatedAt();
        return response;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public EmploymentType getEmploymentType() {
        return employmentType;
    }

    public WorkMode getWorkMode() {
        return workMode;
    }

    public String getUrl() {
        return url;
    }

    public String getSource() {
        return source;
    }

    public String getSalaryRange() {
        return salaryRange;
    }

    public CompanyResponse getCompany() {
        return company;
    }

    public UserDto getOwner() {
        return owner;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
