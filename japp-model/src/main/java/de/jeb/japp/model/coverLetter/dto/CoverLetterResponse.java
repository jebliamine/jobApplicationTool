package de.jeb.japp.model.coverLetter.dto;

import de.jeb.japp.model.coverLetter.CoverLetter;
import de.jeb.japp.model.cv.dto.CVResponse;
import de.jeb.japp.model.job.dto.JobResponse;
import de.jeb.japp.model.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Safe CoverLetter response DTO — CoverLetter must never be serialized
 * directly (its owner is a full User entity). Job/CV are read through the
 * generation request for provenance, matching how Application nests Job/CV.
 */
public class CoverLetterResponse {
    private UUID id;
    private String resultText;
    private UUID generationRequestId;
    private JobResponse job;
    private CVResponse cv;
    private UserDto owner;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CoverLetterResponse() {
    }

    public static CoverLetterResponse from(CoverLetter coverLetter) {
        CoverLetterResponse response = new CoverLetterResponse();
        response.id = coverLetter.getId();
        response.resultText = coverLetter.getResultText();
        response.generationRequestId = coverLetter.getGenerationRequest().getId();
        response.job = JobResponse.from(coverLetter.getGenerationRequest().getJob());
        response.cv = coverLetter.getGenerationRequest().getCvDocument() != null
                ? CVResponse.from(coverLetter.getGenerationRequest().getCvDocument())
                : null;
        response.owner = UserDto.from(coverLetter.getOwner());
        response.createdAt = coverLetter.getCreatedAt();
        response.updatedAt = coverLetter.getUpdatedAt();
        return response;
    }

    public UUID getId() {
        return id;
    }

    public String getResultText() {
        return resultText;
    }

    public UUID getGenerationRequestId() {
        return generationRequestId;
    }

    public JobResponse getJob() {
        return job;
    }

    public CVResponse getCv() {
        return cv;
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
