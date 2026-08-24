package de.jeb.japp.model.user.dto;

import de.jeb.japp.model.user.UserRole;

public class UpdateUserRoleRequest {
    private UserRole role;

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
}
