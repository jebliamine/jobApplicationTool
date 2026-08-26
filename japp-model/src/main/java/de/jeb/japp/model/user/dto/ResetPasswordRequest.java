package de.jeb.japp.model.user.dto;

/** Request body for POST /api/v1/auth/reset-password. */
public class ResetPasswordRequest {
    private String token;
    private String newPassword;

    public ResetPasswordRequest() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
