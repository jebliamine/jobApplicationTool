package de.jeb.japp.user.service;

import de.jeb.japp.commons.exceptions.user.InvalidUserTokenException;
import de.jeb.japp.dao.user.UserDao;
import de.jeb.japp.dao.user.UserTokenDao;
import de.jeb.japp.model.user.User;
import de.jeb.japp.model.user.UserToken;
import de.jeb.japp.model.user.UserTokenType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UserDao userDao;
    @Mock
    private UserTokenDao userTokenDao;
    @Mock
    private PasswordEncoder encoder;
    @Mock
    private AccountEmailSender emailSender;

    private PasswordResetService service;
    private User user;

    @BeforeEach
    void setUp() {
        service = new PasswordResetService(userDao, userTokenDao, encoder, emailSender);
        user = new User();
        user.setEmail("jane@example.com");
        lenient().when(userTokenDao.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private UserToken tokenFor(User user, UserTokenType type, LocalDateTime expiresAt, LocalDateTime usedAt) {
        UserToken token = new UserToken();
        token.setUser(user);
        token.setToken("the-token");
        token.setType(type);
        token.setExpiresAt(expiresAt);
        token.setUsedAt(usedAt);
        return token;
    }

    @Test
    void requestResetCreatesATokenAndSendsAnEmailWhenTheAccountExists() {
        when(userDao.getUserByEmail("jane@example.com")).thenReturn(Optional.of(user));

        service.requestReset("jane@example.com");

        ArgumentCaptor<UserToken> captor = ArgumentCaptor.forClass(UserToken.class);
        verify(userTokenDao).save(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(UserTokenType.PASSWORD_RESET);
        assertThat(captor.getValue().getUser()).isEqualTo(user);
        assertThat(captor.getValue().getExpiresAt()).isAfter(LocalDateTime.now());
        verify(emailSender).sendPasswordResetEmail("jane@example.com", captor.getValue().getToken());
    }

    @Test
    void requestResetDoesNothingObservableWhenTheEmailDoesNotExist() {
        when(userDao.getUserByEmail("unknown@example.com")).thenReturn(Optional.empty());

        service.requestReset("unknown@example.com");

        verify(userTokenDao, never()).save(any());
        verify(emailSender, never()).sendPasswordResetEmail(any(), any());
    }

    @Test
    void requestResetToleratesANullEmailRatherThanCrashing() {
        service.requestReset(null);

        verify(userTokenDao, never()).save(any());
    }

    @Test
    void resetPasswordUpdatesTheHashAndMarksTheTokenUsed() {
        UserToken token = tokenFor(user, UserTokenType.PASSWORD_RESET, LocalDateTime.now().plusMinutes(30), null);
        when(userTokenDao.getByTokenAndType("the-token", UserTokenType.PASSWORD_RESET)).thenReturn(Optional.of(token));
        when(encoder.encode("new-password-123")).thenReturn("new-hash");

        service.resetPassword("the-token", "new-password-123");

        assertThat(user.getPasswordHash()).isEqualTo("new-hash");
        verify(userDao).updateUser(user);
        assertThat(token.getUsedAt()).isNotNull();
        verify(userTokenDao).save(token);
    }

    @Test
    void resetPasswordRejectsAnUnknownToken() {
        when(userTokenDao.getByTokenAndType("bad-token", UserTokenType.PASSWORD_RESET)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resetPassword("bad-token", "new-password-123"))
                .isInstanceOf(InvalidUserTokenException.class);

        verify(userDao, never()).updateUser(any());
    }

    @Test
    void resetPasswordRejectsAnAlreadyUsedToken() {
        UserToken token = tokenFor(user, UserTokenType.PASSWORD_RESET, LocalDateTime.now().plusMinutes(30), LocalDateTime.now().minusMinutes(5));
        when(userTokenDao.getByTokenAndType("the-token", UserTokenType.PASSWORD_RESET)).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.resetPassword("the-token", "new-password-123"))
                .isInstanceOf(InvalidUserTokenException.class);

        verify(userDao, never()).updateUser(any());
    }

    @Test
    void resetPasswordRejectsAnExpiredToken() {
        UserToken token = tokenFor(user, UserTokenType.PASSWORD_RESET, LocalDateTime.now().minusMinutes(1), null);
        when(userTokenDao.getByTokenAndType("the-token", UserTokenType.PASSWORD_RESET)).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.resetPassword("the-token", "new-password-123"))
                .isInstanceOf(InvalidUserTokenException.class);

        verify(userDao, never()).updateUser(any());
    }

    @Test
    void resetPasswordRejectsAPasswordShorterThanEightCharacters() {
        UserToken token = tokenFor(user, UserTokenType.PASSWORD_RESET, LocalDateTime.now().plusMinutes(30), null);
        when(userTokenDao.getByTokenAndType("the-token", UserTokenType.PASSWORD_RESET)).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.resetPassword("the-token", "short1"))
                .isInstanceOf(InvalidUserTokenException.class);

        verify(userDao, never()).updateUser(any());
    }
}
