package de.jeb.japp.generation.service;

import de.jeb.japp.ai.service.ProviderSettingsResolver;
import de.jeb.japp.ai.service.ResolvedProviderConfig;
import de.jeb.japp.commons.exceptions.ai.AiProviderNotFoundException;
import de.jeb.japp.commons.exceptions.job.JobExtractionException;
import de.jeb.japp.commons.exceptions.job.JobValidationException;
import de.jeb.japp.dao.ai.AiProviderConfigurationDao;
import de.jeb.japp.generation.service.provider.job.JobExtractionAdapter;
import de.jeb.japp.generation.service.provider.job.JobExtractionAdapterRegistry;
import de.jeb.japp.generation.service.provider.job.JobExtractionResult;
import de.jeb.japp.model.ai.AdapterType;
import de.jeb.japp.model.ai.AiProviderConfiguration;
import de.jeb.japp.model.job.EmploymentType;
import de.jeb.japp.model.job.WorkMode;
import de.jeb.japp.model.job.dto.JobExtractionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobExtractionServiceTest {

    @Mock
    private AiProviderConfigurationDao providerDao;
    @Mock
    private ProviderSettingsResolver providerSettingsResolver;
    @Mock
    private JobExtractionAdapterRegistry adapterRegistry;
    @Mock
    private JobExtractionAdapter adapter;

    private JobExtractionService service;

    private AiProviderConfiguration placeholderInstance;
    private ResolvedProviderConfig resolvedConfig;

    @BeforeEach
    void setUp() {
        service = new JobExtractionService(providerDao, providerSettingsResolver, adapterRegistry);

        placeholderInstance = new AiProviderConfiguration();
        ReflectionTestUtils.setField(placeholderInstance, "id", UUID.randomUUID());
        placeholderInstance.setAdapterType(AdapterType.PLACEHOLDER.name());

        resolvedConfig = new ResolvedProviderConfig(true, null, "model", "https://example.test");

        lenient().when(providerDao.getFirstByAdapterType(AdapterType.PLACEHOLDER.name()))
                .thenReturn(Optional.of(placeholderInstance));
        lenient().when(adapterRegistry.resolve(AdapterType.PLACEHOLDER)).thenReturn(adapter);
        lenient().when(providerSettingsResolver.resolve(placeholderInstance.getId())).thenReturn(resolvedConfig);
    }

    @Test
    void extractMapsAFullResultOntoTheResponse() {
        when(adapter.extract(any(), any())).thenReturn(new JobExtractionResult(
                "Backend Engineer", "Acme", "Build things.", "Berlin",
                "FULL_TIME", "REMOTE", "€60,000-€75,000", "https://example.test/job"));

        JobExtractionResponse response = service.extract("some job posting text", null);

        assertThat(response.getTitle()).isEqualTo("Backend Engineer");
        assertThat(response.getCompanyName()).isEqualTo("Acme");
        assertThat(response.getDescription()).isEqualTo("Build things.");
        assertThat(response.getLocation()).isEqualTo("Berlin");
        assertThat(response.getEmploymentType()).isEqualTo(EmploymentType.FULL_TIME);
        assertThat(response.getWorkMode()).isEqualTo(WorkMode.REMOTE);
        assertThat(response.getSalaryRange()).isEqualTo("€60,000-€75,000");
        assertThat(response.getUrl()).isEqualTo("https://example.test/job");
    }

    @Test
    void extractRejectsBlankRawText() {
        assertThatThrownBy(() -> service.extract("   ", null))
                .isInstanceOf(JobValidationException.class);
    }

    @Test
    void extractRejectsNullRawText() {
        assertThatThrownBy(() -> service.extract(null, null))
                .isInstanceOf(JobValidationException.class);
    }

    @Test
    void extractIgnoresAnUnrecognizedEmploymentTypeOrWorkMode() {
        when(adapter.extract(any(), any())).thenReturn(new JobExtractionResult(
                "Backend Engineer", "Acme", "Build things.", "Berlin",
                "NOT_A_REAL_TYPE", "NOT_A_REAL_MODE", null, null));

        JobExtractionResponse response = service.extract("some job posting text", null);

        assertThat(response.getEmploymentType()).isNull();
        assertThat(response.getWorkMode()).isNull();
    }

    @Test
    void extractBlanksOutEmptyStringFields() {
        when(adapter.extract(any(), any())).thenReturn(new JobExtractionResult(
                "Backend Engineer", "  ", "Build things.", "", null, null, "  ", ""));

        JobExtractionResponse response = service.extract("some job posting text", null);

        assertThat(response.getCompanyName()).isNull();
        assertThat(response.getLocation()).isNull();
        assertThat(response.getSalaryRange()).isNull();
        assertThat(response.getUrl()).isNull();
    }

    @Test
    void extractUsesTheRequestedProviderInstanceWhenGiven() {
        AiProviderConfiguration openAiInstance = new AiProviderConfiguration();
        UUID providerId = UUID.randomUUID();
        ReflectionTestUtils.setField(openAiInstance, "id", providerId);
        openAiInstance.setAdapterType(AdapterType.OPENAI_COMPATIBLE.name());
        when(providerDao.getById(providerId)).thenReturn(Optional.of(openAiInstance));
        when(adapterRegistry.resolve(AdapterType.OPENAI_COMPATIBLE)).thenReturn(adapter);
        when(providerSettingsResolver.resolve(providerId)).thenReturn(resolvedConfig);
        when(adapter.extract(any(), any())).thenReturn(
                new JobExtractionResult(null, null, null, null, null, null, null, null));

        service.extract("some job posting text", providerId);

        verify(providerDao).getById(providerId);
        verify(providerDao, org.mockito.Mockito.never()).getFirstByAdapterType(any());
    }

    @Test
    void extractRejectsAnUnknownProviderId() {
        UUID unknownId = UUID.randomUUID();
        when(providerDao.getById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.extract("some job posting text", unknownId))
                .isInstanceOf(AiProviderNotFoundException.class);
    }

    @Test
    void extractPropagatesTheAdapterFailureRatherThanSwallowingIt() {
        when(adapter.extract(any(), any())).thenThrow(new JobExtractionException("boom"));

        assertThatThrownBy(() -> service.extract("some job posting text", null))
                .isInstanceOf(JobExtractionException.class)
                .hasMessage("boom");
    }
}
