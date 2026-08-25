package de.jeb.japp.tag.service;

import de.jeb.japp.commons.exceptions.tag.TagAccessDeniedException;
import de.jeb.japp.commons.exceptions.tag.TagNotFoundException;
import de.jeb.japp.commons.exceptions.tag.TagValidationException;
import de.jeb.japp.dao.tag.TagDao;
import de.jeb.japp.model.tag.Tag;
import de.jeb.japp.model.tag.dto.TagRequest;
import de.jeb.japp.model.user.User;
import de.jeb.japp.model.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagDao tagDao;

    private TagService tagService;

    private User owner;
    private User otherUser;
    private User admin;

    @BeforeEach
    void setUp() {
        tagService = new TagService(tagDao);

        owner = new User();
        owner.setId(UUID.randomUUID());
        owner.setRole(UserRole.USER);

        otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        otherUser.setRole(UserRole.USER);

        admin = new User();
        admin.setId(UUID.randomUUID());
        admin.setRole(UserRole.ADMIN);

        lenient().when(tagDao.saveTag(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private TagRequest request(String name) {
        TagRequest request = new TagRequest();
        request.setName(name);
        return request;
    }

    private Tag tagOwnedBy(User user, String name) {
        Tag tag = new Tag();
        tag.setOwner(user);
        tag.setName(name);
        ReflectionTestUtils.setField(tag, "id", UUID.randomUUID());
        return tag;
    }

    @Test
    void createSetsOwnerAndTrimsTheName() {
        Tag created = tagService.create(request("  Remote  "), owner);

        assertThat(created.getOwner()).isEqualTo(owner);
        assertThat(created.getName()).isEqualTo("Remote");
        assertThat(created.getCreatedAt()).isNotNull();
    }

    @Test
    void createRejectsABlankName() {
        assertThatThrownBy(() -> tagService.create(request("   "), owner))
                .isInstanceOf(TagValidationException.class);
    }

    @Test
    void createRejectsADuplicateNameForTheSameOwner() {
        when(tagDao.existsByOwnerAndName(owner, "Remote")).thenReturn(true);

        assertThatThrownBy(() -> tagService.create(request("Remote"), owner))
                .isInstanceOf(TagValidationException.class);
    }

    @Test
    void ownerCanGetTheirOwnTag() {
        UUID id = UUID.randomUUID();
        Tag tag = tagOwnedBy(owner, "Remote");
        when(tagDao.getTagById(id)).thenReturn(Optional.of(tag));

        assertThat(tagService.get(id, owner)).isEqualTo(tag);
    }

    @Test
    void adminCanGetAnyTag() {
        UUID id = UUID.randomUUID();
        Tag tag = tagOwnedBy(owner, "Remote");
        when(tagDao.getTagById(id)).thenReturn(Optional.of(tag));

        assertThat(tagService.get(id, admin)).isEqualTo(tag);
    }

    @Test
    void otherUserCannotGetSomeoneElsesTag() {
        UUID id = UUID.randomUUID();
        Tag tag = tagOwnedBy(owner, "Remote");
        when(tagDao.getTagById(id)).thenReturn(Optional.of(tag));

        assertThatThrownBy(() -> tagService.get(id, otherUser))
                .isInstanceOf(TagAccessDeniedException.class);
    }

    @Test
    void getThrowsNotFoundForMissingTag() {
        UUID id = UUID.randomUUID();
        when(tagDao.getTagById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tagService.get(id, owner))
                .isInstanceOf(TagNotFoundException.class);
    }

    @Test
    void listReturnsAllTagsForAdmin() {
        tagService.list(admin);
        verify(tagDao).getAllTags();
        verify(tagDao, never()).getAllTagsByOwner(any());
    }

    @Test
    void listReturnsOnlyOwnTagsForRegularUser() {
        tagService.list(owner);
        verify(tagDao).getAllTagsByOwner(owner);
        verify(tagDao, never()).getAllTags();
    }

    @Test
    void renameUpdatesTheNameWhenNotADuplicate() {
        UUID id = UUID.randomUUID();
        Tag tag = tagOwnedBy(owner, "Remote");
        when(tagDao.getTagById(id)).thenReturn(Optional.of(tag));
        when(tagDao.existsByOwnerAndName(owner, "Remote job")).thenReturn(false);

        Tag renamed = tagService.rename(id, request("Remote job"), owner);

        assertThat(renamed.getName()).isEqualTo("Remote job");
    }

    @Test
    void renameToItsOwnCurrentNameIsAllowed() {
        UUID id = UUID.randomUUID();
        Tag tag = tagOwnedBy(owner, "Remote");
        when(tagDao.getTagById(id)).thenReturn(Optional.of(tag));

        Tag renamed = tagService.rename(id, request("Remote"), owner);

        assertThat(renamed.getName()).isEqualTo("Remote");
        verify(tagDao, never()).existsByOwnerAndName(any(), any());
    }

    @Test
    void renameRejectsADuplicateOfAnotherExistingTag() {
        UUID id = UUID.randomUUID();
        Tag tag = tagOwnedBy(owner, "Remote");
        when(tagDao.getTagById(id)).thenReturn(Optional.of(tag));
        when(tagDao.existsByOwnerAndName(owner, "Referral")).thenReturn(true);

        assertThatThrownBy(() -> tagService.rename(id, request("Referral"), owner))
                .isInstanceOf(TagValidationException.class);
    }

    @Test
    void deleteSucceedsForOwner() {
        UUID id = UUID.randomUUID();
        Tag tag = tagOwnedBy(owner, "Remote");
        when(tagDao.getTagById(id)).thenReturn(Optional.of(tag));

        tagService.delete(id, owner);

        verify(tagDao).deleteTag(tag.getId());
    }

    @Test
    void otherUserCannotDeleteSomeoneElsesTag() {
        UUID id = UUID.randomUUID();
        Tag tag = tagOwnedBy(owner, "Remote");
        when(tagDao.getTagById(id)).thenReturn(Optional.of(tag));

        assertThatThrownBy(() -> tagService.delete(id, otherUser))
                .isInstanceOf(TagAccessDeniedException.class);

        verify(tagDao, never()).deleteTag(any());
    }

    @Test
    void getOwnedByExactlyAllReturnsEmptyForAnEmptyOrNullList() {
        assertThat(tagService.getOwnedByExactlyAll(List.of(), owner)).isEmpty();
        assertThat(tagService.getOwnedByExactlyAll(null, owner)).isEmpty();
        verify(tagDao, never()).getByIdsAndOwner(any(), any());
    }

    @Test
    void getOwnedByExactlyAllReturnsAllTagsWhenEveryIdIsOwned() {
        Tag remote = tagOwnedBy(owner, "Remote");
        Tag referral = tagOwnedBy(owner, "Referral");
        List<UUID> ids = List.of(remote.getId(), referral.getId());
        when(tagDao.getByIdsAndOwner(ids, owner)).thenReturn(List.of(remote, referral));

        List<Tag> result = tagService.getOwnedByExactlyAll(ids, owner);

        assertThat(result).containsExactlyInAnyOrder(remote, referral);
    }

    @Test
    void getOwnedByExactlyAllRejectsWhenAnIdIsMissingOrNotOwned() {
        Tag remote = tagOwnedBy(owner, "Remote");
        UUID foreignId = UUID.randomUUID();
        List<UUID> ids = List.of(remote.getId(), foreignId);
        // Only the owned tag comes back — the foreign/nonexistent id silently drops out of the query result.
        when(tagDao.getByIdsAndOwner(ids, owner)).thenReturn(List.of(remote));

        assertThatThrownBy(() -> tagService.getOwnedByExactlyAll(ids, owner))
                .isInstanceOf(TagNotFoundException.class);
    }
}
