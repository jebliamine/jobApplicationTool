package de.jeb.japp.user.service;

import de.jeb.japp.commons.exceptions.user.InvalidUserTokenException;
import de.jeb.japp.dao.user.UserDao;
import de.jeb.japp.dao.user.UserTokenDao;
import de.jeb.japp.model.user.User;
import de.jeb.japp.model.user.UserToken;
import de.jeb.japp.model.user.UserTokenType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Backs the "forgot your password" flow. {@link #requestReset} never reveals whether the given
 * email actually belongs to an account — it always completes the same way from the caller's
 * perspective — to avoid turning this endpoint into an account-enumeration oracle.
 */
@Service
public class PasswordResetService {

    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final long TOKEN_VALID_HOURS = 1;

    private final UserDao userDao;
    private final UserTokenDao userTokenDao;
    private final PasswordEncoder encoder;
    private final AccountEmailSender emailSender;

    public PasswordResetService(
            UserDao userDao,
            UserTokenDao userTokenDao,
            PasswordEncoder encoder,
            AccountEmailSender emailSender
    ) {
        this.userDao = userDao;
        this.userTokenDao = userTokenDao;
        this.encoder = encoder;
        this.emailSender = emailSender;
    }

    /** Always "succeeds" from the caller's point of view, whether or not the email exists. */
    public void requestReset(String email) {
        Optional<User> user = email != null ? userDao.getUserByEmail(email) : Optional.empty();
        if (user.isEmpty()) {
            return;
        }

        UserToken token = new UserToken();
        token.setUser(user.get());
        token.setToken(UUID.randomUUID().toString());
        token.setType(UserTokenType.PASSWORD_RESET);
        token.setExpiresAt(LocalDateTime.now().plusHours(TOKEN_VALID_HOURS));
        token.setCreatedAt(LocalDateTime.now());
        userTokenDao.save(token);

        emailSender.sendPasswordResetEmail(user.get().getEmail(), token.getToken());
    }

    public void resetPassword(String rawToken, String newPassword) {
        UserToken token = userTokenDao.getByTokenAndType(rawToken, UserTokenType.PASSWORD_RESET)
                .orElseThrow(() -> new InvalidUserTokenException("This reset link is invalid or has already been used."));
        if (token.isUsed()) {
            throw new InvalidUserTokenException("This reset link is invalid or has already been used.");
        }
        if (token.isExpired()) {
            throw new InvalidUserTokenException("This reset link has expired. Please request a new one.");
        }
        if (newPassword == null || newPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new InvalidUserTokenException("New password must be at least " + MIN_PASSWORD_LENGTH + " characters.");
        }

        User user = token.getUser();
        user.setPasswordHash(encoder.encode(newPassword));
        userDao.updateUser(user);

        token.setUsedAt(LocalDateTime.now());
        userTokenDao.save(token);
    }
}
