package de.jeb.japp.dao.coverletter;

import de.jeb.japp.model.coverLetter.CoverLetter;
import de.jeb.japp.model.user.User;
import de.jeb.japp.repositories.CoverLetterRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CoverLetterDao {

    private final CoverLetterRepository coverLetterRepository;

    public CoverLetterDao(CoverLetterRepository coverLetterRepository) {
        this.coverLetterRepository = coverLetterRepository;
    }

    public List<CoverLetter> getAllCoverLetters(boolean archived) {
        return coverLetterRepository.findByArchived(archived);
    }

    public List<CoverLetter> getAllCoverLettersByOwner(User owner, boolean archived) {
        return coverLetterRepository.findByOwnerAndArchived(owner, archived);
    }

    public Optional<CoverLetter> getCoverLetterById(UUID id) {
        return coverLetterRepository.findById(id);
    }

    public Optional<CoverLetter> getCoverLetterByGenerationRequestId(UUID generationRequestId) {
        return coverLetterRepository.findByGenerationRequestId(generationRequestId);
    }

    public CoverLetter saveCoverLetter(CoverLetter coverLetter) {
        return coverLetterRepository.save(coverLetter);
    }

    public void deleteCoverLetter(UUID id) {
        coverLetterRepository.deleteById(id);
    }

    public long countByArchived(boolean archived) {
        return coverLetterRepository.countByArchived(archived);
    }

    public long countByOwnerAndArchived(User owner, boolean archived) {
        return coverLetterRepository.countByOwnerAndArchived(owner, archived);
    }
}
