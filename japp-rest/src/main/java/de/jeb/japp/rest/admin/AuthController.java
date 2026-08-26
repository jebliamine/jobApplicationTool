package de.jeb.japp.rest.admin;

import de.jeb.japp.model.user.dto.AuthResponse;
import de.jeb.japp.model.user.dto.ForgotPasswordRequest;
import de.jeb.japp.model.user.dto.LoginRequest;
import de.jeb.japp.model.user.dto.RegisterRequest;
import de.jeb.japp.model.user.dto.ResendVerificationRequest;
import de.jeb.japp.model.user.dto.ResetPasswordRequest;
import de.jeb.japp.model.user.dto.VerifyEmailRequest;
import de.jeb.japp.security.service.AuthServiceInterface;
import de.jeb.japp.user.service.EmailVerificationService;
import de.jeb.japp.user.service.PasswordResetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthServiceInterface authServiceInterface;
    private final PasswordResetService passwordResetService;
    private final EmailVerificationService emailVerificationService;


    public AuthController(
            AuthServiceInterface authServiceInterface,
            PasswordResetService passwordResetService,
            EmailVerificationService emailVerificationService
    ) {
        this.authServiceInterface = authServiceInterface;
        this.passwordResetService = passwordResetService;
        this.emailVerificationService = emailVerificationService;
    }


    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest registerRequest) {
        AuthResponse response = authServiceInterface.register(registerRequest);
        // Best-effort: EmailVerificationService already swallows its own send failures, so
        // registration itself is never blocked or failed by an unreachable mail server.
        emailVerificationService.sendVerificationEmailFor(registerRequest.getEmail());
        return response;
    }


    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest loginRequest) {
        return authServiceInterface.login(loginRequest);
    }

    /** Always 200 regardless of whether the email exists — see PasswordResetService for why. */
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        passwordResetService.requestReset(request.getEmail());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(@RequestBody VerifyEmailRequest request) {
        emailVerificationService.verifyEmail(request.getToken());
        return ResponseEntity.ok().build();
    }

    /** Always 200 regardless of whether the email exists or is already verified. */
    @PostMapping("/resend-verification")
    public ResponseEntity<Void> resendVerification(@RequestBody ResendVerificationRequest request) {
        emailVerificationService.sendVerificationEmailFor(request.getEmail());
        return ResponseEntity.ok().build();
    }

}
