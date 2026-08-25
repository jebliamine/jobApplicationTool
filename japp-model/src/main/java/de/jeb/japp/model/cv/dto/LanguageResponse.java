package de.jeb.japp.model.cv.dto;

import de.jeb.japp.model.cv.Language;

import java.util.UUID;

public class LanguageResponse {
    private UUID id;
    private String name;
    private String level;

    public LanguageResponse() {
    }

    public static LanguageResponse from(Language language) {
        LanguageResponse response = new LanguageResponse();
        response.id = language.getId();
        response.name = language.getName();
        response.level = language.getLevel();
        return response;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLevel() {
        return level;
    }
}
