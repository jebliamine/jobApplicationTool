package de.jeb.japp.model.user.dto;

/** Fields a user may change about their own profile via PUT /api/v1/users/me. */
public class UpdateUserRequest {
    private String fullName;
    private String email;

    public UpdateUserRequest() {
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
}
