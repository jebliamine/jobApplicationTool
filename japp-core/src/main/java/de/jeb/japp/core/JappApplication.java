package de.jeb.japp.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;


@SpringBootApplication(scanBasePackages = "de.jeb.japp")
@EnableJpaRepositories(basePackages = "de.jeb.japp")
@EntityScan(basePackages = "de.jeb.japp.model")
public class JappApplication {

    public static void main(String[] args) {
        loadDotenvIntoSystemProperties();
        SpringApplication.run(JappApplication.class, args);
    }

    /**
     * Local dev only: application.yml's {@code ${VAR:}} placeholders (AI/job-search provider
     * credentials) are documented as coming from the gitignored root .env, but nothing else in
     * this project loads that file into the process — launching via an IDE run configuration or
     * plain {@code java -jar} only sees real OS environment variables, so a value that only exists
     * in .env silently resolves to blank. This copies each .env entry into a JVM system property
     * (a source Spring's Environment already reads) before the context starts, without overriding
     * a real OS-level env var of the same name. Searches upward from the working directory since
     * an IDE run configuration's working directory is typically a module folder, not the repo root
     * where .env actually lives.
     */
    private static void loadDotenvIntoSystemProperties() {
        Path dir = Path.of("").toAbsolutePath();
        for (int i = 0; i < 6 && dir != null; i++, dir = dir.getParent()) {
            Path candidate = dir.resolve(".env");
            if (Files.isRegularFile(candidate)) {
                applyDotenv(candidate);
                return;
            }
        }
    }

    private static void applyDotenv(Path envFile) {
        List<String> lines;
        try {
            lines = Files.readAllLines(envFile);
        } catch (IOException e) {
            return;
        }
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }
            int separator = trimmed.indexOf('=');
            if (separator <= 0) {
                continue;
            }
            String key = trimmed.substring(0, separator).trim();
            String value = trimmed.substring(separator + 1).trim();
            if (System.getenv(key) == null && System.getProperty(key) == null) {
                System.setProperty(key, value);
            }
        }
    }

}
