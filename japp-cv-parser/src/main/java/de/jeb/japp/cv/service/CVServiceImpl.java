package de.jeb.japp.cv.service;

import de.jeb.japp.dao.cv.CVDao;
import de.jeb.japp.dao.user.UserDao;
import de.jeb.japp.file.storage.services.FileStorageServiceInterface;
import de.jeb.japp.model.cv.CVDocument;
import de.jeb.japp.model.storage.StoredFile;
import de.jeb.japp.model.user.User;
import de.jeb.japp.security.service.JwtService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CVServiceImpl implements CVServiceInterface {

    private final CVDao cvDao;
    private final UserDao userDao;
    private final JwtService jwtService;
    private final FileStorageServiceInterface storageService;

    public CVServiceImpl(CVDao cvDao, UserDao userDao, JwtService jwtService, FileStorageServiceInterface storageService) {
        this.cvDao = cvDao;
        this.userDao = userDao;
        this.jwtService = jwtService;
        this.storageService = storageService;
    }

    @Override
    public CVDocument uploadCv(MultipartFile file, String title, User owner) {
        try {
            String path = saveFile(file);

            CVDocument cv = new CVDocument();
            cv.setTitle(title);
            cv.setFileName(file.getOriginalFilename());
            cv.setFilePath(path);
            cv.setContentType(file.getContentType());
            cv.setSize(file.getSize());
            cv.setCreatedAt(LocalDateTime.now());

            cv.setOwner(owner);

            StoredFile stored =
                    storageService.save(file, owner.getId());

            return cvDao.saveCV(cv);

        } catch (Exception e) {
            throw new RuntimeException("Upload failed", e);
        }
    }

    @Override
    public CVDocument getCv(UUID id) {
        return null;
    }

    private String saveFile(MultipartFile file) throws IOException {
        String path = "uploads/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        Files.copy(file.getInputStream(), Paths.get(path));
        return path;
    }

    @Override
    public List<CVDocument> getAll() {
        return cvDao.getAllCVs();
    }

    @Override
    public List<CVDocument> getAllByOwner(User user) {
        return cvDao.getAllCVsByOwner(user);
    }
}
