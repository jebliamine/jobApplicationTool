package de.jeb.japp.user.service;

import de.jeb.japp.commons.exceptions.user.InvalidPasswordChangeException;
import de.jeb.japp.dao.user.UserDao;
import de.jeb.japp.model.user.User;
import de.jeb.japp.model.user.dto.ChangePasswordRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserPasswordServiceTest {

    @Mock
    private UserDao userDao;
    @Mock
    private PasswordEncoder encoder;

    private UserPasswordService userPasswordService;
    private User user;

    @BeforeEach
    void setUp() {
        userPasswordService = new UserPasswordService(userDao, encoder);
        user = new User();
        user.setPasswordHash("old-hash");
    }

    @Test
    void changesPasswordWhenCurrentPasswordMatchesAndNewPasswordIsLongEnough() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("correct-current");
        request.setNewPassword("new-password-123");

        when(encoder.matches("correct-current", "old-hash")).thenReturn(true);
        when(encoder.encode("new-password-123")).thenReturn("new-hash");

        userPasswordService.changePassword(user, request);

        assertThat(user.getPasswordHash()).isEqualTo("new-hash");
        verify(userDao).updateUser(user);
    }

    @Test
    void rejectsIncorrectCurrentPassword() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("wrong-current");
        request.setNewPassword("new-password-123");

        when(encoder.matches("wrong-current", "old-hash")).thenReturn(false);

        assertThatThrownBy(() -> userPasswordService.changePassword(user, request))
                .isInstanceOf(InvalidPasswordChangeException.class);

        verify(userDao, never()).updateUser(user);
    }

    @Test
    void rejectsNewPasswordShorterThanEightCharacters() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("correct-current");
        request.setNewPassword("short1");

        when(encoder.matches("correct-current", "old-hash")).thenReturn(true);

        assertThatThrownBy(() -> userPasswordService.changePassword(user, request))
                .isInstanceOf(InvalidPasswordChangeException.class);

        verify(userDao, never()).updateUser(user);
    }
}
