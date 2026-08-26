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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock
    private UserDao userDao;
    @Mock
    private UserTokenDao userTokenDao;
    @Mock
    private AccountEmailSender emailSender;

    private EmailVerificationService service;
    private User user;

    @BeforeEach
    void setUp() {
        service = new EmailVerificationService(userDao, userTokenDao, emailSender);
        user = new User();
        user.setEmail("jane@example.com");
        user.setEmailVerified(false);
    }

    private UserToken tokenFor(User user, LocalDateTime expiresAt, LocalDateTime usedAt) {
        UserToken token = new UserToken();
        token.setUser(user);
        token.setToken("the-token");
        token.setType(UserTokenType.EMAIL_VERIFICATION);
        token.setExpiresAt(expiresAt);
        token.setUsedAt(usedAt);
        return token;
    }

    @Test
    void sendVerificationEmailForCreatesATokenAndSendsAnEmailWhenUnverified() {
        when(userDao.getUserByEmail("jane@example.com")).thenReturn(Optional.of(user));
        when(userTokenDao.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.sendVerificationEmailFor("jane@example.com");

        ArgumentCaptor<UserToken> captor = ArgumentCaptor.forClass(UserToken.class);
        verify(userTokenDao).save(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(UserTokenType.EMAIL_VERIFICATION);
        verify(emailSender).sendVerificationEmail("jane@example.com", captor.getValue().getToken());
    }

    @Test
    void sendVerificationEmailForDoesNothingWhenTheAccountIsAlreadyVerified() {
        user.setEmailVerified(true);
        when(userDao.getUserByEmail("jane@example.com")).thenReturn(Optional.of(user));

        service.sendVerificationEmailFor("jane@example.com");

        verify(userTokenDao, never()).save(any());
        verify(emailSender, never()).sendVerificationEmail(any(), any());
    }

    @Test
    void sendVerificationEmailForDoesNothingWhenTheEmailDoesNotExist() {
        when(userDao.getUserByEmail("unknown@example.com")).thenReturn(Optional.empty());

        service.sendVerificationEmailFor("unknown@example.com");

        verify(userTokenDao, never()).save(any());
    }

    @Test
    void verifyEmailMarksTheUserVerifiedAndConsumesTheToken() {
        UserToken token = tokenFor(user, LocalDateTime.now().plusHours(1), null);
        when(userTokenDao.getByTokenAndType("the-token", UserTokenType.EMAIL_VERIFICATION)).thenReturn(Optional.of(token));

        service.verifyEmail("the-token");

        assertThat(user.isEmailVerified()).isTrue();
        verify(userDao).updateUser(user);
        assertThat(token.getUsedAt()).isNotNull();
        verify(userTokenDao).save(token);
    }

    @Test
    void verifyEmailRejectsAnUnknownToken() {
        when(userTokenDao.getByTokenAndType("bad-token", UserTokenType.EMAIL_VERIFICATION)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.verifyEmail("bad-token"))
                .isInstanceOf(InvalidUserTokenException.class);
    }

    @Test
    void verifyEmailRejectsAnAlreadyUsedToken() {
        UserToken token = tokenFor(user, LocalDateTime.now().plusHours(1), LocalDateTime.now().minusMinutes(5));
        when(userTokenDao.getByTokenAndType("the-token", UserTokenType.EMAIL_VERIFICATION)).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.verifyEmail("the-token"))
                .isInstanceOf(InvalidUserTokenException.class);

        verify(userDao, never()).updateUser(any());
    }

    @Test
    void verifyEmailRejectsAnExpiredToken() {
        UserToken token = tokenFor(user, LocalDateTime.now().minusMinutes(1), null);
        when(userTokenDao.getByTokenAndType("the-token", UserTokenType.EMAIL_VERIFICATION)).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.verifyEmail("the-token"))
                .isInstanceOf(InvalidUserTokenException.class);

        verify(userDao, never()).updateUser(any());
    }
}
