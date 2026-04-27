package de.jeb.japp.repositories;

import de.jeb.japp.model.cv.CVDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CVRepository extends JpaRepository<CVDocument, UUID> {
}
