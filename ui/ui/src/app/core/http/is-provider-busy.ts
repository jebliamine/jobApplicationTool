/**
 * Matches the wording adapters use for a transient, capacity-related failure (rate limit hit,
 * provider temporarily unavailable, connection timeout) — deliberately provider-agnostic so it
 * covers Gemini/OpenAI-compatible/Anthropic without depending on their exact phrasing. Anything
 * that doesn't match (misconfiguration, auth, validation) is treated as a real error.
 */
const RETRYABLE_FAILURE_PATTERN = /rate limit|currently unavailable|temporarily unavailable|timeout or connection/i;

export function isProviderBusy(message: string): boolean {
  return RETRYABLE_FAILURE_PATTERN.test(message);
}
