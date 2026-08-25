package de.jeb.japp.model.tag.dto;

/** Request body for POST /api/v1/tags and PUT /api/v1/tags/{id}. */
public class TagRequest {
    private String name;

    public TagRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
