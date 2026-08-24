package de.jeb.japp.core.security;

import de.jeb.japp.model.user.User;
import de.jeb.japp.model.user.UserRole;
import de.jeb.japp.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Seeds a single admin account on first startup (idempotent — checked by
 * email each run). Email/password come from spring.app.bootstrap-admin.* so
 * they can be overridden via BOOTSTRAP_ADMIN_EMAIL/BOOTSTRAP_ADMIN_PASSWORD
 * instead of the previous hardcoded admin@japp.de / "admin" credentials.
 */
@Component
public class AdminSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);
    private static final String DEFAULT_PASSWORD = "ChangeMe123!";

    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final String adminEmail;
    private final String adminPassword;

    public AdminSeeder(
            UserRepository repo,
            PasswordEncoder encoder,
            @Value("${spring.app.bootstrap-admin.email}") String adminEmail,
            @Value("${spring.app.bootstrap-admin.password}") String adminPassword) {
        this.repo = repo;
        this.encoder = encoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        if (repo.findUserByEmail(adminEmail).isEmpty()) {
            User admin = new User();
            admin.setEmail(adminEmail);
            admin.setPasswordHash(encoder.encode(adminPassword));
            admin.setRole(UserRole.ADMIN);
            admin.setCreatedAt(LocalDateTime.now());
            repo.save(admin);
            log.info("Seeded bootstrap admin account ({}).", adminEmail);
        }

        if (DEFAULT_PASSWORD.equals(adminPassword)) {
            log.warn(
                    "Bootstrap admin is using the default password. Set BOOTSTRAP_ADMIN_PASSWORD "
                            + "before running this outside a solo local dev environment.");
        }
    }
}