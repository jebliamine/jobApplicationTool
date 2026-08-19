package de.jeb.japp.repositories;

import de.jeb.japp.model.generation.GenerationRequest;
import de.jeb.japp.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GenerationRequestRepository extends JpaRepository<GenerationRequest, UUID> {
    List<GenerationRequest> findByUser(User user);

    boolean existsByJobId(UUID jobId);

    long countByUser(User user);

    @Query("SELECT gr.status, COUNT(gr) FROM GenerationRequest gr WHERE gr.user = :user GROUP BY gr.status")
    List<Object[]> countByUserGroupByStatus(@Param("user") User user);

    @Query("SELECT gr.status, COUNT(gr) FROM GenerationRequest gr GROUP BY gr.status")
    List<Object[]> countAllGroupByStatus();
}
