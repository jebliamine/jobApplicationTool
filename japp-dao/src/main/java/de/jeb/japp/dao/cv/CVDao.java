package de.jeb.japp.dao.cv;

import de.jeb.japp.model.cv.CVDocument;
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

    public List<CVDocument> getAllLetters() {
        return cvRepository.findAll();
    }

    public Optional<CVDocument> getLetterById(UUID id) {
        return cvRepository.findById(id);
    }

    public CVDocument saveLetter(CVDocument cvDocument) {
        return cvRepository.save(cvDocument);
    }

    public void deleteLetter(UUID id) {
        cvRepository.deleteById(id);
    }

    public void deleteAll() {
        cvRepository.deleteAll();
    }

}
