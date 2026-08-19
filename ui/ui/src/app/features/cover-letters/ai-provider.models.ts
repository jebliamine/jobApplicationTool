/** Mirrors the response body of GET /api/v1/ai/providers — never contains credentials. */
export interface AiProviderResponse {
  id: string;
  displayName: string;
  available: boolean;
  model: string | null;
}
