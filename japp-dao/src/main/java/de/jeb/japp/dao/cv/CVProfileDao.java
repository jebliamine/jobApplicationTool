package de.jeb.japp.dao.cv;

import de.jeb.japp.model.cv.CVProfile;
import de.jeb.japp.repositories.CVProfileRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class CVProfileDao {

    private final CVProfileRepository repository;

    public CVProfileDao(CVProfileRepository repository) {
        this.repository = repository;
    }

    public Optional<CVProfile> getByCvDocumentId(UUID cvDocumentId) {
        return repository.findByCvDocumentId(cvDocumentId);
    }

    public CVProfile save(CVProfile profile) {
        return repository.save(profile);
    }
}
