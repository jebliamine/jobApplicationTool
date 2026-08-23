package de.jeb.japp.ai.service;

import de.jeb.japp.dao.ai.AiProviderConfigurationDao;
import de.jeb.japp.model.ai.AdapterType;
import de.jeb.japp.model.ai.AiProviderConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiProviderConfigurationSeederTest {

    @Mock
    private AiProviderConfigurationDao dao;

    private AiProviderConfigurationSeeder seeder;

    @BeforeEach
    void setUp() {
        seeder = new AiProviderConfigurationSeeder(dao);
    }

    @Test
    void createsTheBuiltInPlaceholderInstanceWhenNoneExists() {
        when(dao.existsByAdapterType(AdapterType.PLACEHOLDER.name())).thenReturn(false);
        ArgumentCaptor<AiProviderConfiguration> captor = ArgumentCaptor.forClass(AiProviderConfiguration.class);

        seeder.run(null);

        verify(dao).save(captor.capture());
        AiProviderConfiguration placeholder = captor.getValue();
        assertThat(placeholder.getAdapterType()).isEqualTo(AdapterType.PLACEHOLDER.name());
        assertThat(placeholder.isEnabled()).isTrue();
    }

    @Test
    void doesNotOverwriteAnExistingPlaceholderRow() {
        when(dao.existsByAdapterType(AdapterType.PLACEHOLDER.name())).thenReturn(true);

        seeder.run(null);

        verify(dao, never()).save(any());
    }
}
