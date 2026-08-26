package de.jeb.japp.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccountEmailSenderTest {

    @Mock
    private JavaMailSender mailSender;

    private AccountEmailSender sender;

    @BeforeEach
    void setUp() {
        sender = new AccountEmailSender(mailSender, "http://localhost:4200");
    }

    @Test
    void passwordResetEmailIncludesATokenizedLinkToTheFrontend() {
        sender.sendPasswordResetEmail("jane@example.com", "abc123");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        assertThatMessage(captor.getValue(), "jane@example.com", "http://localhost:4200/reset-password?token=abc123");
    }

    @Test
    void verificationEmailIncludesATokenizedLinkToTheFrontend() {
        sender.sendVerificationEmail("jane@example.com", "xyz789");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        assertThatMessage(captor.getValue(), "jane@example.com", "http://localhost:4200/verify-email?token=xyz789");
    }

    @Test
    void aSendFailureIsSwallowedRatherThanPropagated() {
        doThrow(new MailSendException("no server")).when(mailSender).send(any(SimpleMailMessage.class));

        assertThatCode(() -> sender.sendPasswordResetEmail("jane@example.com", "abc123")).doesNotThrowAnyException();
    }

    private void assertThatMessage(SimpleMailMessage message, String expectedTo, String expectedLinkFragment) {
        org.assertj.core.api.Assertions.assertThat(message.getTo()).containsExactly(expectedTo);
        org.assertj.core.api.Assertions.assertThat(message.getText()).contains(expectedLinkFragment);
    }
}
