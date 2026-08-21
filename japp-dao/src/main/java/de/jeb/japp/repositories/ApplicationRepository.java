package de.jeb.japp.repositories;

import de.jeb.japp.model.application.Application;
import de.jeb.japp.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID> {
    List<Application> findByUser(User user);

    boolean existsByJobId(UUID jobId);

    long countByUser(User user);

    @Query("SELECT a.status, COUNT(a) FROM Application a WHERE a.user = :user GROUP BY a.status")
    List<Object[]> countByUserGroupByStatus(@Param("user") User user);

    @Query("SELECT a.status, COUNT(a) FROM Application a GROUP BY a.status")
    List<Object[]> countAllGroupByStatus();

    @Query("SELECT a.appliedAt, COUNT(a) FROM Application a WHERE a.user = :user AND a.appliedAt >= :since GROUP BY a.appliedAt")
    List<Object[]> countByUserGroupByAppliedAtSince(@Param("user") User user, @Param("since") LocalDate since);

    @Query("SELECT a.appliedAt, COUNT(a) FROM Application a WHERE a.appliedAt >= :since GROUP BY a.appliedAt")
    List<Object[]> countAllGroupByAppliedAtSince(@Param("since") LocalDate since);
}
