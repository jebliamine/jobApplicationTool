package de.jeb.japp.model.user.dto;

import de.jeb.japp.model.user.User;
import de.jeb.japp.model.user.UserRole;

public class UserDto {
    private String fullName;
    private String email;
    private UserRole role;
    private boolean emailVerified;
    /** Relative to /api/v1 (e.g. "/users/{id}/avatar"), matching how the frontend builds every other API URL. Null when the user has no uploaded avatar. */
    private String avatarUrl;

    public UserDto() {
    }

    public UserDto(String fullName, String email, UserRole role, boolean emailVerified, String avatarUrl) {
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.emailVerified = emailVerified;
        this.avatarUrl = avatarUrl;
    }

    public static UserDto from(User user) {
        String avatarUrl = user.getAvatarStorageKey() != null ? "/users/" + user.getId() + "/avatar" : null;
        return new UserDto(user.getFullName(), user.getEmail(), user.getRole(), user.isEmailVerified(), avatarUrl);
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
