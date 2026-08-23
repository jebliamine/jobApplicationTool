package de.jeb.japp.ai.service;

import de.jeb.japp.ai.service.encryption.AiCredentialEncryptor;
import de.jeb.japp.commons.exceptions.ai.AiProviderAccessDeniedException;
import de.jeb.japp.commons.exceptions.ai.AiProviderNotFoundException;
import de.jeb.japp.commons.exceptions.ai.AiProviderValidationException;
import de.jeb.japp.dao.ai.AiProviderConfigurationDao;
import de.jeb.japp.model.ai.AdapterType;
import de.jeb.japp.model.ai.AiProviderConfiguration;
import de.jeb.japp.model.ai.dto.AdminAiProviderResponse;
import de.jeb.japp.model.ai.dto.AiProviderCreateRequest;
import de.jeb.japp.model.ai.dto.AiProviderTestResult;
import de.jeb.japp.model.ai.dto.AiProviderUpdateRequest;
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
    private UUID instanceId;

    @BeforeEach
    void setUp() {
        service = new AdminAiProviderService(dao, encryptor, resolver, connectionTester);

        admin = new User();
        admin.setId(UUID.randomUUID());
        admin.setRole(UserRole.ADMIN);

        regularUser = new User();
        regularUser.setId(UUID.randomUUID());
        regularUser.setRole(UserRole.USER);

        instanceId = UUID.randomUUID();
    }

    private AiProviderConfiguration existingGeminiInstance() {
        AiProviderConfiguration config = new AiProviderConfiguration();
        config.setAdapterType(AdapterType.GEMINI_GENERATE_CONTENT.name());
        config.setDisplayName("Google Gemini");
        return config;
    }

    @Test
    void nonAdminCannotListProviders() {
        assertThatThrownBy(() -> service.listProviders(regularUser))
                .isInstanceOf(AiProviderAccessDeniedException.class);
    }

    @Test
    void nonAdminCannotCreateProviders() {
        assertThatThrownBy(() -> service.createProvider(new AiProviderCreateRequest(), regularUser))
                .isInstanceOf(AiProviderAccessDeniedException.class);
        verifyNoInteractions(dao);
    }

    @Test
    void nonAdminCannotUpdateProviders() {
        assertThatThrownBy(() -> service.updateProvider(instanceId, new AiProviderUpdateRequest(), regularUser))
                .isInstanceOf(AiProviderAccessDeniedException.class);
        verifyNoInteractions(dao);
    }

    @Test
    void nonAdminCannotDeleteProviders() {
        assertThatThrownBy(() -> service.deleteProvider(instanceId, regularUser))
                .isInstanceOf(AiProviderAccessDeniedException.class);
        verifyNoInteractions(dao);
    }

    @Test
    void nonAdminCannotTestConnection() {
        assertThatThrownBy(() -> service.testConnection(instanceId, regularUser))
                .isInstanceOf(AiProviderAccessDeniedException.class);
        verifyNoInteractions(connectionTester);
    }

    @Test
    void adminCanListAllInstances() {
        when(dao.getAll()).thenReturn(List.of(existingGeminiInstance()));

        List<AdminAiProviderResponse> result = service.listProviders(admin);

        assertThat(result).hasSize(1);
    }

    @Test
    void createRejectsAnUnknownAdapterType() {
        AiProviderCreateRequest request = new AiProviderCreateRequest();
        request.setAdapterType("NOT_A_TYPE");
        request.setDisplayName("Something");

        assertThatThrownBy(() -> service.createProvider(request, admin))
                .isInstanceOf(AiProviderValidationException.class);
    }

    @Test
    void createRejectsPlaceholderAsANewInstance() {
        AiProviderCreateRequest request = new AiProviderCreateRequest();
        request.setAdapterType("PLACEHOLDER");
        request.setDisplayName("Another Placeholder");

        assertThatThrownBy(() -> service.createProvider(request, admin))
                .isInstanceOf(AiProviderValidationException.class);
    }

    @Test
    void createRejectsAMissingDisplayName() {
        AiProviderCreateRequest request = new AiProviderCreateRequest();
        request.setAdapterType("OPENAI_COMPATIBLE");

        assertThatThrownBy(() -> service.createProvider(request, admin))
                .isInstanceOf(AiProviderValidationException.class);
    }

    @Test
    void createSavesANewInstance() {
        when(dao.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AiProviderCreateRequest request = new AiProviderCreateRequest();
        request.setAdapterType("OPENAI_COMPATIBLE");
        request.setDisplayName("My Ollama Server");
        request.setBaseUrl("http://localhost:11434/v1");
        request.setDefaultModel("llama3");
        request.setEnabled(true);

        AdminAiProviderResponse response = service.createProvider(request, admin);

        assertThat(response.getAdapterType()).isEqualTo("OPENAI_COMPATIBLE");
        assertThat(response.getDisplayName()).isEqualTo("My Ollama Server");
        assertThat(response.getBaseUrl()).isEqualTo("http://localhost:11434/v1");
        assertThat(response.isEnabled()).isTrue();
    }

    @Test
    void updateRejectsAnUnknownInstance() {
        when(dao.getById(instanceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateProvider(instanceId, new AiProviderUpdateRequest(), admin))
                .isInstanceOf(AiProviderNotFoundException.class);
    }

    @Test
    void updateEnablesAndSetsModelAndBaseUrl() {
        AiProviderConfiguration existing = existingGeminiInstance();
        existing.setEnabled(false);
        when(dao.getById(instanceId)).thenReturn(Optional.of(existing));
        when(dao.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AiProviderUpdateRequest request = new AiProviderUpdateRequest();
        request.setEnabled(true);
        request.setDefaultModel("gemini-2.5-flash");
        request.setBaseUrl("https://custom.example");

        AdminAiProviderResponse response = service.updateProvider(instanceId, request, admin);

        assertThat(response.isEnabled()).isTrue();
        assertThat(response.getDefaultModel()).isEqualTo("gemini-2.5-flash");
        assertThat(response.getBaseUrl()).isEqualTo("https://custom.example");
        verify(resolver).invalidate(instanceId);
    }

    @Test
    void updateChangesTheDisplayName() {
        AiProviderConfiguration existing = existingGeminiInstance();
        when(dao.getById(instanceId)).thenReturn(Optional.of(existing));
        when(dao.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AiProviderUpdateRequest request = new AiProviderUpdateRequest();
        request.setDisplayName("Renamed Instance");

        AdminAiProviderResponse response = service.updateProvider(instanceId, request, admin);

        assertThat(response.getDisplayName()).isEqualTo("Renamed Instance");
    }

    @Test
    void updateSetsUpdatedByToTheAdminRequester() {
        AiProviderConfiguration existing = existingGeminiInstance();
        when(dao.getById(instanceId)).thenReturn(Optional.of(existing));
        ArgumentCaptor<AiProviderConfiguration> captor = ArgumentCaptor.forClass(AiProviderConfiguration.class);
        when(dao.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        service.updateProvider(instanceId, new AiProviderUpdateRequest(), admin);

        assertThat(captor.getValue().getUpdatedBy()).isEqualTo(admin);
        assertThat(captor.getValue().getUpdatedAt()).isNotNull();
    }

    @Test
    void updateStoresEncryptedApiKeyWhenProvided() {
        AiProviderConfiguration existing = existingGeminiInstance();
        when(dao.getById(instanceId)).thenReturn(Optional.of(existing));
        when(dao.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(encryptor.isAvailable()).thenReturn(true);
        when(encryptor.encrypt("real-api-key")).thenReturn("ciphertext");

        AiProviderUpdateRequest request = new AiProviderUpdateRequest();
        request.setApiKey("real-api-key");

        AdminAiProviderResponse response = service.updateProvider(instanceId, request, admin);

        assertThat(response.isHasApiKey()).isTrue();
        verify(encryptor).encrypt("real-api-key");
    }

    @Test
    void updateRejectsApiKeyWhenEncryptionIsUnavailable() {
        AiProviderConfiguration existing = existingGeminiInstance();
        when(dao.getById(instanceId)).thenReturn(Optional.of(existing));
        when(encryptor.isAvailable()).thenReturn(false);

        AiProviderUpdateRequest request = new AiProviderUpdateRequest();
        request.setApiKey("real-api-key");

        assertThatThrownBy(() -> service.updateProvider(instanceId, request, admin))
                .isInstanceOf(AiProviderValidationException.class);
        verify(dao, never()).save(any());
    }

    @Test
    void omittingApiKeyLeavesExistingKeyUnchanged() {
        AiProviderConfiguration existing = existingGeminiInstance();
        existing.setEncryptedApiKey("existing-ciphertext");
        when(dao.getById(instanceId)).thenReturn(Optional.of(existing));
        when(dao.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AiProviderUpdateRequest request = new AiProviderUpdateRequest();
        request.setEnabled(true);

        AdminAiProviderResponse response = service.updateProvider(instanceId, request, admin);

        assertThat(response.isHasApiKey()).isTrue();
        verifyNoInteractions(encryptor);
    }

    @Test
    void clearApiKeyRemovesTheStoredKey() {
        AiProviderConfiguration existing = existingGeminiInstance();
        existing.setEncryptedApiKey("existing-ciphertext");
        when(dao.getById(instanceId)).thenReturn(Optional.of(existing));
        ArgumentCaptor<AiProviderConfiguration> captor = ArgumentCaptor.forClass(AiProviderConfiguration.class);
        when(dao.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        AiProviderUpdateRequest request = new AiProviderUpdateRequest();
        request.setClearApiKey(true);

        AdminAiProviderResponse response = service.updateProvider(instanceId, request, admin);

        assertThat(response.isHasApiKey()).isFalse();
        assertThat(captor.getValue().getEncryptedApiKey()).isNull();
        verify(resolver).invalidate(instanceId);
    }

    @Test
    void deleteRejectsAnUnknownInstance() {
        when(dao.getById(instanceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteProvider(instanceId, admin))
                .isInstanceOf(AiProviderNotFoundException.class);
    }

    @Test
    void deleteRejectsThePlaceholderInstance() {
        AiProviderConfiguration placeholder = new AiProviderConfiguration();
        placeholder.setAdapterType(AdapterType.PLACEHOLDER.name());
        when(dao.getById(instanceId)).thenReturn(Optional.of(placeholder));

        assertThatThrownBy(() -> service.deleteProvider(instanceId, admin))
                .isInstanceOf(AiProviderValidationException.class);
        verify(dao, never()).deleteById(any());
    }

    @Test
    void deleteRemovesTheInstanceAndInvalidatesTheCache() {
        when(dao.getById(instanceId)).thenReturn(Optional.of(existingGeminiInstance()));

        service.deleteProvider(instanceId, admin);

        verify(dao).deleteById(instanceId);
        verify(resolver).invalidate(instanceId);
    }

    @Test
    void testConnectionDelegatesToConnectionTester() {
        when(dao.getById(instanceId)).thenReturn(Optional.of(existingGeminiInstance()));
        when(connectionTester.test(instanceId)).thenReturn(new AiProviderTestResult(true, "Connection successful."));

        AiProviderTestResult result = service.testConnection(instanceId, admin);

        assertThat(result.isSuccess()).isTrue();
        verify(connectionTester).test(instanceId);
    }

    @Test
    void testConnectionRejectsAnUnknownInstance() {
        when(dao.getById(instanceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.testConnection(instanceId, admin))
                .isInstanceOf(AiProviderNotFoundException.class);
        verifyNoInteractions(connectionTester);
    }
}
