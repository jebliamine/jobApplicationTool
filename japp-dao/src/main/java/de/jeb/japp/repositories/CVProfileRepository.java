package de.jeb.japp.repositories;

import de.jeb.japp.model.cv.CVProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CVProfileRepository extends JpaRepository<CVProfile, UUID> {
    Optional<CVProfile> findByCvDocumentId(UUID cvDocumentId);
}
