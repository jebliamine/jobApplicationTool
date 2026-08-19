package de.jeb.japp.rest.ai;

import de.jeb.japp.ai.service.AiProviderCatalogService;
import de.jeb.japp.model.ai.dto.AiProviderResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Authenticated, not ADMIN-only — every logged-in user can see which
 * providers are currently available to generate with. Never returns
 * credentials; the generation form uses this instead of a hardcoded list.
 */
@RestController
@RequestMapping("api/v1/ai/providers")
public class AiProviderController {

    private final AiProviderCatalogService catalogService;

    public AiProviderController(AiProviderCatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    public List<AiProviderResponse> getProviders() {
        return catalogService.listProviders();
    }
}
