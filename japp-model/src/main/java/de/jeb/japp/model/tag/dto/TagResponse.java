package de.jeb.japp.model.tag.dto;

import de.jeb.japp.model.tag.Tag;

import java.util.UUID;

public class TagResponse {
    private UUID id;
    private String name;

    public TagResponse() {
    }

    public static TagResponse from(Tag tag) {
        TagResponse response = new TagResponse();
        response.id = tag.getId();
        response.name = tag.getName();
        return response;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
