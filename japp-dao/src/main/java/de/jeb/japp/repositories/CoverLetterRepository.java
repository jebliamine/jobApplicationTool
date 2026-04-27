package de.jeb.japp.repositories;

import de.jeb.japp.model.coverLetter.CoverLetter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CoverLetterRepository extends JpaRepository<CoverLetter, UUID> {
}
