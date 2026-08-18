package de.jeb.japp.file.storage.services;

import de.jeb.japp.model.storage.StoredFile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class LocalFileStorageService implements FileStorageServiceInterface {

    private final Path root = Paths.get("storage/cv");

    @Override
    public StoredFile save(MultipartFile file, UUID userId) throws IOException {

        Files.createDirectories(root);

        String ext = getExtension(file.getOriginalFilename());

        String key =
                userId + "/"
                        + UUID.randomUUID()
                        + "." + ext;

        Path target = root.resolve(key);

        Files.createDirectories(target.getParent());

        Files.copy(file.getInputStream(), target);

        return new StoredFile(
                key,
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize()
        );
    }

    @Override
    public Resource load(String storageKey) {
        Path file = root.resolve(storageKey);
        return new FileSystemResource(file);
    }

    @Override
    public void delete(String storageKey) throws IOException {
        Files.deleteIfExists(root.resolve(storageKey));
    }

    /**
     * Never trust the raw extension for filesystem paths — it comes from the
     * user-supplied original filename and is concatenated straight into the
     * storage key below, so an unsanitized value (e.g. containing "../")
     * could escape {@code root}. Only a short alphanumeric extension is
     * accepted; anything else falls back to "bin".
     */
    private String getExtension(String name) {
        if (name == null || !name.contains(".")) return "bin";
        String ext = name.substring(name.lastIndexOf(".") + 1);
        return ext.matches("[a-zA-Z0-9]{1,10}") ? ext.toLowerCase() : "bin";
    }
}
