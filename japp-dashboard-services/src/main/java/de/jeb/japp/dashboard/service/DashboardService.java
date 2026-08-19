package de.jeb.japp.dashboard.service;

import de.jeb.japp.application.service.ApplicationService;
import de.jeb.japp.cv.service.CVServiceInterface;
import de.jeb.japp.dao.user.UserDao;
import de.jeb.japp.generation.service.CoverLetterService;
import de.jeb.japp.generation.service.GenerationRequestService;
import de.jeb.japp.job.service.JobService;
import de.jeb.japp.model.dashboard.dto.DashboardResponse;
import de.jeb.japp.model.user.User;
import de.jeb.japp.model.user.UserRole;
import org.springframework.stereotype.Service;

/**
 * Pure aggregation over the existing domain services — no persistence, no
 * business rules of its own. Every count reuses the ADMIN-sees-everything /
 * USER-sees-own scoping each domain service already implements (see
 * JobService#count, ApplicationService#count, etc.), so authorization is
 * never duplicated here.
 *
 * totalUsers is sourced from UserDao directly rather than a "UserService",
 * because japp-user-Service is not wired into the application context
 * (JappApplication's scanBasePackages covers only de.jeb.japp; the module's
 * code lives under de.jeb.user) — the same reason UserProfileService in
 * japp-rest talks to UserDao directly instead of that module.
 */
@Service
public class DashboardService {

    private final JobService jobService;
    private final ApplicationService applicationService;
    private final CVServiceInterface cvService;
    private final CoverLetterService coverLetterService;
    private final GenerationRequestService generationRequestService;
    private final UserDao userDao;

    public DashboardService(
            JobService jobService,
            ApplicationService applicationService,
            CVServiceInterface cvService,
            CoverLetterService coverLetterService,
            GenerationRequestService generationRequestService,
            UserDao userDao
    ) {
        this.jobService = jobService;
        this.applicationService = applicationService;
        this.cvService = cvService;
        this.coverLetterService = coverLetterService;
        this.generationRequestService = generationRequestService;
        this.userDao = userDao;
    }

    public DashboardResponse getDashboard(User requester) {
        DashboardResponse response = new DashboardResponse();
        response.setCvCount(cvService.count(requester));
        response.setJobCount(jobService.count(requester));
        response.setApplicationCount(applicationService.count(requester));
        response.setActiveCoverLetterCount(coverLetterService.countActive(requester));
        response.setArchivedCoverLetterCount(coverLetterService.countArchived(requester));
        response.setGenerationRequestCount(generationRequestService.count(requester));
        response.setGenerationStatusCounts(generationRequestService.countByStatus(requester));

        if (requester.getRole() == UserRole.ADMIN) {
            response.setTotalUsers(userDao.countAll());
        }

        return response;
    }
}
