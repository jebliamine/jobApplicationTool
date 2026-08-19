package de.jeb.japp.generation.service;

import de.jeb.japp.commons.exceptions.coverletter.CoverLetterAccessDeniedException;
import de.jeb.japp.commons.exceptions.coverletter.CoverLetterNotFoundException;
import de.jeb.japp.commons.exceptions.coverletter.CoverLetterValidationException;
import de.jeb.japp.dao.coverletter.CoverLetterDao;
import de.jeb.japp.model.coverLetter.CoverLetter;
import de.jeb.japp.model.coverLetter.dto.CoverLetterUpdateRequest;
import de.jeb.japp.model.user.User;
import de.jeb.japp.model.user.UserRole;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * CoverLetters are only created as a result of generation (see
 * GenerationRequestService) — this service only covers viewing, listing, and
 * editing the generated text. Same service-layer ownership-check pattern as
 * CVDocument/Job/Company/Application.
 */
@Service
public class CoverLetterService {

    private final CoverLetterDao coverLetterDao;

    public CoverLetterService(CoverLetterDao coverLetterDao) {
        this.coverLetterDao = coverLetterDao;
    }

    public CoverLetter get(UUID id, User requester) {
        CoverLetter coverLetter = find(id);
        assertAccess(coverLetter.getOwner(), requester);
        return coverLetter;
    }

    /** archived=false is the default/normal view; archived=true is the explicit archived view. */
    public List<CoverLetter> list(User requester, boolean archived) {
        return requester.getRole() == UserRole.ADMIN
                ? coverLetterDao.getAllCoverLetters(archived)
                : coverLetterDao.getAllCoverLettersByOwner(requester, archived);
    }

    public CoverLetter update(UUID id, CoverLetterUpdateRequest request, User requester) {
        CoverLetter coverLetter = get(id, requester);
        validate(request);
        coverLetter.setResultText(request.getResultText().trim());
        coverLetter.setUpdatedAt(LocalDateTime.now());
        return coverLetterDao.saveCoverLetter(coverLetter);
    }

    /** USER can archive their own CoverLetters; ADMIN can archive any. Preserves the GenerationRequest/Application relationship. */
    public CoverLetter archive(UUID id, User requester) {
        CoverLetter coverLetter = get(id, requester);
        coverLetter.setArchived(true);
        coverLetter.setUpdatedAt(LocalDateTime.now());
        return coverLetterDao.saveCoverLetter(coverLetter);
    }

    public CoverLetter unarchive(UUID id, User requester) {
        CoverLetter coverLetter = get(id, requester);
        coverLetter.setArchived(false);
        coverLetter.setUpdatedAt(LocalDateTime.now());
        return coverLetterDao.saveCoverLetter(coverLetter);
    }

    /**
     * Permanent deletion — ADMIN only, regardless of ownership. A USER can
     * never permanently delete a CoverLetter, even their own (they can only
     * archive it). The GenerationRequest is never touched; the Application
     * FK is ON DELETE SET NULL at the database level.
     */
    public void delete(UUID id, User requester) {
        CoverLetter coverLetter = get(id, requester);
        if (requester.getRole() != UserRole.ADMIN) {
            throw new CoverLetterAccessDeniedException("Only an administrator can permanently delete a cover letter.");
        }
        coverLetterDao.deleteCoverLetter(coverLetter.getId());
    }

    private CoverLetter find(UUID id) {
        return coverLetterDao.getCoverLetterById(id)
                .orElseThrow(() -> new CoverLetterNotFoundException("Cover letter not found."));
    }

    private void validate(CoverLetterUpdateRequest request) {
        if (request.getResultText() == null || request.getResultText().isBlank()) {
            throw new CoverLetterValidationException("Cover letter content cannot be empty.");
        }
    }

    private void assertAccess(User owner, User requester) {
        boolean isOwner = owner != null && owner.getId().equals(requester.getId());
        boolean isAdmin = requester.getRole() == UserRole.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new CoverLetterAccessDeniedException("You do not have access to this cover letter.");
        }
    }
}
