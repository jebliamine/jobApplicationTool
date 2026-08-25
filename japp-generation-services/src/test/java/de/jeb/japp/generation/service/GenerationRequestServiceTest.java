package de.jeb.japp.generation.service;

import de.jeb.japp.ai.service.ProviderSettingsResolver;
import de.jeb.japp.ai.service.ResolvedProviderConfig;
import de.jeb.japp.commons.exceptions.cv.CVAccessDeniedException;
import de.jeb.japp.commons.exceptions.generation.CoverLetterGenerationException;
import de.jeb.japp.commons.exceptions.generation.GenerationRequestAccessDeniedException;
import de.jeb.japp.commons.exceptions.generation.GenerationRequestNotFoundException;
import de.jeb.japp.commons.exceptions.generation.GenerationRequestValidationException;
import de.jeb.japp.commons.exceptions.job.JobAccessDeniedException;
import de.jeb.japp.dao.ai.AiProviderConfigurationDao;
import de.jeb.japp.dao.coverletter.CoverLetterDao;
import de.jeb.japp.dao.cv.CVDao;
import de.jeb.japp.dao.cv.CVProfileDao;
import de.jeb.japp.dao.generation.GenerationRequestDao;
import de.jeb.japp.dao.job.JobDao;
import de.jeb.japp.generation.service.provider.CoverLetterGenerationAdapter;
import de.jeb.japp.generation.service.provider.CoverLetterGenerationAdapterRegistry;
import de.jeb.japp.generation.service.provider.GenerationInput;
import de.jeb.japp.generation.service.provider.GenerationResult;
import de.jeb.japp.model.ai.AdapterType;
import de.jeb.japp.model.ai.AiProviderConfiguration;
import de.jeb.japp.model.company.Company;
import de.jeb.japp.model.coverLetter.CoverLetter;
import de.jeb.japp.model.cv.CVDocument;
import de.jeb.japp.model.cv.CVProfile;
import de.jeb.japp.model.cv.Experience;
import de.jeb.japp.model.cv.Language;
import de.jeb.japp.model.cv.ProfileGenerationStatus;
import de.jeb.japp.model.cv.Skill;
import de.jeb.japp.model.generation.GenerationRequest;
import de.jeb.japp.model.generation.GenerationStatus;
import de.jeb.japp.model.generation.dto.GenerationRequestCreateRequest;
import de.jeb.japp.model.job.Job;
import de.jeb.japp.model.user.User;
import de.jeb.japp.model.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenerationRequestServiceTest {

    @Mock
    private GenerationRequestDao generationRequestDao;
    @Mock
    private CoverLetterDao coverLetterDao;
    @Mock
    private JobDao jobDao;
    @Mock
    private CVDao cvDao;
    @Mock
    private CVProfileDao cvProfileDao;
    @Mock
    private AiProviderConfigurationDao providerDao;
    @Mock
    private ProviderSettingsResolver providerSettingsResolver;
    @Mock
    private CoverLetterGenerationAdapterRegistry adapterRegistry;
    @Mock
    private CoverLetterGenerationAdapter placeholderAdapter;
    @Mock
    private CoverLetterGenerationAdapter geminiAdapter;

    private GenerationRequestService generationRequestService;

    private User owner;
    private User otherUser;
    private User admin;
    private Company company;
    private Job job;
    private CVDocument cv;
    private AiProviderConfiguration placeholderInstance;
    private AiProviderConfiguration geminiInstance;

    @BeforeEach
    void setUp() {
        generationRequestService = new GenerationRequestService(
                generationRequestDao, coverLetterDao, jobDao, cvDao, cvProfileDao, providerDao, providerSettingsResolver,
                adapterRegistry);

        owner = new User();
        owner.setId(UUID.randomUUID());
        owner.setRole(UserRole.USER);
        owner.setFullName("Jane Doe");
        owner.setEmail("jane@example.com");

        otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        otherUser.setRole(UserRole.USER);

        admin = new User();
        admin.setId(UUID.randomUUID());
        admin.setRole(UserRole.ADMIN);

        company = new Company();
        company.setName("Acme");

        job = new Job();
        job.setOwner(owner);
        job.setCompany(company);
        job.setTitle("Backend Engineer");
        job.setDescription("Build and maintain backend services.");

        cv = new CVDocument();
        cv.setOwner(owner);
        cv.setTitle("My Resume");
        cv.setExtractedText("Extracted CV content.");
        ReflectionTestUtils.setField(cv, "id", UUID.randomUUID());

        placeholderInstance = providerInstance(AdapterType.PLACEHOLDER, "Placeholder");
        geminiInstance = providerInstance(AdapterType.GEMINI_GENERATE_CONTENT, "Google Gemini");

        // Requests without an explicit providerId default to the built-in Placeholder instance.
        lenient().when(providerDao.getFirstByAdapterType(AdapterType.PLACEHOLDER.name()))
                .thenReturn(Optional.of(placeholderInstance));
        lenient().when(providerDao.getById(placeholderInstance.getId())).thenReturn(Optional.of(placeholderInstance));
        lenient().when(providerDao.getById(geminiInstance.getId())).thenReturn(Optional.of(geminiInstance));

        lenient().when(adapterRegistry.resolve(AdapterType.PLACEHOLDER)).thenReturn(placeholderAdapter);
        lenient().when(adapterRegistry.resolve(AdapterType.GEMINI_GENERATE_CONTENT)).thenReturn(geminiAdapter);

        lenient().when(providerSettingsResolver.resolve(placeholderInstance.getId()))
                .thenReturn(new ResolvedProviderConfig(true, null, "deterministic-v1", null));
        lenient().when(providerSettingsResolver.resolve(geminiInstance.getId()))
                .thenReturn(new ResolvedProviderConfig(true, "key", "gemini-2.0-flash", "https://gemini.test"));

        // saveGenerationRequest is called repeatedly through process(); echo back whatever is passed in.
        lenient().when(generationRequestDao.saveGenerationRequest(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Default: the resolved adapter succeeds. Individual tests override this to exercise failure handling.
        lenient().when(placeholderAdapter.generate(any(), any()))
                .thenReturn(new GenerationResult(
                        "Generated cover letter mentioning " + job.getTitle() + " at " + company.getName() + "."));
        lenient().when(geminiAdapter.generate(any(), any()))
                .thenReturn(new GenerationResult(
                        "Gemini cover letter mentioning " + job.getTitle() + " at " + company.getName() + "."));
    }

    private AiProviderConfiguration providerInstance(AdapterType type, String displayName) {
        AiProviderConfiguration config = new AiProviderConfiguration();
        config.setAdapterType(type.name());
        config.setDisplayName(displayName);
        config.setEnabled(true);
        ReflectionTestUtils.setField(config, "id", UUID.randomUUID());
        return config;
    }

    private GenerationRequestCreateRequest validRequest() {
        GenerationRequestCreateRequest request = new GenerationRequestCreateRequest();
        request.setJobId(UUID.randomUUID());
        request.setCvDocumentId(UUID.randomUUID());
        return request;
    }

    private GenerationRequest requestOwnedBy(User user) {
        GenerationRequest generationRequest = new GenerationRequest();
        generationRequest.setUser(user);
        generationRequest.setJob(job);
        generationRequest.setStatus(GenerationStatus.COMPLETED);
        return generationRequest;
    }

    @Test
    void userCanGenerateUsingTheirOwnJobAndCv() {
        GenerationRequestCreateRequest request = validRequest();
        when(jobDao.getJobById(request.getJobId())).thenReturn(Optional.of(job));
        when(cvDao.getCVById(request.getCvDocumentId())).thenReturn(Optional.of(cv));

        GenerationRequest result = generationRequestService.create(request, owner);

        assertThat(result.getUser()).isEqualTo(owner);
        assertThat(result.getJob()).isEqualTo(job);
        assertThat(result.getCvDocument()).isEqualTo(cv);
        assertThat(result.getJobDescriptionSnapshot()).isEqualTo(job.getDescription());
        assertThat(result.getCvTextSnapshot()).isEqualTo(cv.getExtractedText());
    }

    @Test
    void cvTextSnapshotIsNullWhenTheCvHasNoExtractedText() {
        cv.setExtractedText(null);
        GenerationRequestCreateRequest request = validRequest();
        when(jobDao.getJobById(request.getJobId())).thenReturn(Optional.of(job));
        when(cvDao.getCVById(request.getCvDocumentId())).thenReturn(Optional.of(cv));

        GenerationRequest result = generationRequestService.create(request, owner);

        assertThat(result.getCvTextSnapshot()).isNull();
    }

    @Test
    void generationInputCarriesTheCvsExtractedText() {
        GenerationRequestCreateRequest request = validRequest();
        when(jobDao.getJobById(request.getJobId())).thenReturn(Optional.of(job));
        when(cvDao.getCVById(request.getCvDocumentId())).thenReturn(Optional.of(cv));

        generationRequestService.create(request, owner);

        ArgumentCaptor<GenerationInput> captor = ArgumentCaptor.forClass(GenerationInput.class);
        verify(placeholderAdapter).generate(any(), captor.capture());
        assertThat(captor.getValue().cvText()).isEqualTo(cv.getExtractedText());
    }

    private CVProfile completedProfile(String fullName, String summary, Experience... experiences) {
        CVProfile profile = new CVProfile();
        profile.setCvDocument(cv);
        profile.setFullName(fullName);
        profile.setSummary(summary);
        profile.setExperiences(List.of(experiences));
        profile.setStatus(ProfileGenerationStatus.COMPLETED);
        return profile;
    }

    private Experience experience(String title, String company, String description) {
        Experience experience = new Experience();
        experience.setTitle(title);
        experience.setCompany(company);
        experience.setDescription(description);
        return experience;
    }

    private Skill skill(String name) {
        Skill skill = new Skill();
        skill.setName(name);
        return skill;
    }

    private Language language(String name, String level) {
        Language language = new Language();
        language.setName(name);
        language.setLevel(level);
        return language;
    }

    @Test
    void structuredCvProfileIsUsedWhenRequestedAndCompleted() {
        GenerationRequestCreateRequest request = validRequest();
        request.setUseStructuredCv(true);
        when(jobDao.getJobById(request.getJobId())).thenReturn(Optional.of(job));
        when(cvDao.getCVById(request.getCvDocumentId())).thenReturn(Optional.of(cv));
        when(cvProfileDao.getByCvDocumentId(cv.getId())).thenReturn(Optional.of(
                completedProfile("Jane Doe", "Backend engineer with 5 years of experience.",
                        experience("Software Engineer", "Acme Corp", "Built and maintained backend services."))));

        GenerationRequest result = generationRequestService.create(request, owner);

        assertThat(result.getCvTextSnapshot())
                .contains("Jane Doe")
                .contains("Backend engineer with 5 years of experience.")
                .contains("Software Engineer")
                .contains("Acme Corp")
                .contains("Built and maintained backend services.")
                .doesNotContain(cv.getExtractedText());

        ArgumentCaptor<GenerationInput> captor = ArgumentCaptor.forClass(GenerationInput.class);
        verify(placeholderAdapter).generate(any(), captor.capture());
        assertThat(captor.getValue().cvText()).isEqualTo(result.getCvTextSnapshot());
    }

    @Test
    void structuredCvTextIncludesSkillsAndLanguages() {
        GenerationRequestCreateRequest request = validRequest();
        request.setUseStructuredCv(true);
        when(jobDao.getJobById(request.getJobId())).thenReturn(Optional.of(job));
        when(cvDao.getCVById(request.getCvDocumentId())).thenReturn(Optional.of(cv));

        CVProfile profile = completedProfile("Jane Doe", "Backend engineer.",
                experience("Software Engineer", "Acme Corp", "Built things."));
        profile.setSkills(List.of(skill("Java"), skill("Kubernetes")));
        profile.setLanguages(List.of(language("English", "native"), language("German", "B2")));
        when(cvProfileDao.getByCvDocumentId(cv.getId())).thenReturn(Optional.of(profile));

        GenerationRequest result = generationRequestService.create(request, owner);

        assertThat(result.getCvTextSnapshot())
                .contains("Java")
                .contains("Kubernetes")
                .contains("English")
                .contains("native")
                .contains("German")
                .contains("B2");
    }

    @Test
    void rawCvTextIsUsedWhenStructuredCvIsNotRequested() {
        GenerationRequestCreateRequest request = validRequest();
        when(jobDao.getJobById(request.getJobId())).thenReturn(Optional.of(job));
        when(cvDao.getCVById(request.getCvDocumentId())).thenReturn(Optional.of(cv));

        GenerationRequest result = generationRequestService.create(request, owner);

        assertThat(result.getCvTextSnapshot()).isEqualTo(cv.getExtractedText());
        verifyNoInteractions(cvProfileDao);
    }

    @Test
    void fallsBackToRawTextWhenNoProfileHasEverBeenGenerated() {
        GenerationRequestCreateRequest request = validRequest();
        request.setUseStructuredCv(true);
        when(jobDao.getJobById(request.getJobId())).thenReturn(Optional.of(job));
        when(cvDao.getCVById(request.getCvDocumentId())).thenReturn(Optional.of(cv));
        when(cvProfileDao.getByCvDocumentId(cv.getId())).thenReturn(Optional.empty());

        GenerationRequest result = generationRequestService.create(request, owner);

        assertThat(result.getCvTextSnapshot()).isEqualTo(cv.getExtractedText());
    }

    @Test
    void fallsBackToRawTextWhenTheProfileFailedOrIsStillGenerating() {
        GenerationRequestCreateRequest request = validRequest();
        request.setUseStructuredCv(true);
        when(jobDao.getJobById(request.getJobId())).thenReturn(Optional.of(job));
        when(cvDao.getCVById(request.getCvDocumentId())).thenReturn(Optional.of(cv));
        CVProfile failedProfile = completedProfile("Jane Doe", "Summary");
        failedProfile.setStatus(ProfileGenerationStatus.FAILED);
        when(cvProfileDao.getByCvDocumentId(cv.getId())).thenReturn(Optional.of(failedProfile));

        GenerationRequest result = generationRequestService.create(request, owner);

        assertThat(result.getCvTextSnapshot()).isEqualTo(cv.getExtractedText());
    }

    @Test
    void serviceCallsTheAdapterAndReachesCompletedWhenItSucceeds() {
        GenerationRequestCreateRequest request = validRequest();
        when(jobDao.getJobById(request.getJobId())).thenReturn(Optional.of(job));
        when(cvDao.getCVById(request.getCvDocumentId())).thenReturn(Optional.of(cv));

        GenerationRequest result = generationRequestService.create(request, owner);

        verify(placeholderAdapter).generate(any(), any());
        assertThat(result.getStatus()).isEqualTo(GenerationStatus.COMPLETED);
        assertThat(result.getStartedAt()).isNotNull();
        assertThat(result.getCompletedAt()).isNotNull();
        assertThat(result.getErrorMessage()).isNull();
        assertThat(result.getProviderInstance()).isEqualTo(placeholderInstance);
        assertThat(result.getModel()).isEqualTo("deterministic-v1");
    }

    @Test
    void aCoverLetterIsCreatedFromTheAdaptersResultOnSuccess() {
        GenerationRequestCreateRequest request = validRequest();
        when(jobDao.getJobById(request.getJobId())).thenReturn(Optional.of(job));
        when(cvDao.getCVById(request.getCvDocumentId())).thenReturn(Optional.of(cv));
        when(placeholderAdapter.generate(any(), any())).thenReturn(new GenerationResult("Text from the provider."));

        generationRequestService.create(request, owner);

        ArgumentCaptor<CoverLetter> captor = ArgumentCaptor.forClass(CoverLetter.class);
        verify(coverLetterDao).saveCoverLetter(captor.capture());
        CoverLetter savedCoverLetter = captor.getValue();

        assertThat(savedCoverLetter.getOwner()).isEqualTo(owner);
        assertThat(savedCoverLetter.getGenerationRequest()).isNotNull();
        // The stored text must come straight from the adapter's result — proves the
        // service depends on the abstraction rather than generating content itself.
        assertThat(savedCoverLetter.getResultText()).isEqualTo("Text from the provider.");
    }

    @Test
    void adapterFailureResultsInFailedWithTheErrorMessageStored() {
        GenerationRequestCreateRequest request = validRequest();
        when(jobDao.getJobById(request.getJobId())).thenReturn(Optional.of(job));
        when(cvDao.getCVById(request.getCvDocumentId())).thenReturn(Optional.of(cv));
        when(placeholderAdapter.generate(any(), any()))
                .thenThrow(new CoverLetterGenerationException("The provider could not generate a letter."));

        GenerationRequest result = generationRequestService.create(request, owner);

        assertThat(result.getStatus()).isEqualTo(GenerationStatus.FAILED);
        assertThat(result.getErrorMessage()).isEqualTo("The provider could not generate a letter.");
        assertThat(result.getCompletedAt()).isNotNull();
        verifyNoInteractions(coverLetterDao);
    }

    @Test
    void userCannotUseAnotherUsersJob() {
        GenerationRequestCreateRequest request = validRequest();
        Job othersJob = new Job();
        othersJob.setOwner(otherUser);
        when(jobDao.getJobById(request.getJobId())).thenReturn(Optional.of(othersJob));

        assertThatThrownBy(() -> generationRequestService.create(request, owner))
                .isInstanceOf(JobAccessDeniedException.class);

        verifyNoInteractions(coverLetterDao);
    }

    @Test
    void userCannotUseAnotherUsersCv() {
        GenerationRequestCreateRequest request = validRequest();
        CVDocument othersCv = new CVDocument();
        othersCv.setOwner(otherUser);
        when(jobDao.getJobById(request.getJobId())).thenReturn(Optional.of(job));
        when(cvDao.getCVById(request.getCvDocumentId())).thenReturn(Optional.of(othersCv));

        assertThatThrownBy(() -> generationRequestService.create(request, owner))
                .isInstanceOf(CVAccessDeniedException.class);

        verifyNoInteractions(coverLetterDao);
    }

    @Test
    void createRejectsMissingJob() {
        GenerationRequestCreateRequest request = validRequest();
        request.setJobId(null);

        assertThatThrownBy(() -> generationRequestService.create(request, owner))
                .isInstanceOf(GenerationRequestValidationException.class);

        verifyNoInteractions(jobDao, cvDao);
    }

    @Test
    void createRejectsMissingCv() {
        GenerationRequestCreateRequest request = validRequest();
        request.setCvDocumentId(null);

        assertThatThrownBy(() -> generationRequestService.create(request, owner))
                .isInstanceOf(GenerationRequestValidationException.class);

        verifyNoInteractions(jobDao, cvDao);
    }

    @Test
    void ownerCanGetTheirOwnGenerationRequest() {
        UUID id = UUID.randomUUID();
        GenerationRequest generationRequest = requestOwnedBy(owner);
        when(generationRequestDao.getGenerationRequestById(id)).thenReturn(Optional.of(generationRequest));

        assertThat(generationRequestService.get(id, owner)).isEqualTo(generationRequest);
    }

    @Test
    void adminCanGetAnyGenerationRequest() {
        UUID id = UUID.randomUUID();
        GenerationRequest generationRequest = requestOwnedBy(owner);
        when(generationRequestDao.getGenerationRequestById(id)).thenReturn(Optional.of(generationRequest));

        assertThat(generationRequestService.get(id, admin)).isEqualTo(generationRequest);
    }

    @Test
    void userCannotAccessAnotherUsersGenerationRequest() {
        UUID id = UUID.randomUUID();
        GenerationRequest generationRequest = requestOwnedBy(owner);
        when(generationRequestDao.getGenerationRequestById(id)).thenReturn(Optional.of(generationRequest));

        assertThatThrownBy(() -> generationRequestService.get(id, otherUser))
                .isInstanceOf(GenerationRequestAccessDeniedException.class);
    }

    @Test
    void getThrowsNotFoundForMissingGenerationRequest() {
        UUID id = UUID.randomUUID();
        when(generationRequestDao.getGenerationRequestById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> generationRequestService.get(id, owner))
                .isInstanceOf(GenerationRequestNotFoundException.class);
    }

    @Test
    void listReturnsAllGenerationRequestsForAdmin() {
        generationRequestService.list(admin);
        verify(generationRequestDao).getAllGenerationRequests();
        verify(generationRequestDao, never()).getAllGenerationRequestsByOwner(any());
    }

    @Test
    void listReturnsOnlyOwnGenerationRequestsForRegularUser() {
        generationRequestService.list(owner);
        verify(generationRequestDao).getAllGenerationRequestsByOwner(owner);
        verify(generationRequestDao, never()).getAllGenerationRequests();
    }

    @Test
    void listReturnsWhatDaoProvides() {
        when(generationRequestDao.getAllGenerationRequestsByOwner(owner))
                .thenReturn(List.of(requestOwnedBy(owner)));

        List<GenerationRequest> result = generationRequestService.list(owner);

        assertThat(result).hasSize(1);
    }

    @Test
    void defaultsToThePlaceholderInstanceWhenProviderIdIsOmitted() {
        GenerationRequestCreateRequest request = validRequest();
        when(jobDao.getJobById(request.getJobId())).thenReturn(Optional.of(job));
        when(cvDao.getCVById(request.getCvDocumentId())).thenReturn(Optional.of(cv));

        GenerationRequest result = generationRequestService.create(request, owner);

        verify(placeholderAdapter).generate(any(), any());
        verify(geminiAdapter, never()).generate(any(), any());
        assertThat(result.getProvider()).isEqualTo("Placeholder");
        assertThat(result.getModel()).isEqualTo("deterministic-v1");
    }

    @Test
    void selectsTheRequestedProviderInstance() {
        GenerationRequestCreateRequest request = validRequest();
        request.setProviderId(geminiInstance.getId());
        when(jobDao.getJobById(request.getJobId())).thenReturn(Optional.of(job));
        when(cvDao.getCVById(request.getCvDocumentId())).thenReturn(Optional.of(cv));

        GenerationRequest result = generationRequestService.create(request, owner);

        verify(geminiAdapter).generate(any(), any());
        verify(placeholderAdapter, never()).generate(any(), any());
        assertThat(result.getProvider()).isEqualTo("Google Gemini");
        assertThat(result.getModel()).isEqualTo("gemini-2.0-flash");
        assertThat(result.getStatus()).isEqualTo(GenerationStatus.COMPLETED);
    }

    @Test
    void selectedAdapterFailureResultsInFailedGenerationRequest() {
        GenerationRequestCreateRequest request = validRequest();
        request.setProviderId(geminiInstance.getId());
        when(jobDao.getJobById(request.getJobId())).thenReturn(Optional.of(job));
        when(cvDao.getCVById(request.getCvDocumentId())).thenReturn(Optional.of(cv));
        when(geminiAdapter.generate(any(), any()))
                .thenThrow(new CoverLetterGenerationException("Gemini is not configured."));

        GenerationRequest result = generationRequestService.create(request, owner);

        assertThat(result.getStatus()).isEqualTo(GenerationStatus.FAILED);
        assertThat(result.getErrorMessage()).isEqualTo("Gemini is not configured.");
        assertThat(result.getProvider()).isEqualTo("Google Gemini");
        verifyNoInteractions(coverLetterDao);
    }

    @Test
    void unknownProviderIdIsRejectedBeforeAnythingElseIsResolved() {
        GenerationRequestCreateRequest request = validRequest();
        UUID unknownId = UUID.randomUUID();
        request.setProviderId(unknownId);
        when(providerDao.getById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> generationRequestService.create(request, owner))
                .isInstanceOf(de.jeb.japp.commons.exceptions.ai.AiProviderNotFoundException.class);

        verifyNoInteractions(jobDao, cvDao, coverLetterDao);
    }
}
