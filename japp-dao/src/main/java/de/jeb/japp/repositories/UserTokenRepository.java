package de.jeb.japp.repositories;

import de.jeb.japp.model.user.UserToken;
import de.jeb.japp.model.user.UserTokenType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserTokenRepository extends JpaRepository<UserToken, UUID> {
    Optional<UserToken> findByTokenAndType(String token, UserTokenType type);
}
