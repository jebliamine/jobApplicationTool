package de.jeb.japp.model.application.dto;

import de.jeb.japp.model.application.Application;
import de.jeb.japp.model.application.ApplicationStatus;
import de.jeb.japp.model.application.InterviewStage;
import de.jeb.japp.model.coverLetter.dto.CoverLetterResponse;
import de.jeb.japp.model.cv.dto.CVResponse;
import de.jeb.japp.model.job.dto.JobResponse;
import de.jeb.japp.model.tag.dto.TagResponse;
import de.jeb.japp.model.user.dto.UserDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Safe Application response DTO — Application must never be serialized
 * directly (its user is a full User entity). Job, CVDocument, and CoverLetter
 * are nested through their own response DTOs (JobResponse already carries
 * company info; CoverLetterResponse is the existing DTO used everywhere else
 * a CoverLetter is returned — not a separate "summary" shape).
 */
public class ApplicationResponse {
    private UUID id;
    private JobResponse job;
    private CVResponse cv;
    private CoverLetterResponse coverLetter;
    private ApplicationStatus status;
    private LocalDate appliedAt;
    private LocalDate deadline;
    private LocalDate followUpDate;
    private String contactPerson;
    private String notes;
    private UserDto owner;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<TagResponse> tags;
    private List<InterviewStageResponse> interviewStages;

    public ApplicationResponse() {
    }

    public static ApplicationResponse from(Application application) {
        ApplicationResponse response = new ApplicationResponse();
        response.id = application.getId();
        response.job = JobResponse.from(application.getJob());
        response.cv = application.getCvDocument() != null ? CVResponse.from(application.getCvDocument()) : null;
        response.coverLetter = application.getCoverLetter() != null
                ? CoverLetterResponse.from(application.getCoverLetter())
                : null;
        response.status = application.getStatus();
        response.appliedAt = application.getAppliedAt();
        response.deadline = application.getDeadline();
        response.followUpDate = application.getFollowUpDate();
        response.contactPerson = application.getContactPerson();
        response.notes = application.getNotes();
        response.owner = UserDto.from(application.getUser());
        response.createdAt = application.getCreatedAt();
        response.updatedAt = application.getUpdatedAt();
        response.tags = application.getTags() == null
                ? List.of()
                : application.getTags().stream()
                        .map(TagResponse::from)
                        .sorted(Comparator.comparing(TagResponse::getName, String.CASE_INSENSITIVE_ORDER))
                        .toList();
        response.interviewStages = application.getInterviewStages() == null
                ? List.of()
                : application.getInterviewStages().stream()
                        .map(InterviewStageResponse::from)
                        .sorted(Comparator.comparing(InterviewStageResponse::getScheduledDate,
                                Comparator.nullsLast(Comparator.naturalOrder())))
                        .toList();
        return response;
    }

    public UUID getId() {
        return id;
    }

    public JobResponse getJob() {
        return job;
    }

    public CVResponse getCv() {
        return cv;
    }

    public CoverLetterResponse getCoverLetter() {
        return coverLetter;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public LocalDate getAppliedAt() {
        return appliedAt;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public LocalDate getFollowUpDate() {
        return followUpDate;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public String getNotes() {
        return notes;
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

    public List<TagResponse> getTags() {
        return tags;
    }

    public List<InterviewStageResponse> getInterviewStages() {
        return interviewStages;
    }
}
