package de.jeb.japp.dao.cv;

import de.jeb.japp.model.cv.CVDocument;
import de.jeb.japp.model.user.User;
import de.jeb.japp.repositories.CVRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CVDao {

    private final CVRepository cvRepository;

    public CVDao(CVRepository cvRepository) {
        this.cvRepository = cvRepository;

    }

    public List<CVDocument> getAllCVs() {
//        only admin can
        return cvRepository.findAll();
    }

    public Optional<CVDocument> getCVById(UUID id) {
        return cvRepository.findById(id);
    }

    public CVDocument saveCV(CVDocument cvDocument) {
        return cvRepository.save(cvDocument);
    }

    public List<CVDocument> getAllCVsByOwner(User user) {
        return cvRepository.findByOwner(user);
    }

    public void deleteCV(UUID id) {
        cvRepository.deleteById(id);
    }

    public void deleteAll() {
        cvRepository.deleteAll();
    }

}
