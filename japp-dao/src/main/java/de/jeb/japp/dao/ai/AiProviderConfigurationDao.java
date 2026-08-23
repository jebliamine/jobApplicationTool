package de.jeb.japp.dao.ai;

import de.jeb.japp.model.ai.AiProviderConfiguration;
import de.jeb.japp.repositories.AiProviderConfigurationRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AiProviderConfigurationDao {

    private final AiProviderConfigurationRepository repository;

    public AiProviderConfigurationDao(AiProviderConfigurationRepository repository) {
        this.repository = repository;
    }

    public Optional<AiProviderConfiguration> getById(UUID id) {
        return repository.findById(id);
    }

    public List<AiProviderConfiguration> getAll() {
        return repository.findAllByOrderByDisplayNameAsc();
    }

    public boolean existsByAdapterType(String adapterType) {
        return repository.existsByAdapterType(adapterType);
    }

    /** The singleton built-in Placeholder row, if it has been seeded. */
    public Optional<AiProviderConfiguration> getFirstByAdapterType(String adapterType) {
        return repository.findFirstByAdapterType(adapterType);
    }

    public AiProviderConfiguration save(AiProviderConfiguration configuration) {
        return repository.save(configuration);
    }

    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}
