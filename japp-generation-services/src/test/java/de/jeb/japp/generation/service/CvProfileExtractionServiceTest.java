package de.jeb.japp.generation.service;

import de.jeb.japp.ai.service.ProviderSettingsResolver;
import de.jeb.japp.ai.service.ResolvedProviderConfig;
import de.jeb.japp.commons.exceptions.cv.CVAccessDeniedException;
import de.jeb.japp.commons.exceptions.cv.CVNotFoundException;
import de.jeb.japp.commons.exceptions.generation.CvProfileGenerationException;
import de.jeb.japp.dao.ai.AiProviderConfigurationDao;
import de.jeb.japp.dao.cv.CVDao;
import de.jeb.japp.dao.cv.CVProfileDao;
import de.jeb.japp.generation.service.provider.CvProfileExtractionAdapter;
import de.jeb.japp.generation.service.provider.CvProfileExtractionAdapterRegistry;
import de.jeb.japp.generation.service.provider.CvProfileExtractionResult;
import de.jeb.japp.generation.service.provider.ExperienceData;
import de.jeb.japp.generation.service.provider.LanguageData;
import de.jeb.japp.model.ai.AdapterType;
import de.jeb.japp.model.ai.AiProviderConfiguration;
import de.jeb.japp.model.cv.CVDocument;
import de.jeb.japp.model.cv.CVProfile;
import de.jeb.japp.model.cv.Language;
import de.jeb.japp.model.cv.ProfileGenerationStatus;
import de.jeb.japp.model.cv.Skill;
import de.jeb.japp.model.user.User;
import de.jeb.japp.model.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CvProfileExtractionServiceTest {

    @Mock
    private CVProfileDao cvProfileDao;
    @Mock
    private CVDao cvDao;
    @Mock
    private AiProviderConfigurationDao providerDao;
    @Mock
    private ProviderSettingsResolver providerSettingsResolver;
    @Mock
    private CvProfileExtractionAdapterRegistry adapterRegistry;
    @Mock
    private CvProfileExtractionAdapter adapter;

    private CvProfileExtractionService service;

    private User owner;
    private User otherUser;
    private CVDocument cv;
    private AiProviderConfiguration placeholderInstance;
    private ResolvedProviderConfig resolvedConfig;

    @BeforeEach
    void setUp() {
        service = new CvProfileExtractionService(cvProfileDao, cvDao, providerDao, providerSettingsResolver, adapterRegistry);

        owner = new User();
        owner.setId(UUID.randomUUID());
        owner.setRole(UserRole.USER);

        otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        otherUser.setRole(UserRole.USER);

        cv = new CVDocument();
        ReflectionTestUtils.setField(cv, "id", UUID.randomUUID());
        cv.setOwner(owner);
        cv.setExtractedText("Jane Doe, Software Engineer at Acme since 2020.");

        placeholderInstance = new AiProviderConfiguration();
        ReflectionTestUtils.setField(placeholderInstance, "id", UUID.randomUUID());
        placeholderInstance.setAdapterType(AdapterType.PLACEHOLDER.name());

        resolvedConfig = new ResolvedProviderConfig(true, null, "model", "https://example.test");

        lenient().when(cvDao.getCVById(cv.getId())).thenReturn(Optional.of(cv));
        lenient().when(providerDao.getFirstByAdapterType(AdapterType.PLACEHOLDER.name()))
                .thenReturn(Optional.of(placeholderInstance));
        lenient().when(adapterRegistry.resolve(AdapterType.PLACEHOLDER)).thenReturn(adapter);
        lenient().when(providerSettingsResolver.resolve(placeholderInstance.getId())).thenReturn(resolvedConfig);
        lenient().when(cvProfileDao.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void generateCreatesANewProfileAndMarksItCompletedOnSuccess() {
        lenient().when(cvProfileDao.getByCvDocumentId(cv.getId())).thenReturn(Optional.empty());
        CvProfileExtractionResult result = new CvProfileExtractionResult(
                "Jane Doe",
                "An experienced engineer.",
                List.of(new ExperienceData("Acme", "Engineer", "2020-01-01", null, "Built things.")),
                List.of("Java", "Kubernetes"),
                List.of(new LanguageData("English", "native"), new LanguageData("German", "B2")));
        when(adapter.extract(any(), any())).thenReturn(result);

        CVProfile profile = service.generate(cv.getId(), null, owner);

        assertThat(profile.getStatus()).isEqualTo(ProfileGenerationStatus.COMPLETED);
        assertThat(profile.getFullName()).isEqualTo("Jane Doe");
        assertThat(profile.getSummary()).isEqualTo("An experienced engineer.");
        assertThat(profile.getErrorMessage()).isNull();
        assertThat(profile.getGeneratedAt()).isNotNull();
        assertThat(profile.getExperiences()).hasSize(1);
        assertThat(profile.getExperiences().get(0).getStartDate()).isEqualTo(LocalDate.of(2020, 1, 1));
        assertThat(profile.getExperiences().get(0).getCvProfile()).isSameAs(profile);
        assertThat(profile.getSkills()).extracting(Skill::getName).containsExactly("Java", "Kubernetes");
        assertThat(profile.getSkills().get(0).getCvProfile()).isSameAs(profile);
        assertThat(profile.getLanguages()).extracting(Language::getName, Language::getLevel)
                .containsExactly(org.assertj.core.groups.Tuple.tuple("English", "native"),
                        org.assertj.core.groups.Tuple.tuple("German", "B2"));
    }

    @Test
    void generateReplacesExperiencesSkillsAndLanguagesFromAPreviousRunInsteadOfAccumulatingThem() {
        CVProfile existing = new CVProfile();
        existing.setCvDocument(cv);
        existing.setExperiences(new java.util.ArrayList<>(List.of(staleExperience(existing))));
        existing.setSkills(new java.util.ArrayList<>(List.of(staleSkill(existing))));
        existing.setLanguages(new java.util.ArrayList<>(List.of(staleLanguage(existing))));
        when(cvProfileDao.getByCvDocumentId(cv.getId())).thenReturn(Optional.of(existing));
        when(adapter.extract(any(), any()))
                .thenReturn(new CvProfileExtractionResult("Jane", "Summary", List.of(), List.of(), List.of()));

        CVProfile profile = service.generate(cv.getId(), null, owner);

        assertThat(profile.getExperiences()).isEmpty();
        assertThat(profile.getSkills()).isEmpty();
        assertThat(profile.getLanguages()).isEmpty();
    }

    @Test
    void blankSkillAndLanguageNamesAreDropped() {
        lenient().when(cvProfileDao.getByCvDocumentId(cv.getId())).thenReturn(Optional.empty());
        when(adapter.extract(any(), any())).thenReturn(new CvProfileExtractionResult(
                "Jane", "Summary", List.of(),
                java.util.Arrays.asList("Java", "  ", null),
                List.of(new LanguageData("  ", "native"), new LanguageData("French", "A1"))));

        CVProfile profile = service.generate(cv.getId(), null, owner);

        assertThat(profile.getSkills()).extracting(Skill::getName).containsExactly("Java");
        assertThat(profile.getLanguages()).extracting(Language::getName).containsExactly("French");
    }

    @Test
    void generateMarksTheProfileFailedWithoutThrowingWhenTheAdapterFails() {
        lenient().when(cvProfileDao.getByCvDocumentId(cv.getId())).thenReturn(Optional.empty());
        when(adapter.extract(any(), any())).thenThrow(new CvProfileGenerationException("boom"));

        CVProfile profile = service.generate(cv.getId(), null, owner);

        assertThat(profile.getStatus()).isEqualTo(ProfileGenerationStatus.FAILED);
        assertThat(profile.getErrorMessage()).isEqualTo("boom");
    }

    @Test
    void generateRejectsACvOwnedBySomeoneElse() {
        assertThatThrownBy(() -> service.generate(cv.getId(), null, otherUser))
                .isInstanceOf(CVAccessDeniedException.class);
    }

    @Test
    void generateRejectsAnUnknownCv() {
        UUID unknownId = UUID.randomUUID();
        when(cvDao.getCVById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.generate(unknownId, null, owner))
                .isInstanceOf(CVNotFoundException.class);
    }

    @Test
    void getReturnsEmptyWhenNoGenerationHasEverBeenAttempted() {
        when(cvProfileDao.getByCvDocumentId(cv.getId())).thenReturn(Optional.empty());

        assertThat(service.get(cv.getId(), owner)).isEmpty();
    }

    @Test
    void getRejectsACvOwnedBySomeoneElse() {
        assertThatThrownBy(() -> service.get(cv.getId(), otherUser))
                .isInstanceOf(CVAccessDeniedException.class);
    }

    private de.jeb.japp.model.cv.Experience staleExperience(CVProfile owner) {
        de.jeb.japp.model.cv.Experience experience = new de.jeb.japp.model.cv.Experience();
        experience.setCvProfile(owner);
        experience.setCompany("Old Co");
        return experience;
    }

    private Skill staleSkill(CVProfile owner) {
        Skill skill = new Skill();
        skill.setCvProfile(owner);
        skill.setName("Old Skill");
        return skill;
    }

    private Language staleLanguage(CVProfile owner) {
        Language language = new Language();
        language.setCvProfile(owner);
        language.setName("Old Language");
        return language;
    }
}
