package de.jeb.japp.model.user.dto;

/** Request body for POST /api/v1/auth/verify-email. */
public class VerifyEmailRequest {
    private String token;

    public VerifyEmailRequest() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
