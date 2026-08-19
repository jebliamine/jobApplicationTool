package de.jeb.japp.rest.job;

import de.jeb.japp.job.service.JobService;
import de.jeb.japp.model.job.dto.JobRequest;
import de.jeb.japp.model.job.dto.JobResponse;
import de.jeb.japp.model.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping
    public List<JobResponse> getJobs(@AuthenticationPrincipal User user) {
        return jobService.list(user).stream().map(JobResponse::from).toList();
    }

    @PostMapping
    public JobResponse createJob(@RequestBody JobRequest request, @AuthenticationPrincipal User user) {
        return JobResponse.from(jobService.create(request, user));
    }

    @GetMapping("/{id}")
    public JobResponse getJob(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        return JobResponse.from(jobService.get(id, user));
    }

    @PutMapping("/{id}")
    public JobResponse updateJob(
            @PathVariable UUID id,
            @RequestBody JobRequest request,
            @AuthenticationPrincipal User user
    ) {
        return JobResponse.from(jobService.update(id, request, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        jobService.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}
