package de.jeb.japp.rest.generation;

import de.jeb.japp.generation.service.GenerationRequestService;
import de.jeb.japp.model.coverLetter.dto.CoverLetterResponse;
import de.jeb.japp.model.generation.GenerationRequest;
import de.jeb.japp.model.generation.dto.GenerationRequestCreateRequest;
import de.jeb.japp.model.generation.dto.GenerationRequestResponse;
import de.jeb.japp.model.user.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/generation-requests")
public class GenerationRequestController {

    private final GenerationRequestService generationRequestService;

    public GenerationRequestController(GenerationRequestService generationRequestService) {
        this.generationRequestService = generationRequestService;
    }

    @GetMapping
    public List<GenerationRequestResponse> getGenerationRequests(@AuthenticationPrincipal User user) {
        return generationRequestService.list(user).stream().map(this::toResponse).toList();
    }

    @PostMapping
    public GenerationRequestResponse createGenerationRequest(
            @RequestBody GenerationRequestCreateRequest request,
            @AuthenticationPrincipal User user
    ) {
        return toResponse(generationRequestService.create(request, user));
    }

    @GetMapping("/{id}")
    public GenerationRequestResponse getGenerationRequest(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        return toResponse(generationRequestService.get(id, user));
    }

    private GenerationRequestResponse toResponse(GenerationRequest request) {
        CoverLetterResponse coverLetter = generationRequestService.findCoverLetter(request.getId())
                .map(CoverLetterResponse::from)
                .orElse(null);
        return GenerationRequestResponse.from(request, coverLetter);
    }
}
