package de.jeb.japp.repositories;

import de.jeb.japp.model.cv.CVDocument;
import de.jeb.japp.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CVRepository extends JpaRepository<CVDocument, UUID> {
    public List<CVDocument> findByOwner(User user);
}
