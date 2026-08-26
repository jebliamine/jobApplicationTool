package de.jeb.japp.dashboard.service;

import de.jeb.japp.application.service.ApplicationService;
import de.jeb.japp.cv.service.CVServiceInterface;
import de.jeb.japp.generation.service.CoverLetterService;
import de.jeb.japp.generation.service.GenerationRequestService;
import de.jeb.japp.job.service.JobService;
import de.jeb.japp.model.application.Application;
import de.jeb.japp.model.application.ApplicationStatus;
import de.jeb.japp.model.dashboard.dto.DashboardResponse;
import de.jeb.japp.model.generation.GenerationStatus;
import de.jeb.japp.model.user.User;
import de.jeb.japp.model.user.UserRole;
import de.jeb.japp.user.service.UserServiceInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private JobService jobService;
    @Mock
    private ApplicationService applicationService;
    @Mock
    private CVServiceInterface cvService;
    @Mock
    private CoverLetterService coverLetterService;
    @Mock
    private GenerationRequestService generationRequestService;
    @Mock
    private UserServiceInterface userService;

    private DashboardService dashboardService;

    private User owner;
    private User admin;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardService(
                jobService, applicationService, cvService, coverLetterService, generationRequestService, userService);

        owner = new User();
        owner.setId(UUID.randomUUID());
        owner.setRole(UserRole.USER);

        admin = new User();
        admin.setId(UUID.randomUUID());
        admin.setRole(UserRole.ADMIN);

        // Funnel metrics are computed from applicationService.list(...) — default to empty so
        // existing tests that don't care about funnel metrics aren't forced to stub it.
        lenient().when(applicationService.list(owner)).thenReturn(List.of());
        lenient().when(applicationService.list(admin)).thenReturn(List.of());
    }

    private Application applicationWithStatus(ApplicationStatus status) {
        Application application = new Application();
        application.setStatus(status);
        application.setUpdatedAt(LocalDateTime.now());
        return application;
    }

    private Map<GenerationStatus, Long> statusCounts(long pending, long inProgress, long completed, long failed) {
        Map<GenerationStatus, Long> counts = new EnumMap<>(GenerationStatus.class);
        counts.put(GenerationStatus.PENDING, pending);
        counts.put(GenerationStatus.IN_PROGRESS, inProgress);
        counts.put(GenerationStatus.COMPLETED, completed);
        counts.put(GenerationStatus.FAILED, failed);
        return counts;
    }

    @Test
    void assemblesCountsFromEachDomainServiceForRegularUser() {
        when(cvService.count(owner)).thenReturn(3L);
        when(jobService.count(owner)).thenReturn(5L);
        when(applicationService.count(owner)).thenReturn(7L);
        when(coverLetterService.countActive(owner)).thenReturn(2L);
        when(coverLetterService.countArchived(owner)).thenReturn(1L);
        when(generationRequestService.count(owner)).thenReturn(4L);
        when(generationRequestService.countByStatus(owner)).thenReturn(statusCounts(1, 1, 2, 0));

        DashboardResponse response = dashboardService.getDashboard(owner);

        assertThat(response.getCvCount()).isEqualTo(3L);
        assertThat(response.getJobCount()).isEqualTo(5L);
        assertThat(response.getApplicationCount()).isEqualTo(7L);
        assertThat(response.getActiveCoverLetterCount()).isEqualTo(2L);
        assertThat(response.getArchivedCoverLetterCount()).isEqualTo(1L);
        assertThat(response.getGenerationRequestCount()).isEqualTo(4L);
        assertThat(response.getGenerationStatusCounts()).isEqualTo(statusCounts(1, 1, 2, 0));
    }

    @Test
    void regularUserNeverReceivesTotalUsers() {
        when(generationRequestService.countByStatus(owner)).thenReturn(statusCounts(0, 0, 0, 0));

        DashboardResponse response = dashboardService.getDashboard(owner);

        assertThat(response.getTotalUsers()).isNull();
        verify(userService, never()).countAllUsers();
    }

    @Test
    void adminReceivesTotalUsers() {
        when(userService.countAllUsers()).thenReturn(42L);
        when(generationRequestService.countByStatus(admin)).thenReturn(statusCounts(0, 0, 0, 0));

        DashboardResponse response = dashboardService.getDashboard(admin);

        assertThat(response.getTotalUsers()).isEqualTo(42L);
    }

    @Test
    void eachDomainServiceIsQueriedWithTheAuthenticatedRequester() {
        when(generationRequestService.countByStatus(owner)).thenReturn(statusCounts(0, 0, 0, 0));

        dashboardService.getDashboard(owner);

        verify(cvService).count(owner);
        verify(jobService).count(owner);
        verify(applicationService).count(owner);
        verify(coverLetterService).countActive(owner);
        verify(coverLetterService).countArchived(owner);
        verify(generationRequestService).count(owner);
        verify(generationRequestService).countByStatus(owner);
    }

    @Test
    void emptyUserGetsAllZeroCounts() {
        when(cvService.count(owner)).thenReturn(0L);
        when(jobService.count(owner)).thenReturn(0L);
        when(applicationService.count(owner)).thenReturn(0L);
        when(coverLetterService.countActive(owner)).thenReturn(0L);
        when(coverLetterService.countArchived(owner)).thenReturn(0L);
        when(generationRequestService.count(owner)).thenReturn(0L);
        when(generationRequestService.countByStatus(owner)).thenReturn(statusCounts(0, 0, 0, 0));

        DashboardResponse response = dashboardService.getDashboard(owner);

        assertThat(response.getCvCount()).isZero();
        assertThat(response.getJobCount()).isZero();
        assertThat(response.getApplicationCount()).isZero();
        assertThat(response.getActiveCoverLetterCount()).isZero();
        assertThat(response.getArchivedCoverLetterCount()).isZero();
        assertThat(response.getGenerationRequestCount()).isZero();
        assertThat(response.getGenerationStatusCounts().values()).allMatch(count -> count == 0L);
        assertThat(response.getTotalUsers()).isNull();
    }

    @Test
    void includesFunnelMetricsComputedFromTheRequestersApplications() {
        when(generationRequestService.countByStatus(owner)).thenReturn(statusCounts(0, 0, 0, 0));
        when(applicationService.list(owner)).thenReturn(List.of(
                applicationWithStatus(ApplicationStatus.APPLIED),
                applicationWithStatus(ApplicationStatus.OFFER)));

        DashboardResponse response = dashboardService.getDashboard(owner);

        assertThat(response.getFunnelMetrics()).isNotNull();
        assertThat(response.getFunnelMetrics().getTotalApplications()).isEqualTo(2);
        assertThat(response.getFunnelMetrics().getOfferRate()).isEqualTo(0.5);
    }

    @Test
    void funnelMetricsAreEmptyWhenTheRequesterHasNoApplications() {
        when(generationRequestService.countByStatus(owner)).thenReturn(statusCounts(0, 0, 0, 0));

        DashboardResponse response = dashboardService.getDashboard(owner);

        assertThat(response.getFunnelMetrics().getTotalApplications()).isZero();
        assertThat(response.getFunnelMetrics().getResponseRate()).isZero();
        assertThat(response.getFunnelMetrics().getByCompany()).isEmpty();
    }
}
