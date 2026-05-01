package de.jeb.japp.repositories;

import de.jeb.japp.model.cv.GenerationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface GenerationRequestRepository extends JpaRepository<GenerationRequest, UUID> {
}
