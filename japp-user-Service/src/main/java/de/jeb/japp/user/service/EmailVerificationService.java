package de.jeb.japp.user.service;

import de.jeb.japp.commons.exceptions.user.InvalidUserTokenException;
import de.jeb.japp.dao.user.UserDao;
import de.jeb.japp.dao.user.UserTokenDao;
import de.jeb.japp.model.user.User;
import de.jeb.japp.model.user.UserToken;
import de.jeb.japp.model.user.UserTokenType;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Backs new-account email verification: an initial send right after self-registration, plus an
 * explicit "resend" endpoint. {@link #sendVerificationEmailFor} never reveals whether the given
 * email exists or is already verified — same account-enumeration rationale as
 * {@link PasswordResetService#requestReset}.
 */
@Service
public class EmailVerificationService {

    private static final long TOKEN_VALID_HOURS = 24;

    private final UserDao userDao;
    private final UserTokenDao userTokenDao;
    private final AccountEmailSender emailSender;

    public EmailVerificationService(UserDao userDao, UserTokenDao userTokenDao, AccountEmailSender emailSender) {
        this.userDao = userDao;
        this.userTokenDao = userTokenDao;
        this.emailSender = emailSender;
    }

    /** Always "succeeds" from the caller's point of view, whether or not the email exists or is already verified. */
    public void sendVerificationEmailFor(String email) {
        Optional<User> user = email != null ? userDao.getUserByEmail(email) : Optional.empty();
        if (user.isEmpty() || user.get().isEmailVerified()) {
            return;
        }

        UserToken token = new UserToken();
        token.setUser(user.get());
        token.setToken(UUID.randomUUID().toString());
        token.setType(UserTokenType.EMAIL_VERIFICATION);
        token.setExpiresAt(LocalDateTime.now().plusHours(TOKEN_VALID_HOURS));
        token.setCreatedAt(LocalDateTime.now());
        userTokenDao.save(token);

        emailSender.sendVerificationEmail(user.get().getEmail(), token.getToken());
    }

    public void verifyEmail(String rawToken) {
        UserToken token = userTokenDao.getByTokenAndType(rawToken, UserTokenType.EMAIL_VERIFICATION)
                .orElseThrow(() -> new InvalidUserTokenException("This verification link is invalid or has already been used."));
        if (token.isUsed()) {
            throw new InvalidUserTokenException("This verification link is invalid or has already been used.");
        }
        if (token.isExpired()) {
            throw new InvalidUserTokenException("This verification link has expired. Please request a new one.");
        }

        User user = token.getUser();
        user.setEmailVerified(true);
        userDao.updateUser(user);

        token.setUsedAt(LocalDateTime.now());
        userTokenDao.save(token);
    }
}
