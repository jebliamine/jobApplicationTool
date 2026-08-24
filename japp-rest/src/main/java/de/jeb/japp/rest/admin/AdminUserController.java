package de.jeb.japp.rest.admin;

import de.jeb.japp.model.user.User;
import de.jeb.japp.model.user.dto.AdminCreateUserRequest;
import de.jeb.japp.model.user.dto.AdminUserResponse;
import de.jeb.japp.model.user.dto.UpdateUserEnabledRequest;
import de.jeb.japp.model.user.dto.UpdateUserRoleRequest;
import de.jeb.japp.user.service.AdminUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * ADMIN-only. Authorization is enforced entirely in AdminUserService (manual
 * requester.getRole() == ADMIN check, matching the rest of the project) —
 * this controller only passes the authenticated User through.
 */
@RestController
@RequestMapping("api/v1/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public List<AdminUserResponse> getUsers(@AuthenticationPrincipal User user) {
        return adminUserService.listUsers(user);
    }

    @PostMapping
    public ResponseEntity<AdminUserResponse> createUser(
            @RequestBody AdminCreateUserRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminUserService.createUser(request, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        adminUserService.deleteUser(id, user);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/role")
    public AdminUserResponse updateRole(
            @PathVariable UUID id,
            @RequestBody UpdateUserRoleRequest request,
            @AuthenticationPrincipal User user
    ) {
        return adminUserService.updateRole(id, request, user);
    }

    @PutMapping("/{id}/enabled")
    public AdminUserResponse updateEnabled(
            @PathVariable UUID id,
            @RequestBody UpdateUserEnabledRequest request,
            @AuthenticationPrincipal User user
    ) {
        return adminUserService.updateEnabled(id, request, user);
    }
}
