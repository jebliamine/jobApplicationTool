package de.jeb.japp.generation.service.provider;

import de.jeb.japp.model.generation.GenerationProvider;
import org.springframework.stereotype.Service;

/**
 * Deterministic, no external AI call: templates the Job/CV metadata into a
 * cover letter body. The default {@link CoverLetterGenerationProvider} —
 * used whenever no provider is explicitly selected, and requires no external
 * configuration. Fails when the job has no description, since a real
 * generator would have nothing to generate from either — this is the same
 * failure behavior the previous inline implementation had, just relocated
 * here.
 */
@Service
public class PlaceholderCoverLetterGenerationProvider implements CoverLetterGenerationProvider {

    private static final int DESCRIPTION_EXCERPT_LENGTH = 400;
    private static final String MODEL = "deterministic-v1";

    @Override
    public GenerationProvider id() {
        return GenerationProvider.PLACEHOLDER;
    }

    @Override
    public String model() {
        return MODEL;
    }

    @Override
    public GenerationResult generate(GenerationInput input) {
        if (input.jobDescription() == null || input.jobDescription().isBlank()) {
            throw new CoverLetterGenerationException("The selected job has no description to generate from.");
        }

        String cvReference = input.cvTitle() != null ? " and my CV \"" + input.cvTitle() + "\"" : "";
        String description = input.jobDescription().trim();
        String descriptionExcerpt = description.length() > DESCRIPTION_EXCERPT_LENGTH
                ? description.substring(0, DESCRIPTION_EXCERPT_LENGTH).trim() + "…"
                : description;

        String content = "Dear Hiring Team at " + input.companyName() + ",\n\n"
                + "I am writing to express my interest in the " + input.jobTitle() + " position. "
                + "Based on the role description" + cvReference
                + ", I believe my background aligns well with what you are looking for:\n\n"
                + "\"" + descriptionExcerpt + "\"\n\n"
                + "I would welcome the opportunity to discuss how I can contribute to your team.\n\n"
                + "Sincerely,\n" + input.applicantName()
                + "\n\n[This is a placeholder cover letter generated without an AI provider.]";

        return new GenerationResult(content);
    }
}
