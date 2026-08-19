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

    public Optional<AiProviderConfiguration> getByProvider(String provider) {
        return repository.findByProvider(provider);
    }

    public List<AiProviderConfiguration> getAll() {
        return repository.findAllByOrderByProviderAsc();
    }

    public boolean existsByProvider(String provider) {
        return repository.existsByProvider(provider);
    }

    public AiProviderConfiguration save(AiProviderConfiguration configuration) {
        return repository.save(configuration);
    }

    public Optional<AiProviderConfiguration> getById(UUID id) {
        return repository.findById(id);
    }
}
