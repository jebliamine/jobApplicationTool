package de.jeb.japp.file.storage.services;

import de.jeb.japp.model.storage.StoredFile;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;


public interface FileStorageServiceInterface {
    StoredFile save(MultipartFile file, UUID userId) throws IOException;

    Resource load(String key);

    void delete(String key) throws IOException;
}
