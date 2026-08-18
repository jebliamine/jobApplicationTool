package de.jeb.japp.rest.admin;

import de.jeb.japp.dao.user.UserDao;
import de.jeb.japp.model.user.User;
import de.jeb.japp.model.user.dto.UpdateUserRequest;
import de.jeb.japp.model.user.dto.UserDto;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * Business logic behind PUT /api/v1/users/me. Kept in japp-rest (rather than
 * the unwired japp-user-Service module — see JappApplication's
 * scanBasePackages, which only covers de.jeb.japp) so it's actually reachable.
 */
@Service
public class UserProfileService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final UserDao userDao;

    public UserProfileService(UserDao userDao) {
        this.userDao = userDao;
    }

    public UserDto updateProfile(User currentUser, UpdateUserRequest request) {
        String fullName = request.getFullName() == null ? "" : request.getFullName().trim();
        String email = request.getEmail() == null ? "" : request.getEmail().trim();

        if (fullName.isEmpty()) {
            throw new InvalidProfileUpdateException("Full name is required.");
        }
        if (email.isEmpty() || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidProfileUpdateException("A valid email is required.");
        }

        if (!email.equals(currentUser.getEmail())) {
            userDao.getUserByEmail(email)
                    .filter(existing -> !existing.getId().equals(currentUser.getId()))
                    .ifPresent(existing -> {
                        throw new DuplicateEmailException("Email already in use.");
                    });
        }

        currentUser.setFullName(fullName);
        currentUser.setEmail(email);

        User saved = userDao.updateUser(currentUser);
        return UserDto.from(saved);
    }
}
