package de.jeb.japp.user.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Sends the plain-text password-reset and email-verification emails. Delivery failures (no SMTP
 * server configured/reachable — the local-dev default) are logged and swallowed rather than
 * thrown: the caller must never fail a request just because outbound mail is unavailable, same
 * "still works, only the dependent feature is degraded" convention as AI_CREDENTIALS_ENCRYPTION_KEY
 * being unset. Callers (PasswordResetService/EmailVerificationService) already avoid leaking
 * whether an email address exists, so a swallowed send failure changes nothing observable there.
 */
@Component
public class AccountEmailSender {

    private static final Logger log = LoggerFactory.getLogger(AccountEmailSender.class);

    private final JavaMailSender mailSender;
    private final String frontendUrl;

    public AccountEmailSender(JavaMailSender mailSender, @Value("${app.frontend-url}") String frontendUrl) {
        this.mailSender = mailSender;
        this.frontendUrl = frontendUrl;
    }

    public void sendPasswordResetEmail(String toEmail, String token) {
        String link = frontendUrl + "/reset-password?token=" + token;
        send(toEmail, "Reset your JAPP password",
                "We received a request to reset your JAPP password.\n\n"
                        + "Reset it here: " + link + "\n\n"
                        + "This link expires in 1 hour. If you didn't request this, you can ignore this email.");
    }

    public void sendVerificationEmail(String toEmail, String token) {
        String link = frontendUrl + "/verify-email?token=" + token;
        send(toEmail, "Verify your JAPP email address",
                "Welcome to JAPP! Please verify your email address to finish setting up your account.\n\n"
                        + "Verify it here: " + link + "\n\n"
                        + "This link expires in 24 hours.");
    }

    private void send(String toEmail, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
        } catch (MailException e) {
            log.warn("Could not send account email (subject=\"{}\"): {}", subject, e.getMessage());
        }
    }
}
