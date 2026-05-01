package de.jeb.japp.security.service;

import de.jeb.japp.dao.user.UserDao;
import de.jeb.japp.model.user.User;
import de.jeb.japp.model.user.UserRole;
import de.jeb.japp.model.user.dto.AuthResponse;
import de.jeb.japp.model.user.dto.LoginRequest;
import de.jeb.japp.model.user.dto.RegisterRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthServiceInterface {

    private final UserDao userDao;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthServiceImpl(UserDao repo, PasswordEncoder encoder, JwtService jwt) {
        this.userDao = repo;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    @Override
    public AuthResponse login(LoginRequest req) {

        Optional<User> optUser = userDao.getUserByEmail(req.getEmail());
        if (optUser.isPresent()) {
            User user = optUser.get();
            if (!encoder.matches(
                    req.getPassword(),
                    user.getPasswordHash()
            )) {
                throw new RuntimeException("Invalid credentials");
            }

            return new AuthResponse(
                    jwt.generateToken(user.getEmail())
            );
        } else {
            throw new RuntimeException("user not found with email: " + req.getEmail());
        }

    }

    @Override
    public AuthResponse register(RegisterRequest registerRequest) {
        if (userDao.getUserByEmail(registerRequest.getEmail()).isPresent()) {
            throw new RuntimeException("Email already used");
        }

        User user = new User();
        user.setFullName(registerRequest.getFullName());
        user.setEmail(registerRequest.getEmail());
        user.setPasswordHash(
                encoder.encode(registerRequest.getPassword())
        );
        user.setRole(UserRole.USER);
        return new AuthResponse(jwt.generateToken(userDao.registerUser(user).getEmail()));

    }
}
