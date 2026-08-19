package de.jeb.japp.model.generation.dto;

import de.jeb.japp.model.coverLetter.dto.CoverLetterResponse;
import de.jeb.japp.model.cv.dto.CVResponse;
import de.jeb.japp.model.generation.GenerationRequest;
import de.jeb.japp.model.generation.GenerationStatus;
import de.jeb.japp.model.job.dto.JobResponse;
import de.jeb.japp.model.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Safe GenerationRequest response DTO — GenerationRequest must never be
 * serialized directly (its user is a full User entity). Carries the
 * resulting CoverLetter (null until COMPLETED) so the client can poll a
 * single endpoint through the whole PENDING → IN_PROGRESS → COMPLETED/FAILED
 * lifecycle.
 */
public class GenerationRequestResponse {
    private UUID id;
    private JobResponse job;
    private CVResponse cv;
    private GenerationStatus status;
    private String provider;
    private String model;
    private String errorMessage;
    private CoverLetterResponse coverLetter;
    private UserDto owner;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    public GenerationRequestResponse() {
    }

    public static GenerationRequestResponse from(GenerationRequest request, CoverLetterResponse coverLetter) {
        GenerationRequestResponse response = new GenerationRequestResponse();
        response.id = request.getId();
        response.job = JobResponse.from(request.getJob());
        response.cv = request.getCvDocument() != null ? CVResponse.from(request.getCvDocument()) : null;
        response.status = request.getStatus();
        response.provider = request.getProvider();
        response.model = request.getModel();
        response.errorMessage = request.getErrorMessage();
        response.coverLetter = coverLetter;
        response.owner = UserDto.from(request.getUser());
        response.createdAt = request.getCreatedAt();
        response.startedAt = request.getStartedAt();
        response.completedAt = request.getCompletedAt();
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

    public GenerationStatus getStatus() {
        return status;
    }

    public String getProvider() {
        return provider;
    }

    public String getModel() {
        return model;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public CoverLetterResponse getCoverLetter() {
        return coverLetter;
    }

    public UserDto getOwner() {
        return owner;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
}
