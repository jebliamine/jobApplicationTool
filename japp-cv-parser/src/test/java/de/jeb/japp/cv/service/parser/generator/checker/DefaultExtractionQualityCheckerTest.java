package de.jeb.japp.cv.service.parser.generator.checker;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultExtractionQualityCheckerTest {

    private final DefaultExtractionQualityChecker checker = new DefaultExtractionQualityChecker();

    @Test
    void nullTextIsEmpty() {
        assertThat(checker.check(null)).isEqualTo(ExtractionQuality.EMPTY);
    }

    @Test
    void blankTextIsEmpty() {
        assertThat(checker.check("   \n\t  ")).isEqualTo(ExtractionQuality.EMPTY);
    }

    @Test
    void veryShortTextIsEmpty() {
        assertThat(checker.check("hello")).isEqualTo(ExtractionQuality.EMPTY);
    }

    @Test
    void lowWordCountIsPoor() {
        String text = "word ".repeat(30);
        assertThat(checker.check(text)).isEqualTo(ExtractionQuality.POOR);
    }

    @Test
    void garbledCharactersArePoor() {
        String garbage = "@#$%^&*()_+-=[]{}|;:,.<>?/~`".repeat(15);
        assertThat(checker.check(garbage)).isEqualTo(ExtractionQuality.POOR);
    }

    @Test
    void repeatedSingleCharacterIsPoor() {
        String text = "a".repeat(500) + " word word word word word word word word word word";
        assertThat(checker.check(text)).isEqualTo(ExtractionQuality.POOR);
    }

    @Test
    void realisticCvTextIsGood() {
        String text = """
                John Doe
                Senior Software Engineer

                Summary
                Experienced backend engineer with a decade of experience building distributed systems,
                REST APIs, and cloud native platforms using Java, Spring Boot, and PostgreSQL.

                Experience
                Acme Corp - Senior Software Engineer (2019 - Present)
                - Designed and implemented microservices handling millions of requests per day.
                - Led migration from a monolithic architecture to a service oriented architecture.
                - Mentored junior engineers and conducted code reviews across multiple teams.

                Globex Inc - Software Engineer (2015 - 2019)
                - Built internal tooling for continuous integration and deployment pipelines.
                - Collaborated with product managers to define and ship new features.

                Education
                Bachelor of Science in Computer Science, State University, 2015

                Skills
                Java, Spring Boot, PostgreSQL, Docker, Kubernetes, AWS, REST APIs, Git
                """;
        assertThat(checker.check(text)).isEqualTo(ExtractionQuality.GOOD);
    }
}
