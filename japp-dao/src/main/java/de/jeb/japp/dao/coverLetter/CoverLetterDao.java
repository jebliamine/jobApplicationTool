package de.jeb.japp.dao.coverLetter;

import de.jeb.japp.model.coverLetter.CoverLetter;
import de.jeb.japp.repositories.CoverLetterRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CoverLetterDao {
    private final CoverLetterRepository coverLetterRepository;

    public CoverLetterDao(CoverLetterRepository coverLetterRepository) {
        this.coverLetterRepository = coverLetterRepository;
    }


    public List<CoverLetter> getAllLetters() {
        return coverLetterRepository.findAll();
    }

    public Optional<CoverLetter> getLetterById(UUID id) {
        return coverLetterRepository.findById(id);
    }

    public CoverLetter saveLetter(CoverLetter coverLetter) {
        return coverLetterRepository.save(coverLetter);
    }

    public void deleteLetter(UUID id) {
        coverLetterRepository.deleteById(id);
    }

    public void deleteAll() {
        coverLetterRepository.deleteAll();
    }


}
