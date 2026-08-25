package de.jeb.japp.repositories;

import de.jeb.japp.model.tag.Tag;
import de.jeb.japp.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {
    List<Tag> findByOwner(User owner);

    List<Tag> findByIdInAndOwner(List<UUID> ids, User owner);

    boolean existsByOwnerAndNameIgnoreCase(User owner, String name);

    long countByOwner(User owner);
}
