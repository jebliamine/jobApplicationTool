package de.jeb.japp.tag.service;

import de.jeb.japp.commons.exceptions.tag.TagAccessDeniedException;
import de.jeb.japp.commons.exceptions.tag.TagNotFoundException;
import de.jeb.japp.commons.exceptions.tag.TagValidationException;
import de.jeb.japp.dao.tag.TagDao;
import de.jeb.japp.model.tag.Tag;
import de.jeb.japp.model.tag.dto.TagRequest;
import de.jeb.japp.model.user.User;
import de.jeb.japp.model.user.UserRole;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/**
 * Tag is user-owned — each user manages their own tag vocabulary, applicable
 * to both Job and Application (see their {@code tags} collections). Same
 * service-layer ownership-check pattern as Company/Job/Application.
 * <p>
 * {@link #getOwnedByExactlyAll} is the cross-domain entry point JobService
 * and ApplicationService call to validate a set of tag ids before assigning
 * them — the Tag-domain analogue of CompanyService#getOwnedByExactly.
 */
@Service
public class TagService {

    private final TagDao tagDao;

    public TagService(TagDao tagDao) {
        this.tagDao = tagDao;
    }

    public Tag create(TagRequest request, User owner) {
        String name = validate(request);
        if (tagDao.existsByOwnerAndName(owner, name)) {
            throw new TagValidationException("A tag named \"" + name + "\" already exists.");
        }
        Tag tag = new Tag();
        tag.setOwner(owner);
        tag.setName(name);
        tag.setCreatedAt(LocalDateTime.now());
        return tagDao.saveTag(tag);
    }

    public Tag get(UUID id, User requester) {
        Tag tag = find(id);
        assertAccess(tag.getOwner(), requester);
        return tag;
    }

    public List<Tag> list(User requester) {
        return requester.getRole() == UserRole.ADMIN
                ? tagDao.getAllTags()
                : tagDao.getAllTagsByOwner(requester);
    }

    /** ADMIN gets the global count, matching {@link #list}'s ADMIN-sees-everything convention. */
    public long count(User requester) {
        return requester.getRole() == UserRole.ADMIN
                ? tagDao.countAll()
                : tagDao.countByOwner(requester);
    }

    public Tag rename(UUID id, TagRequest request, User requester) {
        Tag tag = get(id, requester);
        String name = validate(request);
        if (!name.equalsIgnoreCase(tag.getName()) && tagDao.existsByOwnerAndName(tag.getOwner(), name)) {
            throw new TagValidationException("A tag named \"" + name + "\" already exists.");
        }
        tag.setName(name);
        return tagDao.saveTag(tag);
    }

    public void delete(UUID id, User requester) {
        Tag tag = get(id, requester);
        tagDao.deleteTag(tag.getId());
    }

    /**
     * Strict ownership check across a whole batch at once (no admin bypass) — used by
     * JobService/ApplicationService to verify every requested tag id belongs to the
     * job's/application's actual owner before assigning them, regardless of who
     * (including an admin) is performing the request.
     */
    public List<Tag> getOwnedByExactlyAll(List<UUID> ids, User owner) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<Tag> found = tagDao.getByIdsAndOwner(ids, owner);
        if (found.size() != new HashSet<>(ids).size()) {
            throw new TagNotFoundException("One or more tags were not found.");
        }
        return found;
    }

    private Tag find(UUID id) {
        return tagDao.getTagById(id).orElseThrow(() -> new TagNotFoundException("Tag not found."));
    }

    private String validate(TagRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new TagValidationException("A tag name is required.");
        }
        return request.getName().trim();
    }

    private void assertAccess(User owner, User requester) {
        boolean isOwner = owner != null && owner.getId().equals(requester.getId());
        boolean isAdmin = requester.getRole() == UserRole.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new TagAccessDeniedException("You do not have access to this tag.");
        }
    }
}
