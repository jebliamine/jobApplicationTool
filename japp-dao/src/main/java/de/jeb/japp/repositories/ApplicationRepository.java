package de.jeb.japp.repositories;

import de.jeb.japp.model.application.Application;
import de.jeb.japp.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID> {
    List<Application> findByUser(User user);

    boolean existsByJobId(UUID jobId);

    long countByUser(User user);
}
