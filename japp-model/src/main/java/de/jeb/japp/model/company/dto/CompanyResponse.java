package de.jeb.japp.model.company.dto;

import de.jeb.japp.model.company.Company;
import de.jeb.japp.model.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.UUID;

/** Safe Company response DTO — Company must never be serialized directly (its owner is a full User entity). */
public class CompanyResponse {
    private UUID id;
    private String name;
    private String website;
    private String location;
    private String notes;
    private UserDto owner;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CompanyResponse() {
    }

    public static CompanyResponse from(Company company) {
        CompanyResponse response = new CompanyResponse();
        response.id = company.getId();
        response.name = company.getName();
        response.website = company.getWebsite();
        response.location = company.getLocation();
        response.notes = company.getNotes();
        response.owner = UserDto.from(company.getOwner());
        response.createdAt = company.getCreatedAt();
        response.updatedAt = company.getUpdatedAt();
        return response;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getWebsite() {
        return website;
    }

    public String getLocation() {
        return location;
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
}
