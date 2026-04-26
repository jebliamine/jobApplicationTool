package de.jeb.japp.model.cv;

import java.util.UUID;


public class CVDocument {
    private UUID id;
    private String url;
    private FileExtension type;


    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public FileExtension getType() {
        return type;
    }

    public void setType(FileExtension type) {
        this.type = type;
    }
}
