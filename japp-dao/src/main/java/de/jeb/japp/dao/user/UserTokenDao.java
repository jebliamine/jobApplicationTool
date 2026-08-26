package de.jeb.japp.dao.user;

import de.jeb.japp.model.user.UserToken;
import de.jeb.japp.model.user.UserTokenType;
import de.jeb.japp.repositories.UserTokenRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserTokenDao {

    private final UserTokenRepository userTokenRepository;

    public UserTokenDao(UserTokenRepository userTokenRepository) {
        this.userTokenRepository = userTokenRepository;
    }

    public UserToken save(UserToken token) {
        return userTokenRepository.save(token);
    }

    public Optional<UserToken> getByTokenAndType(String token, UserTokenType type) {
        return userTokenRepository.findByTokenAndType(token, type);
    }
}
