package de.jeb.japp.dao.generation;

import de.jeb.japp.model.generation.GenerationRequest;
import de.jeb.japp.model.user.User;
import de.jeb.japp.repositories.GenerationRequestRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class GenerationRequestDao {

    private final GenerationRequestRepository generationRequestRepository;

    public GenerationRequestDao(GenerationRequestRepository generationRequestRepository) {
        this.generationRequestRepository = generationRequestRepository;
    }

    public List<GenerationRequest> getAllGenerationRequests() {
        return generationRequestRepository.findAll();
    }

    public List<GenerationRequest> getAllGenerationRequestsByOwner(User owner) {
        return generationRequestRepository.findByUser(owner);
    }

    public Optional<GenerationRequest> getGenerationRequestById(UUID id) {
        return generationRequestRepository.findById(id);
    }

    public GenerationRequest saveGenerationRequest(GenerationRequest generationRequest) {
        return generationRequestRepository.save(generationRequest);
    }

    public boolean existsByJobId(UUID jobId) {
        return generationRequestRepository.existsByJobId(jobId);
    }
}
