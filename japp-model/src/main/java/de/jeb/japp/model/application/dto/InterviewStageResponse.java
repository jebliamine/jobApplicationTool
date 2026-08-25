package de.jeb.japp.model.application.dto;

import de.jeb.japp.model.application.InterviewStage;

import java.time.LocalDate;
import java.util.UUID;

public class InterviewStageResponse {
    private UUID id;
    private String title;
    private LocalDate scheduledDate;
    private String notes;
    private boolean completed;

    public InterviewStageResponse() {
    }

    public static InterviewStageResponse from(InterviewStage stage) {
        InterviewStageResponse response = new InterviewStageResponse();
        response.id = stage.getId();
        response.title = stage.getTitle();
        response.scheduledDate = stage.getScheduledDate();
        response.notes = stage.getNotes();
        response.completed = stage.isCompleted();
        return response;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public LocalDate getScheduledDate() {
        return scheduledDate;
    }

    public String getNotes() {
        return notes;
    }

    public boolean isCompleted() {
        return completed;
    }
}
