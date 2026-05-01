package de.jeb.japp.model.storage;

public class StoredFile {
    private final String storageKey;
    private final String originalFilename;
    private final String contentType;
    private final Long size;

    public StoredFile(String storageKey,
                      String originalFilename,
                      String contentType,
                      Long size) {
        this.storageKey = storageKey;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.size = size;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getContentType() {
        return contentType;
    }

    public Long getSize() {
        return size;
    }
}
