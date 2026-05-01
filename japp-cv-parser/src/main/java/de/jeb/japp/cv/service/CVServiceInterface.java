package de.jeb.japp.cv.service;

import de.jeb.japp.model.cv.CVDocument;
import de.jeb.japp.model.user.User;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
public interface CVServiceInterface {

    public CVDocument uploadCv(MultipartFile file, String title, User owner);

    public CVDocument getCv(UUID id);

    public List<CVDocument> getAll();

    public List<CVDocument> getAllByOwner(User user);

}

