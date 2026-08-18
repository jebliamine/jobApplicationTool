package de.jeb.japp.model.cv.dto;

import de.jeb.japp.model.cv.CVDocument;
import de.jeb.japp.model.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Safe CV response DTO. CVDocument must never be serialized directly — its
 * `owner` field is the full User entity (passwordHash included), which
 * would otherwise leak in every response.
 */
public class CVResponse {
    private UUID id;
    private String title;
    private String fileName;
    private String contentType;
    private Long size;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserDto owner;

    public CVResponse() {
    }

    public static CVResponse from(CVDocument document) {
        CVResponse response = new CVResponse();
        response.id = document.getId();
        response.title = document.getTitle();
        response.fileName = document.getFileName();
        response.contentType = document.getContentType();
        response.size = document.getSize();
        response.createdAt = document.getCreatedAt();
        response.updatedAt = document.getUpdatedAt();
        response.owner = UserDto.from(document.getOwner());
        return response;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public Long getSize() {
        return size;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public UserDto getOwner() {
        return owner;
    }
}
