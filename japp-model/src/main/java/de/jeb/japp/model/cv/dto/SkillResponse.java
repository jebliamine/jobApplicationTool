package de.jeb.japp.model.cv.dto;

import de.jeb.japp.model.cv.Skill;

import java.util.UUID;

public class SkillResponse {
    private UUID id;
    private String name;

    public SkillResponse() {
    }

    public static SkillResponse from(Skill skill) {
        SkillResponse response = new SkillResponse();
        response.id = skill.getId();
        response.name = skill.getName();
        return response;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
