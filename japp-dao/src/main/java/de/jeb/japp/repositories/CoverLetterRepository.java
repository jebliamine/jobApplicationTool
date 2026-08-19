package de.jeb.japp.repositories;

import de.jeb.japp.model.coverLetter.CoverLetter;
import de.jeb.japp.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CoverLetterRepository extends JpaRepository<CoverLetter, UUID> {
    List<CoverLetter> findByOwner(User owner);

    Optional<CoverLetter> findByGenerationRequestId(UUID generationRequestId);
}
