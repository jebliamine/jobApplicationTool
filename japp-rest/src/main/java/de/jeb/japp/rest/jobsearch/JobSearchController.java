package de.jeb.japp.rest.jobsearch;

import de.jeb.japp.jobsearch.service.JobSearchService;
import de.jeb.japp.model.jobsearch.dto.ExternalJobSearchResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Live external job search (Adzuna/Jooble/JSearch combined) — distinct from
 * {@code /api/v1/search}, which searches the caller's own tracked jobs/companies/applications/
 * cover letters. Nothing here is persisted; results are only ever saved into a tracked job via
 * the existing {@code POST /api/v1/jobs}, same as paste-to-import.
 */
@RestController
@RequestMapping("api/v1/job-search")
public class JobSearchController {

    private final JobSearchService jobSearchService;

    public JobSearchController(JobSearchService jobSearchService) {
        this.jobSearchService = jobSearchService;
    }

    @GetMapping
    public ExternalJobSearchResponse search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "1") int page
    ) {
        return jobSearchService.search(keyword, location, page);
    }
}
