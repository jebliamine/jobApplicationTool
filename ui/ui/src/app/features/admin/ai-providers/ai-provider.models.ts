/** The wire-protocol adapters the backend supports — see AdapterType (backend). PLACEHOLDER is built-in and not creatable. */
export type AdapterType = 'PLACEHOLDER' | 'OPENAI_COMPATIBLE' | 'ANTHROPIC_MESSAGES' | 'GEMINI_GENERATE_CONTENT';

/** Adapter types an admin can create a new instance of — PLACEHOLDER is built-in only. */
export const CREATABLE_ADAPTER_TYPES: { value: AdapterType; label: string }[] = [
  { value: 'OPENAI_COMPATIBLE', label: 'OpenAI-Compatible (OpenAI, Ollama, LM Studio, vLLM, ...)' },
  { value: 'ANTHROPIC_MESSAGES', label: 'Anthropic' },
  { value: 'GEMINI_GENERATE_CONTENT', label: 'Google Gemini' },
];

/** Mirrors the response body of GET /api/v1/admin/ai/providers — never the API key or its ciphertext. */
export interface AdminAiProviderResponse {
  id: string;
  adapterType: AdapterType;
  displayName: string;
  enabled: boolean;
  hasApiKey: boolean;
  defaultModel: string | null;
  baseUrl: string | null;
}

/** Request body for POST /api/v1/admin/ai/providers — creates a new provider instance. */
export interface AiProviderCreateRequest {
  adapterType: AdapterType;
  displayName: string;
  enabled?: boolean;
  defaultModel?: string | null;
  baseUrl?: string | null;
  apiKey?: string;
}

/**
 * Request body for PUT /api/v1/admin/ai/providers/{id}. apiKey
 * omitted/undefined leaves the existing key unchanged; clearApiKey removes
 * it entirely.
 */
export interface AiProviderUpdateRequest {
  displayName?: string;
  enabled?: boolean;
  defaultModel?: string | null;
  baseUrl?: string | null;
  apiKey?: string;
  clearApiKey?: boolean;
}

/** Result of POST /api/v1/admin/ai/providers/{id}/test. */
export interface AiProviderTestResult {
  success: boolean;
  message: string | null;
}
