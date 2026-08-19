/** Mirrors the response body of GET /api/v1/admin/ai/providers — never the API key or its ciphertext. */
export interface AdminAiProviderResponse {
  provider: string;
  displayName: string;
  enabled: boolean;
  hasApiKey: boolean;
  defaultModel: string | null;
  baseUrl: string | null;
}

/**
 * Request body for PUT /api/v1/admin/ai/providers/{provider}. apiKey
 * omitted/undefined leaves the existing key unchanged; clearApiKey removes
 * it entirely.
 */
export interface AiProviderUpdateRequest {
  enabled?: boolean;
  defaultModel?: string | null;
  baseUrl?: string | null;
  apiKey?: string;
  clearApiKey?: boolean;
}

/** Result of POST /api/v1/admin/ai/providers/{provider}/test. */
export interface AiProviderTestResult {
  success: boolean;
  message: string | null;
}
