package de.jeb.japp.model.user.dto;

/** Request body for POST /api/v1/auth/forgot-password. */
public class ForgotPasswordRequest {
    private String email;

    public ForgotPasswordRequest() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
