package de.jeb.japp.rest.ai;

import de.jeb.japp.ai.service.AdminAiProviderService;
import de.jeb.japp.model.ai.dto.AdminAiProviderResponse;
import de.jeb.japp.model.ai.dto.AiProviderCreateRequest;
import de.jeb.japp.model.ai.dto.AiProviderTestResult;
import de.jeb.japp.model.ai.dto.AiProviderUpdateRequest;
import de.jeb.japp.model.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * ADMIN-only. Authorization is enforced entirely in AdminAiProviderService
 * (manual requester.getRole() == ADMIN check, matching the rest of the
 * project) — this controller only passes the authenticated User through.
 */
@RestController
@RequestMapping("api/v1/admin/ai/providers")
public class AdminAiProviderController {

    private final AdminAiProviderService adminAiProviderService;

    public AdminAiProviderController(AdminAiProviderService adminAiProviderService) {
        this.adminAiProviderService = adminAiProviderService;
    }

    @GetMapping
    public List<AdminAiProviderResponse> getProviders(@AuthenticationPrincipal User user) {
        return adminAiProviderService.listProviders(user);
    }

    @PostMapping
    public ResponseEntity<AdminAiProviderResponse> createProvider(
            @RequestBody AiProviderCreateRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminAiProviderService.createProvider(request, user));
    }

    @PutMapping("/{id}")
    public AdminAiProviderResponse updateProvider(
            @PathVariable UUID id,
            @RequestBody AiProviderUpdateRequest request,
            @AuthenticationPrincipal User user
    ) {
        return adminAiProviderService.updateProvider(id, request, user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProvider(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        adminAiProviderService.deleteProvider(id, user);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/test")
    public AiProviderTestResult testProvider(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        return adminAiProviderService.testConnection(id, user);
    }
}
