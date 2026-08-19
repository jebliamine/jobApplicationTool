package de.jeb.japp.rest.coverletter;

import de.jeb.japp.generation.service.CoverLetterService;
import de.jeb.japp.model.coverLetter.dto.CoverLetterResponse;
import de.jeb.japp.model.coverLetter.dto.CoverLetterUpdateRequest;
import de.jeb.japp.model.user.User;
import org.springframework.http.ResponseEntity;
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
    public List<CoverLetterResponse> getCoverLetters(
            @RequestParam(defaultValue = "false") boolean archived,
            @AuthenticationPrincipal User user
    ) {
        return coverLetterService.list(user, archived).stream().map(CoverLetterResponse::from).toList();
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

    @PatchMapping("/{id}/archive")
    public CoverLetterResponse archiveCoverLetter(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        return CoverLetterResponse.from(coverLetterService.archive(id, user));
    }

    @PatchMapping("/{id}/unarchive")
    public CoverLetterResponse unarchiveCoverLetter(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        return CoverLetterResponse.from(coverLetterService.unarchive(id, user));
    }

    /** Permanent deletion — ADMIN only, enforced in the service, not just hidden in the UI. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCoverLetter(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        coverLetterService.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}
