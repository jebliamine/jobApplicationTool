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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private UserDao userDao;
    @Mock
    private JobDao jobDao;
    @Mock
    private CompanyDao companyDao;
    @Mock
    private CVDao cvDao;
    @Mock
    private ApplicationDao applicationDao;
    @Mock
    private CoverLetterDao coverLetterDao;
    @Mock
    private GenerationRequestDao generationRequestDao;
    @Mock
    private PasswordEncoder passwordEncoder;

    private AdminUserService service;

    private User admin;
    private User regularUser;

    @BeforeEach
    void setUp() {
        service = new AdminUserService(
                userDao, jobDao, companyDao, cvDao, applicationDao, coverLetterDao, generationRequestDao, passwordEncoder);

        admin = new User();
        admin.setId(UUID.randomUUID());
        admin.setRole(UserRole.ADMIN);
        admin.setEmail("admin@example.com");

        regularUser = new User();
        regularUser.setId(UUID.randomUUID());
        regularUser.setRole(UserRole.USER);
        regularUser.setEmail("user@example.com");

        lenient().when(userDao.updateUser(any())).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(userDao.registerUser(any())).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(companyDao.getAllCompaniesByOwner(any())).thenReturn(List.of());
        lenient().when(coverLetterDao.getAllCoverLettersByOwner(any(), eq(false))).thenReturn(List.of());
        lenient().when(coverLetterDao.getAllCoverLettersByOwner(any(), eq(true))).thenReturn(List.of());
    }

    @Test
    void listUsersRejectsANonAdminRequester() {
        assertThatThrownBy(() -> service.listUsers(regularUser)).isInstanceOf(UserAccessDeniedException.class);
    }

    @Test
    void listUsersReturnsEveryUserForAnAdmin() {
        when(userDao.getAllUsers()).thenReturn(List.of(admin, regularUser));

        List<AdminUserResponse> result = service.listUsers(admin);

        assertThat(result).hasSize(2).extracting(AdminUserResponse::getEmail)
                .containsExactlyInAnyOrder("admin@example.com", "user@example.com");
    }

    @Test
    void updateRolePromotesAUserToAdmin() {
        when(userDao.getUserById(regularUser.getId())).thenReturn(Optional.of(regularUser));
        UpdateUserRoleRequest request = new UpdateUserRoleRequest();
        request.setRole(UserRole.ADMIN);

        AdminUserResponse result = service.updateRole(regularUser.getId(), request, admin);

        assertThat(result.getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void updateRoleRejectsRemovingYourOwnAdminRole() {
        UpdateUserRoleRequest request = new UpdateUserRoleRequest();
        request.setRole(UserRole.USER);

        assertThatThrownBy(() -> service.updateRole(admin.getId(), request, admin))
                .isInstanceOf(UserValidationException.class);
        verify(userDao, never()).updateUser(any());
    }

    @Test
    void updateRoleRejectsANonAdminRequester() {
        UpdateUserRoleRequest request = new UpdateUserRoleRequest();
        request.setRole(UserRole.ADMIN);

        assertThatThrownBy(() -> service.updateRole(regularUser.getId(), request, regularUser))
                .isInstanceOf(UserAccessDeniedException.class);
    }

    @Test
    void updateRoleRejectsAMissingRole() {
        UpdateUserRoleRequest request = new UpdateUserRoleRequest();

        assertThatThrownBy(() -> service.updateRole(regularUser.getId(), request, admin))
                .isInstanceOf(UserValidationException.class);
    }

    @Test
    void updateRoleRejectsAnUnknownUser() {
        UUID unknownId = UUID.randomUUID();
        when(userDao.getUserById(unknownId)).thenReturn(Optional.empty());
        UpdateUserRoleRequest request = new UpdateUserRoleRequest();
        request.setRole(UserRole.ADMIN);

        assertThatThrownBy(() -> service.updateRole(unknownId, request, admin))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void updateEnabledDisablesAUser() {
        when(userDao.getUserById(regularUser.getId())).thenReturn(Optional.of(regularUser));
        UpdateUserEnabledRequest request = new UpdateUserEnabledRequest();
        request.setEnabled(false);

        AdminUserResponse result = service.updateEnabled(regularUser.getId(), request, admin);

        assertThat(result.isEnabled()).isFalse();
    }

    @Test
    void updateEnabledRejectsDisablingYourOwnAccount() {
        UpdateUserEnabledRequest request = new UpdateUserEnabledRequest();
        request.setEnabled(false);

        assertThatThrownBy(() -> service.updateEnabled(admin.getId(), request, admin))
                .isInstanceOf(UserValidationException.class);
        verify(userDao, never()).updateUser(any());
    }

    @Test
    void updateEnabledRejectsANonAdminRequester() {
        UpdateUserEnabledRequest request = new UpdateUserEnabledRequest();
        request.setEnabled(false);

        assertThatThrownBy(() -> service.updateEnabled(regularUser.getId(), request, regularUser))
                .isInstanceOf(UserAccessDeniedException.class);
    }

    @Test
    void createUserRejectsANonAdminRequester() {
        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setEmail("new@example.com");
        request.setPassword("password123");

        assertThatThrownBy(() -> service.createUser(request, regularUser))
                .isInstanceOf(UserAccessDeniedException.class);
    }

    @Test
    void createUserRejectsAMissingEmail() {
        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setPassword("password123");

        assertThatThrownBy(() -> service.createUser(request, admin)).isInstanceOf(UserValidationException.class);
    }

    @Test
    void createUserRejectsAShortPassword() {
        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setEmail("new@example.com");
        request.setPassword("short");

        assertThatThrownBy(() -> service.createUser(request, admin)).isInstanceOf(UserValidationException.class);
    }

    @Test
    void createUserRejectsADuplicateEmail() {
        when(userDao.getUserByEmail("user@example.com")).thenReturn(Optional.of(regularUser));
        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setEmail("user@example.com");
        request.setPassword("password123");

        assertThatThrownBy(() -> service.createUser(request, admin)).isInstanceOf(UserValidationException.class);
    }

    @Test
    void createUserDefaultsToUserRoleAndEncodesThePassword() {
        when(userDao.getUserByEmail("new@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setFullName("New Person");
        request.setEmail("new@example.com");
        request.setPassword("password123");

        AdminUserResponse result = service.createUser(request, admin);

        assertThat(result.getRole()).isEqualTo(UserRole.USER);
        assertThat(result.isEnabled()).isTrue();
        assertThat(result.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    void createUserHonorsAnExplicitAdminRole() {
        when(userDao.getUserByEmail("new@example.com")).thenReturn(Optional.empty());
        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setEmail("new@example.com");
        request.setPassword("password123");
        request.setRole(UserRole.ADMIN);

        AdminUserResponse result = service.createUser(request, admin);

        assertThat(result.getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void deleteUserRejectsANonAdminRequester() {
        assertThatThrownBy(() -> service.deleteUser(regularUser.getId(), regularUser))
                .isInstanceOf(UserAccessDeniedException.class);
    }

    @Test
    void deleteUserRejectsDeletingYourOwnAccount() {
        assertThatThrownBy(() -> service.deleteUser(admin.getId(), admin))
                .isInstanceOf(UserValidationException.class);
        verify(userDao, never()).deleteUser(any());
    }

    @Test
    void deleteUserRejectsAnUnknownUser() {
        UUID unknownId = UUID.randomUUID();
        when(userDao.getUserById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteUser(unknownId, admin)).isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void deleteUserRemovesAUserWithNoOwnedData() {
        when(userDao.getUserById(regularUser.getId())).thenReturn(Optional.of(regularUser));

        service.deleteUser(regularUser.getId(), admin);

        verify(userDao).deleteUser(regularUser.getId());
    }

    @Test
    void deleteUserIsBlockedWhenTheUserOwnsJobs() {
        when(userDao.getUserById(regularUser.getId())).thenReturn(Optional.of(regularUser));
        when(jobDao.countByOwner(regularUser)).thenReturn(1L);

        assertThatThrownBy(() -> service.deleteUser(regularUser.getId(), admin))
                .isInstanceOf(UserValidationException.class);
        verify(userDao, never()).deleteUser(any());
    }

    @Test
    void deleteUserIsBlockedWhenTheUserOwnsCoverLetters() {
        when(userDao.getUserById(regularUser.getId())).thenReturn(Optional.of(regularUser));
        when(coverLetterDao.getAllCoverLettersByOwner(regularUser, false))
                .thenReturn(List.of(new de.jeb.japp.model.coverLetter.CoverLetter()));

        assertThatThrownBy(() -> service.deleteUser(regularUser.getId(), admin))
                .isInstanceOf(UserValidationException.class);
        verify(userDao, never()).deleteUser(any());
    }
}
