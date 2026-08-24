package de.jeb.japp.model.cv.dto;

import de.jeb.japp.model.cv.CVProfile;
import de.jeb.japp.model.cv.ProfileGenerationStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class CVProfileResponse {
    private UUID id;
    private String fullName;
    private String summary;
    private List<ExperienceResponse> experiences;
    private ProfileGenerationStatus status;
    private String errorMessage;
    private LocalDateTime generatedAt;

    public CVProfileResponse() {
    }

    /** No generation has ever been attempted for this CV yet. */
    public static CVProfileResponse notAttempted() {
        CVProfileResponse response = new CVProfileResponse();
        response.experiences = List.of();
        response.status = ProfileGenerationStatus.NOT_ATTEMPTED;
        return response;
    }

    public static CVProfileResponse from(CVProfile profile) {
        CVProfileResponse response = new CVProfileResponse();
        response.id = profile.getId();
        response.fullName = profile.getFullName();
        response.summary = profile.getSummary();
        response.experiences = profile.getExperiences() == null
                ? List.of()
                : profile.getExperiences().stream().map(ExperienceResponse::from).toList();
        response.status = profile.getStatus();
        response.errorMessage = profile.getErrorMessage();
        response.generatedAt = profile.getGeneratedAt();
        return response;
    }

    public UUID getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getSummary() {
        return summary;
    }

    public List<ExperienceResponse> getExperiences() {
        return experiences;
    }

    public ProfileGenerationStatus getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }
}
