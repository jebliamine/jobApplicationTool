package de.jeb.japp.dao.application;

import de.jeb.japp.model.application.Application;
import de.jeb.japp.model.user.User;
import de.jeb.japp.repositories.ApplicationRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
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
}
