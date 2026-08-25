package de.jeb.japp.rest.tag;

import de.jeb.japp.model.tag.dto.TagRequest;
import de.jeb.japp.model.tag.dto.TagResponse;
import de.jeb.japp.model.user.User;
import de.jeb.japp.tag.service.TagService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    public List<TagResponse> getTags(@AuthenticationPrincipal User user) {
        return tagService.list(user).stream().map(TagResponse::from).toList();
    }

    @PostMapping
    public TagResponse createTag(@RequestBody TagRequest request, @AuthenticationPrincipal User user) {
        return TagResponse.from(tagService.create(request, user));
    }

    @PutMapping("/{id}")
    public TagResponse renameTag(
            @PathVariable UUID id,
            @RequestBody TagRequest request,
            @AuthenticationPrincipal User user
    ) {
        return TagResponse.from(tagService.rename(id, request, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        tagService.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}
