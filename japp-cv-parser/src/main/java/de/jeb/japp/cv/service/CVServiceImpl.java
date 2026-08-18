package de.jeb.japp.cv.service;

import de.jeb.japp.dao.cv.CVDao;
import de.jeb.japp.dao.user.UserDao;
import de.jeb.japp.file.storage.services.FileStorageServiceInterface;
import de.jeb.japp.model.cv.CVDocument;
import de.jeb.japp.model.storage.StoredFile;
import de.jeb.japp.model.user.User;
import de.jeb.japp.model.user.UserRole;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class CVServiceImpl implements CVServiceInterface {

    private static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "doc", "docx");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    private final CVDao cvDao;
    private final UserDao userDao;
    private final FileStorageServiceInterface storageService;

    public CVServiceImpl(CVDao cvDao, UserDao userDao, FileStorageServiceInterface storageService) {
        this.cvDao = cvDao;
        this.userDao = userDao;
        this.storageService = storageService;
    }

    @Override
    public CVDocument uploadCv(MultipartFile file, String title, User owner) {
        validateUpload(file, title);

        try {
            StoredFile stored = storageService.save(file, owner.getId());

            CVDocument doc = new CVDocument();
            doc.setTitle(title.trim());
            doc.setFileName(stored.getOriginalFilename());
            doc.setStorageKey(stored.getStorageKey());
            doc.setContentType(stored.getContentType());
            doc.setSize(stored.getSize());
            LocalDateTime now = LocalDateTime.now();
            doc.setCreatedAt(now);
            doc.setUpdatedAt(now);
            doc.setOwner(owner);

            return cvDao.saveCV(doc);
        } catch (CvValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Upload failed", e);
        }
    }

    @Override
    public CVDocument getCv(UUID id, User requester) {
        CVDocument doc = cvDao.getCVById(id).orElseThrow(() -> new CvNotFoundException("CV not found."));
        assertAccess(doc, requester);
        return doc;
    }

    @Override
    public List<CVDocument> getAll() {
        return cvDao.getAllCVs();
    }

    @Override
    public List<CVDocument> getAllByOwner(User user) {
        return cvDao.getAllCVsByOwner(user);
    }

    @Override
    public Resource loadResource(CVDocument document) {
        Resource resource = storageService.load(document.getStorageKey());
        if (!resource.exists() || !resource.isReadable()) {
            throw new CvNotFoundException("CV file not found.");
        }
        return resource;
    }

    @Override
    public void deleteCv(UUID id, User requester) {
        CVDocument doc = getCv(id, requester);

        try {
            storageService.delete(doc.getStorageKey());
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete CV file", e);
        }

        cvDao.deleteCV(doc.getId());
    }

    private void assertAccess(CVDocument doc, User requester) {
        boolean isOwner = doc.getOwner() != null && doc.getOwner().getId().equals(requester.getId());
        boolean isAdmin = requester.getRole() == UserRole.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new CvAccessDeniedException("You do not have access to this CV.");
        }
    }

    private void validateUpload(MultipartFile file, String title) {
        if (title == null || title.isBlank()) {
            throw new CvValidationException("A title is required.");
        }
        if (file == null || file.isEmpty()) {
            throw new CvValidationException("A CV file is required.");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new CvValidationException("File exceeds the 10 MB limit.");
        }

        String extension = extractExtension(file.getOriginalFilename());
        boolean extensionOk = ALLOWED_EXTENSIONS.contains(extension);
        boolean contentTypeOk = file.getContentType() != null
                && ALLOWED_CONTENT_TYPES.contains(file.getContentType());

        if (!extensionOk || !contentTypeOk) {
            throw new CvValidationException("Unsupported file type. Allowed formats: PDF, DOC, DOCX.");
        }
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
