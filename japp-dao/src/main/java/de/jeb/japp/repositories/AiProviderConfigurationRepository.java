package de.jeb.japp.repositories;

import de.jeb.japp.model.ai.AiProviderConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiProviderConfigurationRepository extends JpaRepository<AiProviderConfiguration, java.util.UUID> {
    Optional<AiProviderConfiguration> findByProvider(String provider);

    List<AiProviderConfiguration> findAllByOrderByProviderAsc();

    boolean existsByProvider(String provider);
}
