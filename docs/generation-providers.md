# Cover-Letter Generation Providers

Two providers are available for POST /api/v1/generation-requests:

- `PLACEHOLDER` — deterministic, template-based text. No external service, no configuration required. This is the default when `provider` is omitted from the request.
- `GEMINI` — calls Google's Gemini API over HTTPS to generate a German Bewerbungsschreiben. Requires a Gemini API key.

---

# Selecting a provider

Request DTO field:

```json
{
  "jobId": "...",
  "cvDocumentId": "...",
  "provider": "GEMINI"
}
```

Omitting `provider` uses `PLACEHOLDER`.

---

# Configuring Gemini locally

Set these environment variables before starting the backend. All have sensible defaults except the API key — without it, the application still starts normally, but a `GEMINI` generation request fails with a clean error (`GenerationRequest.status = FAILED`) instead of crashing anything.

| Variable | Purpose | Default |
|---|---|---|
| `GEMINI_API_KEY` | Your Gemini API key ([ai.google.dev](https://ai.google.dev)) | *(empty — Gemini unconfigured)* |
| `AI_GEMINI_MODEL` | Gemini model name | `gemini-3.7-flash` (`gemini-2.0-flash` was shut down 2026-06-01 — do not use it) |
| `AI_GEMINI_BASE_URL` | Gemini API base URL | `https://generativelanguage.googleapis.com` |
| `AI_GEMINI_TIMEOUT` | Connect/read timeout | `30s` |

Example (PowerShell):

```powershell
$env:GEMINI_API_KEY = "your-key-here"
```

Example (bash):

```bash
export GEMINI_API_KEY="your-key-here"
```

Never commit a real API key. The key is only ever read from environment configuration on the backend — it is never sent to Angular, never included in any REST response, and never logged (it's sent to Gemini as a request header, not a URL query parameter).

---

# What each provider needs

`PLACEHOLDER` needs a job with a description. Nothing else.

`GEMINI` needs:
- a configured `GEMINI_API_KEY`
- a job with a description
- (optional) a CV — only its title is used, since the system does not extract CV text
