package de.jeb.japp.user.service;

import de.jeb.japp.commons.exceptions.user.InvalidPasswordChangeException;
import de.jeb.japp.dao.user.UserDao;
import de.jeb.japp.model.user.User;
import de.jeb.japp.model.user.dto.ChangePasswordRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/** Business logic behind PUT /api/v1/users/me/password. */
@Service
public class UserPasswordService {

    private static final int MIN_PASSWORD_LENGTH = 8;

    private final UserDao userDao;
    private final PasswordEncoder encoder;

    public UserPasswordService(UserDao userDao, PasswordEncoder encoder) {
        this.userDao = userDao;
        this.encoder = encoder;
    }

    public void changePassword(User currentUser, ChangePasswordRequest request) {
        String currentPassword = request.getCurrentPassword() == null ? "" : request.getCurrentPassword();
        String newPassword = request.getNewPassword() == null ? "" : request.getNewPassword();

        if (!encoder.matches(currentPassword, currentUser.getPasswordHash())) {
            throw new InvalidPasswordChangeException("Current password is incorrect.");
        }
        if (newPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new InvalidPasswordChangeException(
                    "New password must be at least " + MIN_PASSWORD_LENGTH + " characters.");
        }

        currentUser.setPasswordHash(encoder.encode(newPassword));
        userDao.updateUser(currentUser);
    }
}
