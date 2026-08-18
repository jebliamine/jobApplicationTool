package de.jeb.japp.model.user.dto;

import de.jeb.japp.model.user.User;
import de.jeb.japp.model.user.UserRole;

public class UserDto {
    private String fullName;
    private String email;
    private UserRole role;

    public UserDto() {
    }

    public UserDto(String fullName, String email, UserRole role) {
        this.fullName = fullName;
        this.email = email;
        this.role = role;
    }

    public static UserDto from(User user) {
        return new UserDto(user.getFullName(), user.getEmail(), user.getRole());
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
}
