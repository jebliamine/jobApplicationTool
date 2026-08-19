package de.jeb.japp.generation.service;

import de.jeb.japp.commons.exceptions.cv.CVAccessDeniedException;
import de.jeb.japp.commons.exceptions.generation.GenerationRequestAccessDeniedException;
import de.jeb.japp.commons.exceptions.generation.GenerationRequestNotFoundException;
import de.jeb.japp.commons.exceptions.generation.GenerationRequestValidationException;
import de.jeb.japp.commons.exceptions.job.JobAccessDeniedException;
import de.jeb.japp.dao.coverletter.CoverLetterDao;
import de.jeb.japp.dao.cv.CVDao;
import de.jeb.japp.dao.generation.GenerationRequestDao;
import de.jeb.japp.dao.job.JobDao;
import de.jeb.japp.model.company.Company;
import de.jeb.japp.model.coverLetter.CoverLetter;
import de.jeb.japp.model.cv.CVDocument;
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

    private GenerationRequestService generationRequestService;

    private User owner;
    private User otherUser;
    private User admin;
    private Company company;
    private Job job;
    private CVDocument cv;

    @BeforeEach
    void setUp() {
        generationRequestService = new GenerationRequestService(generationRequestDao, coverLetterDao, jobDao, cvDao);

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

        // saveGenerationRequest is called repeatedly through process(); echo back whatever is passed in.
        lenient().when(generationRequestDao.saveGenerationRequest(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
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
    }

    @Test
    void generationRequestReachesCompletedWithThePlaceholderGenerator() {
        GenerationRequestCreateRequest request = validRequest();
        when(jobDao.getJobById(request.getJobId())).thenReturn(Optional.of(job));
        when(cvDao.getCVById(request.getCvDocumentId())).thenReturn(Optional.of(cv));

        GenerationRequest result = generationRequestService.create(request, owner);

        assertThat(result.getStatus()).isEqualTo(GenerationStatus.COMPLETED);
        assertThat(result.getStartedAt()).isNotNull();
        assertThat(result.getCompletedAt()).isNotNull();
        assertThat(result.getErrorMessage()).isNull();
    }

    @Test
    void aCoverLetterIsCreatedFromASuccessfulGenerationRequest() {
        GenerationRequestCreateRequest request = validRequest();
        when(jobDao.getJobById(request.getJobId())).thenReturn(Optional.of(job));
        when(cvDao.getCVById(request.getCvDocumentId())).thenReturn(Optional.of(cv));

        generationRequestService.create(request, owner);

        ArgumentCaptor<CoverLetter> captor = ArgumentCaptor.forClass(CoverLetter.class);
        verify(coverLetterDao).saveCoverLetter(captor.capture());
        CoverLetter savedCoverLetter = captor.getValue();

        assertThat(savedCoverLetter.getOwner()).isEqualTo(owner);
        assertThat(savedCoverLetter.getGenerationRequest()).isNotNull();
        assertThat(savedCoverLetter.getResultText()).isNotBlank();
        assertThat(savedCoverLetter.getResultText()).contains(job.getTitle());
        assertThat(savedCoverLetter.getResultText()).contains(company.getName());
    }

    @Test
    void failedGenerationIsRepresentedCorrectly() {
        job.setDescription(" ");
        GenerationRequestCreateRequest request = validRequest();
        when(jobDao.getJobById(request.getJobId())).thenReturn(Optional.of(job));
        when(cvDao.getCVById(request.getCvDocumentId())).thenReturn(Optional.of(cv));

        GenerationRequest result = generationRequestService.create(request, owner);

        assertThat(result.getStatus()).isEqualTo(GenerationStatus.FAILED);
        assertThat(result.getErrorMessage()).isNotBlank();
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
}
