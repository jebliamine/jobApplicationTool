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
                      "description": string or null (1-3 sentences)
                    }
                  ]
                }

                List experiences most-recent-first. If the CV text is empty, unreadable, or not a CV, \
                return {"fullName": null, "summary": null, "experiences": []}.

                CV text:
                """ + input.cvText();
    }
}
