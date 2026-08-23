package de.jeb.japp.repositories;

import de.jeb.japp.model.ai.AiProviderConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AiProviderConfigurationRepository extends JpaRepository<AiProviderConfiguration, UUID> {
    List<AiProviderConfiguration> findAllByOrderByDisplayNameAsc();

    boolean existsByAdapterType(String adapterType);

    Optional<AiProviderConfiguration> findFirstByAdapterType(String adapterType);
}
