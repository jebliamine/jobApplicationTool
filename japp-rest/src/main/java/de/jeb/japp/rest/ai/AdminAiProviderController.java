package de.jeb.japp.rest.ai;

import de.jeb.japp.ai.service.AdminAiProviderService;
import de.jeb.japp.model.ai.dto.AdminAiProviderResponse;
import de.jeb.japp.model.ai.dto.AiProviderTestResult;
import de.jeb.japp.model.ai.dto.AiProviderUpdateRequest;
import de.jeb.japp.model.user.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PutMapping("/{provider}")
    public AdminAiProviderResponse updateProvider(
            @PathVariable String provider,
            @RequestBody AiProviderUpdateRequest request,
            @AuthenticationPrincipal User user
    ) {
        return adminAiProviderService.updateProvider(provider, request, user);
    }

    @PostMapping("/{provider}/test")
    public AiProviderTestResult testProvider(@PathVariable String provider, @AuthenticationPrincipal User user) {
        return adminAiProviderService.testConnection(provider, user);
    }
}
