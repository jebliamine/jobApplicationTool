package de.jeb.japp.rest.dashboard;

import de.jeb.japp.dashboard.service.DashboardService;
import de.jeb.japp.model.dashboard.dto.DashboardResponse;
import de.jeb.japp.model.user.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * A single role-aware endpoint, matching the ADMIN-sees-everything /
 * USER-sees-own convention every other list endpoint already uses (Job,
 * Application, CoverLetter, GenerationRequest) rather than a separate
 * /admin-namespaced endpoint returning a near-duplicate shape.
 */
@RestController
@RequestMapping("api/v1/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public DashboardResponse getDashboard(@AuthenticationPrincipal User user) {
        return dashboardService.getDashboard(user);
    }
}
