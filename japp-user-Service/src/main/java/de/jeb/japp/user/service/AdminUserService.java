package de.jeb.japp.user.service;

import de.jeb.japp.commons.exceptions.user.UserAccessDeniedException;
import de.jeb.japp.commons.exceptions.user.UserNotFoundException;
import de.jeb.japp.commons.exceptions.user.UserValidationException;
import de.jeb.japp.dao.application.ApplicationDao;
import de.jeb.japp.dao.company.CompanyDao;
import de.jeb.japp.dao.coverletter.CoverLetterDao;
import de.jeb.japp.dao.cv.CVDao;
import de.jeb.japp.dao.generation.GenerationRequestDao;
import de.jeb.japp.dao.job.JobDao;
import de.jeb.japp.dao.user.UserDao;
import de.jeb.japp.model.user.User;
import de.jeb.japp.model.user.UserRole;
import de.jeb.japp.model.user.dto.AdminCreateUserRequest;
import de.jeb.japp.model.user.dto.AdminUserResponse;
import de.jeb.japp.model.user.dto.UpdateUserEnabledRequest;
import de.jeb.japp.model.user.dto.UpdateUserRoleRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Admin-only user management: list every user, create, delete, change role,
 * enable/disable. Authorization follows the existing project-wide convention
 * — a manual requester.getRole() == ADMIN check in the service layer (see
 * AdminAiProviderService), never Spring Method Security.
 * <p>
 * Deliberately does not support editing name/email/password for another
 * user (that stays self-service only, via UserController). Deletion is
 * blocked (not cascaded) for any user who owns data — see deleteUser().
 */
@Service
public class AdminUserService {

    private static final int MIN_PASSWORD_LENGTH = 8;

    private final UserDao userDao;
    private final JobDao jobDao;
    private final CompanyDao companyDao;
    private final CVDao cvDao;
    private final ApplicationDao applicationDao;
    private final CoverLetterDao coverLetterDao;
    private final GenerationRequestDao generationRequestDao;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(
            UserDao userDao,
            JobDao jobDao,
            CompanyDao companyDao,
            CVDao cvDao,
            ApplicationDao applicationDao,
            CoverLetterDao coverLetterDao,
            GenerationRequestDao generationRequestDao,
            PasswordEncoder passwordEncoder
    ) {
        this.userDao = userDao;
        this.jobDao = jobDao;
        this.companyDao = companyDao;
        this.cvDao = cvDao;
        this.applicationDao = applicationDao;
        this.coverLetterDao = coverLetterDao;
        this.generationRequestDao = generationRequestDao;
        this.passwordEncoder = passwordEncoder;
    }

    public List<AdminUserResponse> listUsers(User requester) {
        assertAdmin(requester);
        return userDao.getAllUsers().stream().map(AdminUserResponse::from).toList();
    }

    public AdminUserResponse createUser(AdminCreateUserRequest request, User requester) {
        assertAdmin(requester);

        String email = request.getEmail() == null ? "" : request.getEmail().trim();
        String password = request.getPassword() == null ? "" : request.getPassword();
        if (email.isEmpty()) {
            throw new UserValidationException("An email is required.");
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new UserValidationException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters.");
        }
        if (userDao.getUserByEmail(email).isPresent()) {
            throw new UserValidationException("A user with this email already exists.");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(request.getRole() != null ? request.getRole() : UserRole.USER);
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());

        return AdminUserResponse.from(userDao.registerUser(user));
    }

    /**
     * Deletion is blocked (never cascaded) for a user who owns any data —
     * most owner FKs (job, company, cv, application, cover letter,
     * generation request) have no ON DELETE behavior, so a naive delete
     * would surface as a raw FK-violation 500 anyway; this turns it into a
     * clear, actionable error instead. Disable the account when the data
     * needs to stay.
     */
    public void deleteUser(UUID id, User requester) {
        assertAdmin(requester);
        if (id.equals(requester.getId())) {
            throw new UserValidationException("You cannot delete your own account.");
        }

        User user = find(id);
        if (hasOwnedData(user)) {
            throw new UserValidationException(
                    "This user has existing jobs, applications, or other data. Disable the account instead of deleting it.");
        }

        userDao.deleteUser(id);
    }

    private boolean hasOwnedData(User user) {
        return jobDao.countByOwner(user) > 0
                || !companyDao.getAllCompaniesByOwner(user).isEmpty()
                || cvDao.countByOwner(user) > 0
                || applicationDao.countByOwner(user) > 0
                || !coverLetterDao.getAllCoverLettersByOwner(user, false).isEmpty()
                || !coverLetterDao.getAllCoverLettersByOwner(user, true).isEmpty()
                || generationRequestDao.countByOwner(user) > 0;
    }

    public AdminUserResponse updateRole(UUID id, UpdateUserRoleRequest request, User requester) {
        assertAdmin(requester);
        if (request.getRole() == null) {
            throw new UserValidationException("A role is required.");
        }
        if (id.equals(requester.getId()) && request.getRole() != UserRole.ADMIN) {
            throw new UserValidationException("You cannot remove your own admin role.");
        }

        User user = find(id);
        user.setRole(request.getRole());
        return AdminUserResponse.from(userDao.updateUser(user));
    }

    public AdminUserResponse updateEnabled(UUID id, UpdateUserEnabledRequest request, User requester) {
        assertAdmin(requester);
        if (id.equals(requester.getId()) && !request.isEnabled()) {
            throw new UserValidationException("You cannot disable your own account.");
        }

        User user = find(id);
        user.setEnabled(request.isEnabled());
        return AdminUserResponse.from(userDao.updateUser(user));
    }

    private User find(UUID id) {
        return userDao.getUserById(id).orElseThrow(() -> new UserNotFoundException("User not found."));
    }

    private void assertAdmin(User requester) {
        if (requester.getRole() != UserRole.ADMIN) {
            throw new UserAccessDeniedException("Only an administrator can manage users.");
        }
    }
}
