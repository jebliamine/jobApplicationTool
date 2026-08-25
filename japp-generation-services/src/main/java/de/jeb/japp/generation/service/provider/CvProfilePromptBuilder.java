package de.jeb.japp.generation.service.provider;

/** Builds the prompt asking a model to extract a structured profile from CV text, as JSON only. */
public final class CvProfilePromptBuilder {

    private CvProfilePromptBuilder() {
    }

    public static String build(CvProfileExtractionInput input) {
        return """
                You are extracting structured data from a CV/resume. Read the CV text below and \
                respond with ONLY a single JSON object — no markdown, no code fences, no commentary \
                before or after it — matching exactly this shape:

                {
                  "fullName": string or null,
                  "summary": string or null (a 2-3 sentence professional summary in the third person),
                  "experiences": [
                    {
                      "company": string or null,
                      "title": string or null,
                      "startDate": string or null ("YYYY-MM-DD"; use the 1st of the month if only month/year is known),
                      "endDate": string or null ("YYYY-MM-DD", or null if this is the current role),
                      "description": string or null (see instructions below)
                    }
                  ],
                  "skills": array of strings (every skill/technology/tool the CV lists, one per entry; empty array if none),
                  "languages": [
                    {
                      "name": string (the language itself, e.g. "English"),
                      "level": string or null (the proficiency exactly as the CV states it, e.g. "native", "C1", "fluent")
                    }
                  ]
                }

                For each experience's "description": preserve everything the CV says about that role — every \
                responsibility, achievement, technology, and metric mentioned — as plain text (bullet points may be \
                joined with newlines). Do NOT summarize, shorten, paraphrase away specifics, or drop any bullet point; \
                only the overall "summary" field above should be a short synthesis, never the per-experience descriptions.

                List experiences most-recent-first. If the CV text is empty, unreadable, or not a CV, \
                return {"fullName": null, "summary": null, "experiences": [], "skills": [], "languages": []}.

                CV text:
                """ + input.cvText();
    }
}
