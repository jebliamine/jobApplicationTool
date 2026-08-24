package de.jeb.japp.generation.service.provider.placeholder;

import de.jeb.japp.ai.service.ResolvedProviderConfig;
import de.jeb.japp.commons.exceptions.generation.CvProfileGenerationException;
import de.jeb.japp.generation.service.provider.CvProfileExtractionAdapter;
import de.jeb.japp.generation.service.provider.CvProfileExtractionInput;
import de.jeb.japp.generation.service.provider.CvProfileExtractionResult;
import de.jeb.japp.model.ai.AdapterType;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Deterministic, no external AI call: there is no real structured extraction
 * to perform without a model, so this is used whenever the built-in
 * Placeholder instance is selected, and honestly says so in the summary
 * rather than fabricating plausible-looking name/experience data.
 */
@Service
public class PlaceholderCvProfileExtractionAdapter implements CvProfileExtractionAdapter {

    @Override
    public AdapterType type() {
        return AdapterType.PLACEHOLDER;
    }

    @Override
    public CvProfileExtractionResult extract(ResolvedProviderConfig config, CvProfileExtractionInput input) {
        if (input.cvText() == null || input.cvText().isBlank()) {
            throw new CvProfileGenerationException("This CV has no extracted text to generate a profile from.");
        }

        return new CvProfileExtractionResult(
                null,
                "Configure a real AI provider to generate an actual profile summary. "
                        + "[This is a placeholder result generated without an AI provider.]",
                List.of());
    }
}
