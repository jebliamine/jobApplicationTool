package de.jeb.japp.model.user.dto;

import de.jeb.japp.model.user.User;
import de.jeb.japp.model.user.UserRole;

import java.time.LocalDateTime;
import java.util.UUID;

/** Admin-facing user listing — unlike UserDto ("my own profile"), this includes id/enabled/createdAt for management actions. */
public class AdminUserResponse {
    private UUID id;
    private String fullName;
    private String email;
    private UserRole role;
    private boolean enabled;
    private LocalDateTime createdAt;

    public AdminUserResponse() {
    }

    public static AdminUserResponse from(User user) {
        AdminUserResponse response = new AdminUserResponse();
        response.id = user.getId();
        response.fullName = user.getFullName();
        response.email = user.getEmail();
        response.role = user.getRole();
        response.enabled = user.isEnabled();
        response.createdAt = user.getCreatedAt();
        return response;
    }

    public UUID getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public UserRole getRole() {
        return role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
