package de.jeb.japp.rest.application;

import de.jeb.japp.application.service.ApplicationService;
import de.jeb.japp.model.application.dto.ApplicationRequest;
import de.jeb.japp.model.application.dto.ApplicationResponse;
import de.jeb.japp.model.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping
    public List<ApplicationResponse> getApplications(@AuthenticationPrincipal User user) {
        return applicationService.list(user).stream().map(ApplicationResponse::from).toList();
    }

    @PostMapping
    public ApplicationResponse createApplication(
            @RequestBody ApplicationRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ApplicationResponse.from(applicationService.create(request, user));
    }

    @GetMapping("/{id}")
    public ApplicationResponse getApplication(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        return ApplicationResponse.from(applicationService.get(id, user));
    }

    @PutMapping("/{id}")
    public ApplicationResponse updateApplication(
            @PathVariable UUID id,
            @RequestBody ApplicationRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ApplicationResponse.from(applicationService.update(id, request, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        applicationService.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}
