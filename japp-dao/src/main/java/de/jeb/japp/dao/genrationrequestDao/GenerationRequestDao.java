package de.jeb.japp.dao.genrationrequestDao;

import de.jeb.japp.model.cv.GenerationRequest;
import de.jeb.japp.repositories.GenerationRequestRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class GenerationRequestDao {

    private final GenerationRequestRepository requestRepository;


    public GenerationRequestDao(GenerationRequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    public List<GenerationRequest> getAllLetters() {
        return requestRepository.findAll();
    }

    public Optional<GenerationRequest> getLetterById(UUID id) {
        return requestRepository.findById(id);
    }

    public GenerationRequest saveLetter(GenerationRequest generationRequest) {
        return requestRepository.save(generationRequest);
    }

    public void deleteLetter(UUID id) {
        requestRepository.deleteById(id);
    }

    public void deleteAll() {
        requestRepository.deleteAll();
    }

}
