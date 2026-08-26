package de.jeb.japp.generation.service.provider.job;

/**
 * Builds the prompt asking a model to extract a structured job posting from pasted text, as JSON only.
 */
public final class JobExtractionPromptBuilder {

    private JobExtractionPromptBuilder() {
    }

    public static String build(JobExtractionInput input) {
        return """
                You are extracting structured data from a job posting. Read the text below and \
                respond with ONLY a single JSON object — no markdown, no code fences, no commentary \
                before or after it — matching exactly this shape:
                
                {
                  "title": string or null (the job title),
                  "companyName": string or null (the hiring company's name),
                  "description": string or null (the full job description: responsibilities, requirements, \
                and any other relevant details, preserved as plain text — do not summarize or shorten it),
                  "location": string or null (city/region/country as stated),
                  "employmentType": string or null (one of exactly: "FULL_TIME", "PART_TIME", "CONTRACT", \
                "INTERNSHIP", "FREELANCE" — pick the closest match, or null if it cannot be determined),
                  "workMode": string or null (one of exactly: "REMOTE", "HYBRID", "ONSITE" — pick the closest \
                match, or null if it cannot be determined),
                  "salaryRange": string or null (as stated, e.g. "€60,000–€75,000"),
                  "url": string or null (a posting URL, only if one literally appears in the text)
                }
                
                If the text is empty, unreadable, or not a job posting, return {"title": null, \
                "companyName": null, "description": null, "location": null, "employmentType": null, \
                "workMode": null, "salaryRange": null, "url": null}.
                
                Job posting text:
                """ + input.rawText();
    }
}
