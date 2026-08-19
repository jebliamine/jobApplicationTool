package de.jeb.japp.dao.job;

import de.jeb.japp.model.job.Job;
import de.jeb.japp.model.user.User;
import de.jeb.japp.repositories.JobRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JobDao {

    private final JobRepository jobRepository;

    public JobDao(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    public List<Job> getAllJobsByOwner(User owner) {
        return jobRepository.findByOwner(owner);
    }

    public Optional<Job> getJobById(UUID id) {
        return jobRepository.findById(id);
    }

    public Job saveJob(Job job) {
        return jobRepository.save(job);
    }

    public void deleteJob(UUID id) {
        jobRepository.deleteById(id);
    }

    public boolean existsByCompanyId(UUID companyId) {
        return jobRepository.existsByCompanyId(companyId);
    }

    public long countAll() {
        return jobRepository.count();
    }

    public long countByOwner(User owner) {
        return jobRepository.countByOwner(owner);
    }
}
