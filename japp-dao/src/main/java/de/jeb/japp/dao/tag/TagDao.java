package de.jeb.japp.dao.tag;

import de.jeb.japp.model.tag.Tag;
import de.jeb.japp.model.user.User;
import de.jeb.japp.repositories.TagRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class TagDao {

    private final TagRepository tagRepository;

    public TagDao(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    public List<Tag> getAllTagsByOwner(User owner) {
        return tagRepository.findByOwner(owner);
    }

    public Optional<Tag> getTagById(UUID id) {
        return tagRepository.findById(id);
    }

    /** Returns only the tags among {@code ids} that both exist and belong to {@code owner}. */
    public List<Tag> getByIdsAndOwner(List<UUID> ids, User owner) {
        return tagRepository.findByIdInAndOwner(ids, owner);
    }

    public boolean existsByOwnerAndName(User owner, String name) {
        return tagRepository.existsByOwnerAndNameIgnoreCase(owner, name);
    }

    public Tag saveTag(Tag tag) {
        return tagRepository.save(tag);
    }

    public void deleteTag(UUID id) {
        tagRepository.deleteById(id);
    }

    public long countAll() {
        return tagRepository.count();
    }

    public long countByOwner(User owner) {
        return tagRepository.countByOwner(owner);
    }
}
