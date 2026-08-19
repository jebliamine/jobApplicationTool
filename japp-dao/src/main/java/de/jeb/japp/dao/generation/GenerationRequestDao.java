package de.jeb.japp.dao.generation;

import de.jeb.japp.model.generation.GenerationRequest;
import de.jeb.japp.model.generation.GenerationStatus;
import de.jeb.japp.model.user.User;
import de.jeb.japp.repositories.GenerationRequestRepository;
import org.springframework.stereotype.Repository;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
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

    public long countAll() {
        return generationRequestRepository.count();
    }

    public long countByOwner(User owner) {
        return generationRequestRepository.countByUser(owner);
    }

    public Map<GenerationStatus, Long> countAllGroupByStatus() {
        return toStatusMap(generationRequestRepository.countAllGroupByStatus());
    }

    public Map<GenerationStatus, Long> countByOwnerGroupByStatus(User owner) {
        return toStatusMap(generationRequestRepository.countByUserGroupByStatus(owner));
    }

    /** Fills in every GenerationStatus with 0 so callers never need to null-check a missing key. */
    private Map<GenerationStatus, Long> toStatusMap(List<Object[]> rows) {
        Map<GenerationStatus, Long> counts = new EnumMap<>(GenerationStatus.class);
        for (GenerationStatus status : GenerationStatus.values()) {
            counts.put(status, 0L);
        }
        for (Object[] row : rows) {
            counts.put((GenerationStatus) row[0], (Long) row[1]);
        }
        return counts;
    }
}
