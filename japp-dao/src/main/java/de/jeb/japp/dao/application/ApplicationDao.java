package de.jeb.japp.dao.application;

import de.jeb.japp.model.application.Application;
import de.jeb.japp.model.application.ApplicationStatus;
import de.jeb.japp.model.user.User;
import de.jeb.japp.repositories.ApplicationRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;

@Repository
public class ApplicationDao {

    private final ApplicationRepository applicationRepository;

    public ApplicationDao(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    public List<Application> getAllApplications() {
        return applicationRepository.findAll();
    }

    public List<Application> getAllApplicationsByOwner(User owner) {
        return applicationRepository.findByUser(owner);
    }

    public Optional<Application> getApplicationById(UUID id) {
        return applicationRepository.findById(id);
    }

    public Application saveApplication(Application application) {
        return applicationRepository.save(application);
    }

    public void deleteApplication(UUID id) {
        applicationRepository.deleteById(id);
    }

    public boolean existsByJobId(UUID jobId) {
        return applicationRepository.existsByJobId(jobId);
    }

    public long countAll() {
        return applicationRepository.count();
    }

    public long countByOwner(User owner) {
        return applicationRepository.countByUser(owner);
    }

    public Map<ApplicationStatus, Long> countAllGroupByStatus() {
        return toStatusMap(applicationRepository.countAllGroupByStatus());
    }

    public Map<ApplicationStatus, Long> countByOwnerGroupByStatus(User owner) {
        return toStatusMap(applicationRepository.countByUserGroupByStatus(owner));
    }

    public Map<LocalDate, Long> countAllGroupByAppliedAtSince(LocalDate since) {
        return toDateMap(applicationRepository.countAllGroupByAppliedAtSince(since));
    }

    public Map<LocalDate, Long> countByOwnerGroupByAppliedAtSince(User owner, LocalDate since) {
        return toDateMap(applicationRepository.countByUserGroupByAppliedAtSince(owner, since));
    }

    /** Fills in every ApplicationStatus with 0 so callers never need to null-check a missing key. */
    private Map<ApplicationStatus, Long> toStatusMap(List<Object[]> rows) {
        Map<ApplicationStatus, Long> counts = new EnumMap<>(ApplicationStatus.class);
        for (ApplicationStatus status : ApplicationStatus.values()) {
            counts.put(status, 0L);
        }
        for (Object[] row : rows) {
            counts.put((ApplicationStatus) row[0], (Long) row[1]);
        }
        return counts;
    }

    /** Rows only exist for dates with at least one application — callers treat a missing key as 0. */
    private Map<LocalDate, Long> toDateMap(List<Object[]> rows) {
        Map<LocalDate, Long> counts = new TreeMap<>();
        for (Object[] row : rows) {
            counts.put((LocalDate) row[0], (Long) row[1]);
        }
        return counts;
    }
}
