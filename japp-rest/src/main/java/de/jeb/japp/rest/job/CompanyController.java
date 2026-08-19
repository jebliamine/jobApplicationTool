package de.jeb.japp.rest.job;

import de.jeb.japp.model.company.dto.CompanyRequest;
import de.jeb.japp.model.company.dto.CompanyResponse;
import de.jeb.japp.model.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/companies")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping
    public List<CompanyResponse> getCompanies(@AuthenticationPrincipal User user) {
        return companyService.list(user).stream().map(CompanyResponse::from).toList();
    }

    @PostMapping
    public CompanyResponse createCompany(@RequestBody CompanyRequest request, @AuthenticationPrincipal User user) {
        return CompanyResponse.from(companyService.create(request, user));
    }

    @GetMapping("/{id}")
    public CompanyResponse getCompany(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        return CompanyResponse.from(companyService.get(id, user));
    }

    @PutMapping("/{id}")
    public CompanyResponse updateCompany(
            @PathVariable UUID id,
            @RequestBody CompanyRequest request,
            @AuthenticationPrincipal User user
    ) {
        return CompanyResponse.from(companyService.update(id, request, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompany(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        companyService.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}
