package de.jeb.japp.generation.service;

import de.jeb.japp.commons.exceptions.cv.CVAccessDeniedException;
import de.jeb.japp.commons.exceptions.generation.CoverLetterGenerationException;
import de.jeb.japp.commons.exceptions.generation.GenerationRequestAccessDeniedException;
import de.jeb.japp.commons.exceptions.generation.GenerationRequestNotFoundException;
import de.jeb.japp.commons.exceptions.generation.GenerationRequestValidationException;
import de.jeb.japp.commons.exceptions.job.JobAccessDeniedException;
import de.jeb.japp.dao.coverletter.CoverLetterDao;
import de.jeb.japp.dao.cv.CVDao;
import de.jeb.japp.dao.generation.GenerationRequestDao;
import de.jeb.japp.dao.job.JobDao;
import de.jeb.japp.generation.service.provider.CoverLetterGenerationProvider;
import de.jeb.japp.generation.service.provider.CoverLetterGenerationProviderRegistry;
import de.jeb.japp.generation.service.provider.GenerationResult;
import de.jeb.japp.model.company.Company;
import de.jeb.japp.model.coverLetter.CoverLetter;
import de.jeb.japp.model.cv.CVDocument;
import de.jeb.japp.model.generation.GenerationProvider;
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
    private CoverLetterGenerationProviderRegistry providerRegistry;
    @Mock
    private CoverLetterGenerationProvider placeholderProvider;
    @Mock
    private CoverLetterGenerationProvider geminiProvider;

    private GenerationRequestService generationRequestService;

    private User owner;
    private User otherUser;
    private User admin;
    private Company company;
    private Job job;
    private CVDocument cv;

    @BeforeEach
    void setUp() {
        generationRequestService =
                new GenerationRequestService(generationRequestDao, coverLetterDao, jobDao, cvDao, providerRegistry);

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

        // saveGenerationRequest is called repeatedly through process(); echo back whatever is passed in.
        lenient().when(generationRequestDao.saveGenerationRequest(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Requests without an explicit provider default to PLACEHOLDER (see GenerationRequestService#create).
        lenient().when(providerRegistry.resolve(GenerationProvider.PLACEHOLDER)).thenReturn(placeholderProvider);
        lenient().when(providerRegistry.resolve(GenerationProvider.GEMINI)).thenReturn(geminiProvider);
        lenient().when(placeholderProvider.id()).thenReturn(GenerationProvider.PLACEHOLDER);
        lenient().when(placeholderProvider.model()).thenReturn("deterministic-v1");
        lenient().when(geminiProvider.id()).thenReturn(GenerationProvider.GEMINI);
        lenient().when(geminiProvider.model()).thenReturn("gemini-2.0-flash");

        // Default: the resolved provider succeeds. Individual tests override this to exercise failure handling.
        lenient().when(placeholderProvider.generate(any()))
                .thenReturn(new GenerationResult(
                        "Generated cover letter mentioning " + job.getTitle() + " at " + company.getName() + "."));
        lenient().when(geminiProvider.generate(any()))
                .thenReturn(new GenerationResult(
                        "Gemini cover letter mentioning " + job.getTitle() + " at " + company.getName() + "."));
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

        ArgumentCaptor<de.jeb.japp.generation.service.provider.GenerationInput> captor =
                ArgumentCaptor.forClass(de.jeb.japp.generation.service.provider.GenerationInput.class);
        verify(placeholderProvider).generate(captor.capture());
        assertThat(captor.getValue().cvText()).isEqualTo(cv.getExtractedText());
    }

    @Test
    void serviceCallsTheProviderAndReachesCompletedWhenItSucceeds() {
        GenerationRequestCreateRequest request = validRequest();
        when(jobDao.getJobById(request.getJobId())).thenReturn(Optional.of(job));
        when(cvDao.getCVById(request.getCvDocumentId())).thenReturn(Optional.of(cv));

        GenerationRequest result = generationRequestService.create(request, owner);

        verify(placeholderProvider).generate(any());
        assertThat(result.getStatus()).isEqualTo(GenerationStatus.COMPLETED);
        assertThat(result.getStartedAt()).isNotNull();
        assertThat(result.getCompletedAt()).isNotNull();
        assertThat(result.getErrorMessage()).isNull();
    }

    @Test
    void aCoverLetterIsCreatedFromTheProvidersResultOnSuccess() {
        GenerationRequestCreateRequest request = validRequest();
        when(jobDao.getJobById(request.getJobId())).thenReturn(Optional.of(job));
        when(cvDao.getCVById(request.getCvDocumentId())).thenReturn(Optional.of(cv));
        when(placeholderProvider.generate(any())).thenReturn(new GenerationResult("Text from the provider."));

        generationRequestService.create(request, owner);

        ArgumentCaptor<CoverLetter> captor = ArgumentCaptor.forClass(CoverLetter.class);
        verify(coverLetterDao).saveCoverLetter(captor.capture());
        CoverLetter savedCoverLetter = captor.getValue();

        assertThat(savedCoverLetter.getOwner()).isEqualTo(owner);
        assertThat(savedCoverLetter.getGenerationRequest()).isNotNull();
        // The stored text must come straight from the provider's result — proves the
        // service depends on the abstraction rather than generating content itself.
        assertThat(savedCoverLetter.getResultText()).isEqualTo("Text from the provider.");
    }

    @Test
    void providerFailureResultsInFailedWithTheErrorMessageStored() {
        GenerationRequestCreateRequest request = validRequest();
        when(jobDao.getJobById(request.getJobId())).thenReturn(Optional.of(job));
        when(cvDao.getCVById(request.getCvDocumentId())).thenReturn(Optional.of(cv));
        when(placeholderProvider.generate(any()))
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
    void defaultsToPlaceholderWhenProviderIsOmitted() {
        GenerationRequestCreateRequest request = validRequest();
        when(jobDao.getJobById(request.getJobId())).thenReturn(Optional.of(job));
        when(cvDao.getCVById(request.getCvDocumentId())).thenReturn(Optional.of(cv));

        GenerationRequest result = generationRequestService.create(request, owner);

        verify(providerRegistry).resolve(GenerationProvider.PLACEHOLDER);
        verify(placeholderProvider).generate(any());
        verify(geminiProvider, never()).generate(any());
        assertThat(result.getProvider()).isEqualTo("PLACEHOLDER");
        assertThat(result.getModel()).isEqualTo("deterministic-v1");
    }

    @Test
    void selectsPlaceholderWhenExplicitlyRequested() {
        GenerationRequestCreateRequest request = validRequest();
        request.setProvider(GenerationProvider.PLACEHOLDER);
        when(jobDao.getJobById(request.getJobId())).thenReturn(Optional.of(job));
        when(cvDao.getCVById(request.getCvDocumentId())).thenReturn(Optional.of(cv));

        GenerationRequest result = generationRequestService.create(request, owner);

        verify(providerRegistry).resolve(GenerationProvider.PLACEHOLDER);
        assertThat(result.getProvider()).isEqualTo("PLACEHOLDER");
    }

    @Test
    void selectsGeminiWhenRequested() {
        GenerationRequestCreateRequest request = validRequest();
        request.setProvider(GenerationProvider.GEMINI);
        when(jobDao.getJobById(request.getJobId())).thenReturn(Optional.of(job));
        when(cvDao.getCVById(request.getCvDocumentId())).thenReturn(Optional.of(cv));

        GenerationRequest result = generationRequestService.create(request, owner);

        verify(providerRegistry).resolve(GenerationProvider.GEMINI);
        verify(geminiProvider).generate(any());
        verify(placeholderProvider, never()).generate(any());
        assertThat(result.getProvider()).isEqualTo("GEMINI");
        assertThat(result.getModel()).isEqualTo("gemini-2.0-flash");
        assertThat(result.getStatus()).isEqualTo(GenerationStatus.COMPLETED);
    }

    @Test
    void geminiProviderFailureResultsInFailedGenerationRequest() {
        GenerationRequestCreateRequest request = validRequest();
        request.setProvider(GenerationProvider.GEMINI);
        when(jobDao.getJobById(request.getJobId())).thenReturn(Optional.of(job));
        when(cvDao.getCVById(request.getCvDocumentId())).thenReturn(Optional.of(cv));
        when(geminiProvider.generate(any()))
                .thenThrow(new CoverLetterGenerationException("Gemini is not configured."));

        GenerationRequest result = generationRequestService.create(request, owner);

        assertThat(result.getStatus()).isEqualTo(GenerationStatus.FAILED);
        assertThat(result.getErrorMessage()).isEqualTo("Gemini is not configured.");
        // Provider/model metadata is still recorded even though generation failed.
        assertThat(result.getProvider()).isEqualTo("GEMINI");
        verifyNoInteractions(coverLetterDao);
    }
}
