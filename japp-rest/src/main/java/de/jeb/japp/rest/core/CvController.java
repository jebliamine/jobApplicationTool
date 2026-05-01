package de.jeb.japp.rest.core;

import de.jeb.japp.cv.service.CVServiceInterface;
import de.jeb.japp.model.cv.CVDocument;
import de.jeb.japp.model.user.User;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/cv")
public class CvController {

    private final CVServiceInterface cvService;

    public CvController(CVServiceInterface cvService) {
        this.cvService = cvService;
    }


    @GetMapping("/{id}")
    public CVDocument getCv(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        user.getPasswordHash();
        return cvService.getCv(id);
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public List<CVDocument> getAllCvsByOwner(@AuthenticationPrincipal User user) {
        return cvService.getAllByOwner(user);
    }

    @PostMapping(consumes = "multipart/form-data")
    @PreAuthorize("hasRole('USER')")
    public CVDocument uploadCv(
            @RequestPart("file") MultipartFile file,
            @RequestPart("title") String title,
            @AuthenticationPrincipal User user
    ) {
        return cvService.uploadCv(file, title, user);
    }


}


