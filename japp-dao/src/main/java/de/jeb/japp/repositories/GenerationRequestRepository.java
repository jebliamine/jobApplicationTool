package de.jeb.japp.repositories;

import de.jeb.japp.model.cv.GenerationRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GenerationRequestRepository extends JpaRepository<GenerationRequest, UUID> {
}
