package de.jeb.japp.model.user.dto;

/** Request body for POST /api/v1/auth/resend-verification. */
public class ResendVerificationRequest {
    private String email;

    public ResendVerificationRequest() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
