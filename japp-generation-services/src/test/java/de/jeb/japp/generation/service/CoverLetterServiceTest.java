package de.jeb.japp.generation.service;

import de.jeb.japp.commons.exceptions.coverletter.CoverLetterAccessDeniedException;
import de.jeb.japp.commons.exceptions.coverletter.CoverLetterNotFoundException;
import de.jeb.japp.commons.exceptions.coverletter.CoverLetterValidationException;
import de.jeb.japp.dao.coverletter.CoverLetterDao;
import de.jeb.japp.model.coverLetter.CoverLetter;
import de.jeb.japp.model.coverLetter.dto.CoverLetterUpdateRequest;
import de.jeb.japp.model.generation.GenerationRequest;
import de.jeb.japp.model.user.User;
import de.jeb.japp.model.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class CoverLetterServiceTest {

    @Mock
    private CoverLetterDao coverLetterDao;

    private CoverLetterService coverLetterService;

    private User owner;
    private User otherUser;
    private User admin;
    private GenerationRequest generationRequest;

    @BeforeEach
    void setUp() {
        coverLetterService = new CoverLetterService(coverLetterDao);

        owner = new User();
        owner.setId(UUID.randomUUID());
        owner.setRole(UserRole.USER);

        otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        otherUser.setRole(UserRole.USER);

        admin = new User();
        admin.setId(UUID.randomUUID());
        admin.setRole(UserRole.ADMIN);

        generationRequest = new GenerationRequest();
    }

    private CoverLetter coverLetterOwnedBy(User user) {
        CoverLetter coverLetter = new CoverLetter();
        coverLetter.setOwner(user);
        coverLetter.setGenerationRequest(generationRequest);
        coverLetter.setResultText("Dear Hiring Team, ...");
        return coverLetter;
    }

    @Test
    void ownerCanGetTheirOwnCoverLetter() {
        UUID id = UUID.randomUUID();
        CoverLetter coverLetter = coverLetterOwnedBy(owner);
        when(coverLetterDao.getCoverLetterById(id)).thenReturn(Optional.of(coverLetter));

        assertThat(coverLetterService.get(id, owner)).isEqualTo(coverLetter);
    }

    @Test
    void adminCanGetAnyCoverLetter() {
        UUID id = UUID.randomUUID();
        CoverLetter coverLetter = coverLetterOwnedBy(owner);
        when(coverLetterDao.getCoverLetterById(id)).thenReturn(Optional.of(coverLetter));

        assertThat(coverLetterService.get(id, admin)).isEqualTo(coverLetter);
    }

    @Test
    void userCannotAccessAnotherUsersGeneratedCoverLetter() {
        UUID id = UUID.randomUUID();
        CoverLetter coverLetter = coverLetterOwnedBy(owner);
        when(coverLetterDao.getCoverLetterById(id)).thenReturn(Optional.of(coverLetter));

        assertThatThrownBy(() -> coverLetterService.get(id, otherUser))
                .isInstanceOf(CoverLetterAccessDeniedException.class);
    }

    @Test
    void getThrowsNotFoundForMissingCoverLetter() {
        UUID id = UUID.randomUUID();
        when(coverLetterDao.getCoverLetterById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> coverLetterService.get(id, owner))
                .isInstanceOf(CoverLetterNotFoundException.class);
    }

    @Test
    void listReturnsAllCoverLettersForAdmin() {
        coverLetterService.list(admin);
        verify(coverLetterDao).getAllCoverLetters();
        verify(coverLetterDao, never()).getAllCoverLettersByOwner(any());
    }

    @Test
    void listReturnsOnlyOwnCoverLettersForRegularUser() {
        coverLetterService.list(owner);
        verify(coverLetterDao).getAllCoverLettersByOwner(owner);
        verify(coverLetterDao, never()).getAllCoverLetters();
    }

    @Test
    void listReturnsWhatDaoProvides() {
        when(coverLetterDao.getAllCoverLettersByOwner(owner)).thenReturn(List.of(coverLetterOwnedBy(owner)));

        List<CoverLetter> result = coverLetterService.list(owner);

        assertThat(result).hasSize(1);
    }

    @Test
    void ownerCanEditTheirOwnCoverLetter() {
        UUID id = UUID.randomUUID();
        CoverLetter coverLetter = coverLetterOwnedBy(owner);
        when(coverLetterDao.getCoverLetterById(id)).thenReturn(Optional.of(coverLetter));
        when(coverLetterDao.saveCoverLetter(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CoverLetterUpdateRequest request = new CoverLetterUpdateRequest();
        request.setResultText("Updated cover letter text.");

        CoverLetter updated = coverLetterService.update(id, request, owner);

        assertThat(updated.getResultText()).isEqualTo("Updated cover letter text.");
        assertThat(updated.getUpdatedAt()).isNotNull();
    }

    @Test
    void userCannotEditAnotherUsersCoverLetter() {
        UUID id = UUID.randomUUID();
        CoverLetter coverLetter = coverLetterOwnedBy(owner);
        when(coverLetterDao.getCoverLetterById(id)).thenReturn(Optional.of(coverLetter));

        CoverLetterUpdateRequest request = new CoverLetterUpdateRequest();
        request.setResultText("Malicious overwrite.");

        assertThatThrownBy(() -> coverLetterService.update(id, request, otherUser))
                .isInstanceOf(CoverLetterAccessDeniedException.class);

        verify(coverLetterDao, never()).saveCoverLetter(any());
    }

    @Test
    void updateRejectsBlankResultText() {
        UUID id = UUID.randomUUID();
        CoverLetter coverLetter = coverLetterOwnedBy(owner);
        when(coverLetterDao.getCoverLetterById(id)).thenReturn(Optional.of(coverLetter));

        CoverLetterUpdateRequest request = new CoverLetterUpdateRequest();
        request.setResultText("   ");

        assertThatThrownBy(() -> coverLetterService.update(id, request, owner))
                .isInstanceOf(CoverLetterValidationException.class);

        verify(coverLetterDao, never()).saveCoverLetter(any());
    }
}
