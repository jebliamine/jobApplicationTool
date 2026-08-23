package de.jeb.japp.generation.service.provider.placeholder;

import de.jeb.japp.ai.service.ResolvedProviderConfig;
import de.jeb.japp.commons.exceptions.generation.CoverLetterGenerationException;
import de.jeb.japp.generation.service.provider.CoverLetterGenerationAdapter;
import de.jeb.japp.generation.service.provider.GenerationInput;
import de.jeb.japp.generation.service.provider.GenerationResult;
import de.jeb.japp.model.ai.AdapterType;
import org.springframework.stereotype.Service;

/**
 * Deterministic, no external AI call: templates the Job/CV metadata into a
 * cover letter body. Used whenever the built-in Placeholder instance is
 * selected — requires no external configuration. Fails when the job has no
 * description, since a real generator would have nothing to generate from
 * either.
 */
@Service
public class PlaceholderCoverLetterGenerationAdapter implements CoverLetterGenerationAdapter {

    private static final int DESCRIPTION_EXCERPT_LENGTH = 400;

    @Override
    public AdapterType type() {
        return AdapterType.PLACEHOLDER;
    }

    @Override
    public GenerationResult generate(ResolvedProviderConfig config, GenerationInput input) {
        if (input.jobDescription() == null || input.jobDescription().isBlank()) {
            throw new CoverLetterGenerationException("The selected job has no description to generate from.");
        }

        String cvReference = input.cvText() != null && !input.cvText().isBlank()
                ? " and my CV"
                : (input.cvTitle() != null ? " and my CV \"" + input.cvTitle() + "\"" : "");
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
