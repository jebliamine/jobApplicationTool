package de.jeb.japp.model.cv.dto;

import de.jeb.japp.model.cv.Experience;

import java.time.LocalDate;
import java.util.UUID;

public class ExperienceResponse {
    private UUID id;
    private String company;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;

    public ExperienceResponse() {
    }

    public static ExperienceResponse from(Experience experience) {
        ExperienceResponse response = new ExperienceResponse();
        response.id = experience.getId();
        response.company = experience.getCompany();
        response.title = experience.getTitle();
        response.startDate = experience.getStartDate();
        response.endDate = experience.getEndDate();
        response.description = experience.getDescription();
        return response;
    }

    public UUID getId() {
        return id;
    }

    public String getCompany() {
        return company;
    }

    public String getTitle() {
        return title;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public String getDescription() {
        return description;
    }
}
