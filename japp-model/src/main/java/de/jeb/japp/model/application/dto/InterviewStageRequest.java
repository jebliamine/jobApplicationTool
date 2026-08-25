package de.jeb.japp.model.application.dto;

import java.time.LocalDate;

/** Request body for POST/PUT of an Application's interview-stage sub-resource. */
public class InterviewStageRequest {
    private String title;
    private LocalDate scheduledDate;
    private String notes;
    private boolean completed;

    public InterviewStageRequest() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(LocalDate scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
