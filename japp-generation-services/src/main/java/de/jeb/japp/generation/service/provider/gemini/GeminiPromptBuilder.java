package de.jeb.japp.generation.service.provider.gemini;

import de.jeb.japp.generation.service.provider.GenerationInput;

/**
 * Builds the prompt sent to Gemini from {@link GenerationInput} only — no
 * invented CV content. When {@code cvText} (the CV's extracted, normalized
 * text) is present, it is included as the actual CV content; otherwise
 * {@code cvTitle} — the only CV information available in that case — is
 * presented as the document's title, never as if it were extracted content.
 */
final class GeminiPromptBuilder {

    private GeminiPromptBuilder() {
    }

    static String build(GenerationInput input) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Du bist ein professioneller Bewerbungsassistent. Verfasse ein professionelles, ")
                .append("deutschsprachiges Bewerbungsschreiben (Anschreiben) für eine Bewerbung in Deutschland, ")
                .append("ausschließlich basierend auf den folgenden Informationen. ")
                .append("Antworte NUR mit dem Text des Bewerbungsschreibens selbst — ohne Anrede-Erklärungen, ")
                .append("ohne einleitende Sätze wie \"Hier ist Ihr Bewerbungsschreiben\" und ohne abschließende Kommentare.\n\n");

        prompt.append("Bewerber/in: ").append(input.applicantName()).append('\n');
        prompt.append("Position: ").append(input.jobTitle()).append('\n');
        prompt.append("Unternehmen: ").append(input.companyName()).append('\n');

        if (input.cvText() != null && !input.cvText().isBlank()) {
            prompt.append("\nLebenslauf-Inhalt:\n").append(input.cvText()).append('\n');
        } else if (input.cvTitle() != null && !input.cvTitle().isBlank()) {
            prompt.append("Titel des hinterlegten Lebenslauf-Dokuments: ").append(input.cvTitle()).append('\n');
        }

        prompt.append("\nVollständige Stellenbeschreibung:\n").append(input.jobDescription()).append('\n');

        return prompt.toString();
    }
}
