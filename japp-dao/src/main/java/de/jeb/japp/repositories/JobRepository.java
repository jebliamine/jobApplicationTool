package de.jeb.japp.repositories;

import de.jeb.japp.model.job.Job;
import de.jeb.japp.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JobRepository extends JpaRepository<Job, UUID> {
    List<Job> findByOwner(User owner);

    boolean existsByCompanyId(UUID companyId);

    long countByOwner(User owner);
}
