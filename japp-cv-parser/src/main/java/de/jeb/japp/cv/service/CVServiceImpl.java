package de.jeb.japp.cv.service;

import de.jeb.japp.commons.exceptions.cv.CVAccessDeniedException;
import de.jeb.japp.commons.exceptions.cv.CVNotFoundException;
import de.jeb.japp.commons.exceptions.cv.CVStorageException;
import de.jeb.japp.commons.exceptions.cv.CVValidationException;
import de.jeb.japp.cv.service.parser.DocumentExtractionService;
import de.jeb.japp.cv.service.parser.normalizer.NormalizedDocument;
import de.jeb.japp.dao.cv.CVDao;
import de.jeb.japp.dao.user.UserDao;
import de.jeb.japp.file.storage.services.FileStorageServiceInterface;
import de.jeb.japp.model.cv.CVDocument;
import de.jeb.japp.model.cv.ExtractionStatus;
import de.jeb.japp.model.storage.StoredFile;
import de.jeb.japp.model.user.User;
import de.jeb.japp.model.user.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(CVServiceImpl.class);

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
    private final DocumentExtractionService extractionService;

    public CVServiceImpl(
            CVDao cvDao,
            UserDao userDao,
            FileStorageServiceInterface storageService,
            DocumentExtractionService extractionService
    ) {
        this.cvDao = cvDao;
        this.userDao = userDao;
        this.storageService = storageService;
        this.extractionService = extractionService;
    }

    @Override
    public CVDocument uploadCv(MultipartFile file, String title, User owner) {
        validateUpload(file, title);

        StoredFile stored;
        try {
            stored = storageService.save(file, owner.getId());
        } catch (Exception e) {
            throw new CVStorageException("Upload failed", e);
        }

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

        applyExtraction(doc, stored);

        return cvDao.saveCV(doc);
    }

    /**
     * Best-effort: a CV is still a valid upload even if text extraction fails, so any
     * failure here is caught and recorded on the document rather than failing the upload.
     */
    private void applyExtraction(CVDocument doc, StoredFile stored) {
        try {
            Resource resource = storageService.load(stored.getStorageKey());
            NormalizedDocument normalized = extractionService.process(
                    resource, stored.getOriginalFilename(), stored.getContentType());

            doc.setExtractedText(normalized.text());
            doc.setExtractionStatus(ExtractionStatus.COMPLETED);
            doc.setExtractionQuality(normalized.quality() != null ? normalized.quality().name() : null);
            doc.setExtractedAt(LocalDateTime.now());
        } catch (Exception e) {
            log.warn("CV text extraction failed for upload: fileName={}", stored.getOriginalFilename(), e);
            doc.setExtractionStatus(ExtractionStatus.FAILED);
        }
    }

    @Override
    public CVDocument getCv(UUID id, User requester) {
        CVDocument doc = cvDao.getCVById(id).orElseThrow(() -> new CVNotFoundException("CV not found."));
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
    public long count(User requester) {
        return requester.getRole() == UserRole.ADMIN
                ? cvDao.countAll()
                : cvDao.countByOwner(requester);
    }

    @Override
    public Resource loadResource(CVDocument document) {
        Resource resource = storageService.load(document.getStorageKey());
        if (!resource.exists() || !resource.isReadable()) {
            throw new CVNotFoundException("CV file not found.");
        }
        return resource;
    }

    @Override
    public void deleteCv(UUID id, User requester) {
        CVDocument doc = getCv(id, requester);

        try {
            storageService.delete(doc.getStorageKey());
        } catch (IOException e) {
            throw new CVStorageException("Failed to delete CV file", e);
        }

        cvDao.deleteCV(doc.getId());
    }

    private void assertAccess(CVDocument doc, User requester) {
        boolean isOwner = doc.getOwner() != null && doc.getOwner().getId().equals(requester.getId());
        boolean isAdmin = requester.getRole() == UserRole.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new CVAccessDeniedException("You do not have access to this CV.");
        }
    }

    private void validateUpload(MultipartFile file, String title) {
        if (title == null || title.isBlank()) {
            throw new CVValidationException("A title is required.");
        }
        if (file == null || file.isEmpty()) {
            throw new CVValidationException("A CV file is required.");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new CVValidationException("File exceeds the 10 MB limit.");
        }

        String extension = extractExtension(file.getOriginalFilename());
        boolean extensionOk = ALLOWED_EXTENSIONS.contains(extension);
        boolean contentTypeOk = file.getContentType() != null
                && ALLOWED_CONTENT_TYPES.contains(file.getContentType());

        if (!extensionOk || !contentTypeOk) {
            throw new CVValidationException("Unsupported file type. Allowed formats: PDF, DOC, DOCX.");
        }
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
