package de.jeb.japp.ai.service;

import de.jeb.japp.ai.service.encryption.AiCredentialEncryptor;
import de.jeb.japp.commons.exceptions.ai.AiProviderAccessDeniedException;
import de.jeb.japp.commons.exceptions.ai.AiProviderNotFoundException;
import de.jeb.japp.commons.exceptions.ai.AiProviderValidationException;
import de.jeb.japp.dao.ai.AiProviderConfigurationDao;
import de.jeb.japp.model.ai.AiProviderConfiguration;
import de.jeb.japp.model.ai.dto.AdminAiProviderResponse;
import de.jeb.japp.model.ai.dto.AiProviderTestResult;
import de.jeb.japp.model.ai.dto.AiProviderUpdateRequest;
import de.jeb.japp.model.generation.GenerationProvider;
import de.jeb.japp.model.user.User;
import de.jeb.japp.model.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAiProviderServiceTest {

    @Mock
    private AiProviderConfigurationDao dao;
    @Mock
    private AiCredentialEncryptor encryptor;
    @Mock
    private ProviderSettingsResolver resolver;
    @Mock
    private ProviderConnectionTester connectionTester;

    private AdminAiProviderService service;

    private User admin;
    private User regularUser;

    @BeforeEach
    void setUp() {
        service = new AdminAiProviderService(dao, encryptor, resolver, connectionTester);

        admin = new User();
        admin.setId(UUID.randomUUID());
        admin.setRole(UserRole.ADMIN);

        regularUser = new User();
        regularUser.setId(UUID.randomUUID());
        regularUser.setRole(UserRole.USER);
    }

    @Test
    void nonAdminCannotListProviders() {
        assertThatThrownBy(() -> service.listProviders(regularUser))
                .isInstanceOf(AiProviderAccessDeniedException.class);
    }

    @Test
    void nonAdminCannotUpdateProviders() {
        assertThatThrownBy(() -> service.updateProvider("GEMINI", new AiProviderUpdateRequest(), regularUser))
                .isInstanceOf(AiProviderAccessDeniedException.class);
        verifyNoInteractions(dao);
    }

    @Test
    void nonAdminCannotTestConnection() {
        assertThatThrownBy(() -> service.testConnection("GEMINI", regularUser))
                .isInstanceOf(AiProviderAccessDeniedException.class);
        verifyNoInteractions(connectionTester);
    }

    @Test
    void adminCanListAllKnownProviders() {
        when(dao.getByProvider(any())).thenReturn(Optional.empty());

        List<AdminAiProviderResponse> result = service.listProviders(admin);

        assertThat(result).hasSize(GenerationProvider.values().length);
    }

    @Test
    void updateRejectsUnknownProvider() {
        assertThatThrownBy(() -> service.updateProvider("NOT_A_PROVIDER", new AiProviderUpdateRequest(), admin))
                .isInstanceOf(AiProviderNotFoundException.class);
    }

    @Test
    void updateEnablesAndSetsModelAndBaseUrl() {
        AiProviderConfiguration existing = new AiProviderConfiguration();
        existing.setProvider("GEMINI");
        existing.setEnabled(false);
        when(dao.getByProvider("GEMINI")).thenReturn(Optional.of(existing));
        when(dao.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AiProviderUpdateRequest request = new AiProviderUpdateRequest();
        request.setEnabled(true);
        request.setDefaultModel("gemini-2.5-flash");
        request.setBaseUrl("https://custom.example");

        AdminAiProviderResponse response = service.updateProvider("GEMINI", request, admin);

        assertThat(response.isEnabled()).isTrue();
        assertThat(response.getDefaultModel()).isEqualTo("gemini-2.5-flash");
        assertThat(response.getBaseUrl()).isEqualTo("https://custom.example");
        verify(resolver).invalidate(GenerationProvider.GEMINI);
    }

    @Test
    void updateSetsUpdatedByToTheAdminRequester() {
        AiProviderConfiguration existing = new AiProviderConfiguration();
        existing.setProvider("GEMINI");
        when(dao.getByProvider("GEMINI")).thenReturn(Optional.of(existing));
        ArgumentCaptor<AiProviderConfiguration> captor = ArgumentCaptor.forClass(AiProviderConfiguration.class);
        when(dao.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        service.updateProvider("GEMINI", new AiProviderUpdateRequest(), admin);

        assertThat(captor.getValue().getUpdatedBy()).isEqualTo(admin);
        assertThat(captor.getValue().getUpdatedAt()).isNotNull();
    }

    @Test
    void updateStoresEncryptedApiKeyWhenProvided() {
        AiProviderConfiguration existing = new AiProviderConfiguration();
        existing.setProvider("GEMINI");
        when(dao.getByProvider("GEMINI")).thenReturn(Optional.of(existing));
        when(dao.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(encryptor.isAvailable()).thenReturn(true);
        when(encryptor.encrypt("real-api-key")).thenReturn("ciphertext");

        AiProviderUpdateRequest request = new AiProviderUpdateRequest();
        request.setApiKey("real-api-key");

        AdminAiProviderResponse response = service.updateProvider("GEMINI", request, admin);

        assertThat(response.isHasApiKey()).isTrue();
        verify(encryptor).encrypt("real-api-key");
    }

    @Test
    void updateRejectsApiKeyWhenEncryptionIsUnavailable() {
        AiProviderConfiguration existing = new AiProviderConfiguration();
        existing.setProvider("GEMINI");
        when(dao.getByProvider("GEMINI")).thenReturn(Optional.of(existing));
        when(encryptor.isAvailable()).thenReturn(false);

        AiProviderUpdateRequest request = new AiProviderUpdateRequest();
        request.setApiKey("real-api-key");

        assertThatThrownBy(() -> service.updateProvider("GEMINI", request, admin))
                .isInstanceOf(AiProviderValidationException.class);
        verify(dao, never()).save(any());
    }

    @Test
    void omittingApiKeyLeavesExistingKeyUnchanged() {
        AiProviderConfiguration existing = new AiProviderConfiguration();
        existing.setProvider("GEMINI");
        existing.setEncryptedApiKey("existing-ciphertext");
        when(dao.getByProvider("GEMINI")).thenReturn(Optional.of(existing));
        when(dao.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AiProviderUpdateRequest request = new AiProviderUpdateRequest();
        request.setEnabled(true);

        AdminAiProviderResponse response = service.updateProvider("GEMINI", request, admin);

        assertThat(response.isHasApiKey()).isTrue();
        verifyNoInteractions(encryptor);
    }

    @Test
    void clearApiKeyRemovesTheStoredKey() {
        AiProviderConfiguration existing = new AiProviderConfiguration();
        existing.setProvider("GEMINI");
        existing.setEncryptedApiKey("existing-ciphertext");
        when(dao.getByProvider("GEMINI")).thenReturn(Optional.of(existing));
        ArgumentCaptor<AiProviderConfiguration> captor = ArgumentCaptor.forClass(AiProviderConfiguration.class);
        when(dao.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        AiProviderUpdateRequest request = new AiProviderUpdateRequest();
        request.setClearApiKey(true);

        AdminAiProviderResponse response = service.updateProvider("GEMINI", request, admin);

        assertThat(response.isHasApiKey()).isFalse();
        assertThat(captor.getValue().getEncryptedApiKey()).isNull();
        verify(resolver).invalidate(GenerationProvider.GEMINI);
    }

    @Test
    void testConnectionDelegatesToConnectionTester() {
        when(connectionTester.test(GenerationProvider.GEMINI))
                .thenReturn(new AiProviderTestResult(true, "Connection successful."));

        AiProviderTestResult result = service.testConnection("GEMINI", admin);

        assertThat(result.isSuccess()).isTrue();
        verify(connectionTester).test(GenerationProvider.GEMINI);
    }

    @Test
    void testConnectionRejectsUnknownProvider() {
        assertThatThrownBy(() -> service.testConnection("NOT_A_PROVIDER", admin))
                .isInstanceOf(AiProviderNotFoundException.class);
        verifyNoInteractions(connectionTester);
    }
}
