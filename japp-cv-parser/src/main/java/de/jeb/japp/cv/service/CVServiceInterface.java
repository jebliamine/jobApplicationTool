package de.jeb.japp.cv.service;

import de.jeb.japp.model.cv.CVDocument;
import de.jeb.japp.model.user.User;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
public interface CVServiceInterface {

    public CVDocument uploadCv(MultipartFile file, String title, User owner);

    public CVDocument getCv(UUID id, User requester);

    public List<CVDocument> getAll();

    public List<CVDocument> getAllByOwner(User user);

    /** Authorizes the same way as {@link #getCv}, then loads the physical file through the storage abstraction. */
    public Resource loadResource(CVDocument document);

    /** Authorizes, deletes the physical file, then the database record. */
    public void deleteCv(UUID id, User requester);

}

