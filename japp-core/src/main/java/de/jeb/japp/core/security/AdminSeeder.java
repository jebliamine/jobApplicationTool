package de.jeb.japp.core.security;

import de.jeb.japp.model.user.User;
import de.jeb.japp.model.user.UserRole;
import de.jeb.japp.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository repo;
    private final PasswordEncoder encoder;

    public AdminSeeder(UserRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {

        if (repo.findUserByEmail("admin@japp.de").isEmpty()) {

            User admin = new User();
            admin.setEmail("admin@japp.de");
            admin.setPasswordHash(encoder.encode("admin"));
            admin.setRole(UserRole.ADMIN);
            repo.save(admin);
        }
    }
}