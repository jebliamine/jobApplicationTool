package de.jeb.japp.model.cv;

public enum FileExtension {
    PDF("pdf"),
    DOCX("docx");

    private final String value;

    FileExtension(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}