package de.jeb.japp.ai.service;

import de.jeb.japp.dao.ai.AiProviderConfigurationDao;
import de.jeb.japp.model.ai.AiProviderConfiguration;
import de.jeb.japp.model.generation.GenerationProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
    void createsARowForEveryKnownProviderWhenNoneExist() {
        when(dao.existsByProvider(any())).thenReturn(false);

        seeder.run(null);

        verify(dao, times(GenerationProvider.values().length)).save(any());
    }

    @Test
    void doesNotOverwriteAnExistingRow() {
        when(dao.existsByProvider(any())).thenReturn(true);

        seeder.run(null);

        verify(dao, never()).save(any());
    }

    @Test
    void placeholderSeedsAsEnabledByDefault() {
        when(dao.existsByProvider(any())).thenReturn(false);
        ArgumentCaptor<AiProviderConfiguration> captor = ArgumentCaptor.forClass(AiProviderConfiguration.class);

        seeder.run(null);

        verify(dao, atLeastOnce()).save(captor.capture());
        AiProviderConfiguration placeholder = captor.getAllValues().stream()
                .filter(c -> c.getProvider().equals("PLACEHOLDER"))
                .findFirst()
                .orElseThrow();
        assertThat(placeholder.isEnabled()).isTrue();
    }

    @Test
    void geminiSeedsAsDisabledByDefault() {
        when(dao.existsByProvider(any())).thenReturn(false);
        ArgumentCaptor<AiProviderConfiguration> captor = ArgumentCaptor.forClass(AiProviderConfiguration.class);

        seeder.run(null);

        verify(dao, atLeastOnce()).save(captor.capture());
        AiProviderConfiguration gemini = captor.getAllValues().stream()
                .filter(c -> c.getProvider().equals("GEMINI"))
                .findFirst()
                .orElseThrow();
        assertThat(gemini.isEnabled()).isFalse();
    }

    @Test
    void resultAlwaysCoversPlaceholderAndGemini() {
        when(dao.existsByProvider(any())).thenReturn(false);
        ArgumentCaptor<AiProviderConfiguration> captor = ArgumentCaptor.forClass(AiProviderConfiguration.class);

        seeder.run(null);

        verify(dao, atLeastOnce()).save(captor.capture());
        List<String> seededProviders = captor.getAllValues().stream().map(AiProviderConfiguration::getProvider).toList();
        assertThat(seededProviders).contains("PLACEHOLDER", "GEMINI");
    }
}
