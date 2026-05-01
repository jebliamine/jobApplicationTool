package de.jeb.japp.repositories;

import de.jeb.japp.model.cv.CVDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CVRepository extends JpaRepository<CVDocument, UUID> {
}
