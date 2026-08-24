package de.jeb.japp.rest.core;

import de.jeb.japp.cv.service.CVServiceInterface;
import de.jeb.japp.generation.service.CvProfileExtractionService;
import de.jeb.japp.model.cv.CVDocument;
import de.jeb.japp.model.cv.dto.CVProfileResponse;
import de.jeb.japp.model.cv.dto.CVResponse;
import de.jeb.japp.model.user.User;
import de.jeb.japp.model.user.UserRole;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/cv")
public class CvController {

    private final CVServiceInterface cvService;
    private final CvProfileExtractionService cvProfileExtractionService;

    public CvController(CVServiceInterface cvService, CvProfileExtractionService cvProfileExtractionService) {
        this.cvService = cvService;
        this.cvProfileExtractionService = cvProfileExtractionService;
    }

    @GetMapping("/{id}")
    public CVResponse getCv(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        return CVResponse.from(cvService.getCv(id, user));
    }

    @GetMapping
    public List<CVResponse> getCvs(@AuthenticationPrincipal User user) {
        List<CVDocument> documents = user.getRole() == UserRole.ADMIN
                ? cvService.getAll()
                : cvService.getAllByOwner(user);

        return documents.stream().map(CVResponse::from).toList();
    }

    @PostMapping(consumes = "multipart/form-data")
    public CVResponse uploadCv(
            @RequestPart("file") MultipartFile file,
            @RequestPart("title") String title,
            @AuthenticationPrincipal User user
    ) {
        return CVResponse.from(cvService.uploadCv(file, title, user));
    }

    @GetMapping("/{id}/view")
    public ResponseEntity<Resource> viewCv(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        CVDocument doc = cvService.getCv(id, user);
        Resource resource = cvService.loadResource(doc);

        ContentDisposition disposition = ContentDisposition.inline()
                .filename(doc.getFileName(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(doc.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(resource);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadCv(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        CVDocument doc = cvService.getCv(id, user);
        Resource resource = cvService.loadResource(doc);

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(doc.getFileName(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(doc.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(resource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCv(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        cvService.deleteCv(id, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/profile")
    public CVProfileResponse getCvProfile(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        return cvProfileExtractionService.get(id, user)
                .map(CVProfileResponse::from)
                .orElseGet(CVProfileResponse::notAttempted);
    }

    @PostMapping("/{id}/profile")
    public CVProfileResponse generateCvProfile(
            @PathVariable UUID id,
            @RequestParam(required = false) UUID providerId,
            @AuthenticationPrincipal User user
    ) {
        return CVProfileResponse.from(cvProfileExtractionService.generate(id, providerId, user));
    }
}
