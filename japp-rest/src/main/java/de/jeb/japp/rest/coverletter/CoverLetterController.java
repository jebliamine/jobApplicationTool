package de.jeb.japp.rest.coverletter;

import de.jeb.japp.generation.service.CoverLetterService;
import de.jeb.japp.model.coverLetter.dto.CoverLetterResponse;
import de.jeb.japp.model.coverLetter.dto.CoverLetterUpdateRequest;
import de.jeb.japp.model.user.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/cover-letters")
public class CoverLetterController {

    private final CoverLetterService coverLetterService;

    public CoverLetterController(CoverLetterService coverLetterService) {
        this.coverLetterService = coverLetterService;
    }

    @GetMapping
    public List<CoverLetterResponse> getCoverLetters(@AuthenticationPrincipal User user) {
        return coverLetterService.list(user).stream().map(CoverLetterResponse::from).toList();
    }

    @GetMapping("/{id}")
    public CoverLetterResponse getCoverLetter(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        return CoverLetterResponse.from(coverLetterService.get(id, user));
    }

    @PutMapping("/{id}")
    public CoverLetterResponse updateCoverLetter(
            @PathVariable UUID id,
            @RequestBody CoverLetterUpdateRequest request,
            @AuthenticationPrincipal User user
    ) {
        return CoverLetterResponse.from(coverLetterService.update(id, request, user));
    }
}
