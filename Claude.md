# JAPP — Claude Code Project Instructions

## 1. Project Overview

JAPP is a full-stack job application management system:

- Java 21 / Spring Boot 4.0.5 backend, multi-module Maven project (13 modules under the root `pom.xml`, packaging `pom`)
- Angular 22 frontend (`ui/ui`) — standalone components, Signals, Material 3
- PostgreSQL, schema owned by Flyway (`spring.jpa.hibernate.ddl-auto: validate` — Hibernate never generates DDL)
- JWT-based stateless authentication
- REST API under `/api/v1/**`
- Local filesystem file storage (`storage/`)
- CV parsing (PDF/DOCX/DOC/OCR via Tika)
- Database-backed, admin-managed AI provider configuration feeding cover-letter and CV-profile generation (Gemini, OpenAI-compatible, Anthropic, and a built-in Placeholder adapter)

The backend and frontend are developed as one integrated application. The backend API contract is the source of truth for frontend integration.

Frontend-specific rules (Angular architecture, design system, theming, i18n, forms, frontend testing) live in **`ui/ui/CLAUDE.md`**. This file covers the backend, cross-cutting architecture, and rules that apply repo-wide. Read both when a task touches the API contract.

Never invent API endpoints, DTO fields, request/response structures, authentication behavior, validation rules, database fields, or business rules. If something is unclear, inspect the existing implementation before making assumptions.

## 2. Core Development Principle

Before modifying code, understand the existing architecture and implementation.

1. Inspect → 2. Understand → 3. Plan → 4. Implement → 5. Test → 6. Review

Do not immediately start changing files based only on the user's description. For non-trivial changes: identify the relevant modules, inspect existing implementations, inspect related DTOs, inspect controllers/services/DAOs, inspect frontend consumers, inspect tests, identify dependencies between components, then implement the smallest appropriate change.

## 3. Backend Module Structure

The backend is 18 Maven modules at the repo root (siblings, not nested under a `backend/` folder) — verify against the root `pom.xml` `<modules>` list before trusting this table, since it has drifted before:

| Module | Responsibility |
|---|---|
| `japp-model` | Entities and DTOs (`de.jeb.japp.model.<domain>.dto`). No internal deps — a leaf module. |
| `japp-commons` | Shared domain exception hierarchy only (`de.jeb.japp.commons.exceptions.*`). No business logic. Leaf module. |
| `japp-dao` | Spring Data JPA repository interfaces (`de.jeb.japp.repositories`) **plus** a hand-written DAO facade per domain (`de.jeb.japp.dao.<domain>`) that services actually depend on. This two-layer indirection is intentional and consistent — don't "simplify" it by having services depend on repositories directly. |
| `japp-security-Service` | JWT auth, `SecurityConfig`, filters, password hashing (BCrypt). |
| `japp-file-storage` | Local filesystem file storage (`FileStorageServiceInterface` / `LocalFileStorageService`). Storage dir: `storage/`. Expected to change in a future phase — do not introduce cloud/object storage unless explicitly requested. |
| `japp-user-Service` | User business logic. |
| `japp-job-service` | Job and Company business logic. |
| `japp-tag-service` | Per-user tag taxonomy, applicable to both jobs and applications (two join tables, `V8__tags.sql`). |
| `japp-reminder-service` | Persists which derived reminder (user, application, kind, due date) has been dismissed/snoozed. Reminders themselves stay computed from `Application`'s own deadline/follow-up/interview-stage fields — this module has no reminder table of its own, only dismissal state (`V9__reminder_dismissals.sql`). |
| `japp-search-service` | Cross-entity search over the caller's **own** tracked jobs/companies/applications/cover letters. Distinct from `japp-job-search-services` below. |
| `japp-application-services` | Application-tracking business logic. |
| `japp-cv-parser` | CV parsing/extraction (PDF, DOCX, DOC, OCR via Tika) and CV-profile extraction orchestration. Keep CV parsing concerns isolated from unrelated application logic. |
| `japp-ai-provider-services` | Admin-managed AI provider configuration: `ProviderSettingsResolver`, credential encryption, connection testing, catalog. Has **no knowledge** of cover-letter/generation concepts. |
| `japp-generation-services` | Cover-letter generation, CV-profile extraction, and job-posting extraction (paste-to-import) orchestration: adapter registries, prompt builders, response parsers, `GenerationRequestService`. Depends on `japp-ai-provider-services`, never the reverse. |
| `japp-job-search-services` | Live external job search: fans a query out in parallel to Adzuna/Jooble/JSearch adapters, merges and de-dupes results. Nothing is persisted — results are only saved once the user picks "Save to my jobs," which reuses the existing `POST /api/v1/jobs` create flow. A source with no credentials configured just contributes zero results rather than failing the search (`AI_CREDENTIALS_ENCRYPTION_KEY`-style graceful-degradation convention). Depends only on `japp-model` + `japp-commons` — same leaf-ish shape as `japp-ai-provider-services`. |
| `japp-dashboard-services` | Aggregates user/job/application/cv-parser/generation services for dashboard views. |
| `japp-rest` | REST controllers, `/api/v1/**`. Delegates to the service modules above; contains **no** business logic itself. |
| `japp-core` | Application bootstrap only: `JappApplication` (`@SpringBootApplication` main class), `spring-boot-maven-plugin`, `AdminSeeder`, `application.yml`, and all Flyway migrations (`src/main/resources/db/migration`). This is **not** a business-logic module — it's the runnable shell on top of `japp-rest`. |

Always verify the actual repository structure before assuming a file or module exists.

## 4. Module Dependency Rules

Verified current dependency graph (no cycles):

```
japp-model, japp-commons          (leaves)
japp-dao                          → model
japp-security-Service             → model, dao
japp-file-storage                 → model
japp-user-Service                 → model, dao, commons
japp-job-service                  → model, dao, commons
japp-tag-service                  → model, dao, commons
japp-reminder-service             → model, dao, commons
japp-search-service                → model, dao, commons
japp-application-services         → model, dao, commons
japp-ai-provider-services         → model, dao, commons
japp-cv-parser                    → model, dao, security-Service, file-storage, commons
japp-generation-services          → model, dao, commons, ai-provider-services
japp-job-search-services          → model, commons
japp-dashboard-services           → model, user-Service, job-service, application-services, cv-parser, generation-services
japp-rest                         → model, dao, commons, security-Service, user-Service, job-service, tag-service,
                                     reminder-service, search-service, application-services, cv-parser,
                                     generation-services, dashboard-services, ai-provider-services, job-search-services
japp-core                         → security-Service, rest   (+ jpa, postgresql, flyway; owns spring-boot-maven-plugin)
```

Rules to preserve:

- `japp-ai-provider-services` may never depend on `japp-generation-services`, or on any generation/cover-letter concept. The dependency is one-directional: `japp-generation-services` → `japp-ai-provider-services`.
- `japp-model` and `japp-commons` stay dependency-free of other `japp-*` modules.
- Business logic belongs in the feature-service modules (`japp-*-Service`/`japp-*-services`), not in `japp-rest` controllers and not in `japp-dao`.
- `japp-core` should stay a thin bootstrap module (main class, seeders, config, migrations). Do not move business logic into it.
- When adding a new domain, follow the existing pattern: DTOs/entities in `japp-model`, exceptions in `japp-commons`, persistence in `japp-dao`, business logic in a new or existing feature-service module, HTTP exposure in `japp-rest`.

## 5. AI Provider & Generation Architecture

This is a core part of the system and easy to get wrong by guessing — always inspect before changing it.

- **Provider model**: AI providers are **admin-managed instances**, not a closed enum. Multiple instances can share one `AdapterType` (e.g. several OpenAI-compatible endpoints). Stored in the `ai_provider_configuration` table (`AiProviderConfiguration` entity in `japp-model`). This model replaced an older hardcoded enum approach (Flyway `V4__dynamic_ai_provider_instances.sql`) — treat any documentation or comment describing a fixed `PLACEHOLDER`/`GEMINI` enum as **outdated** (this includes `docs/generation-providers.md`, which has not been updated since that migration and should not be trusted as current).
- **`ProviderSettingsResolver`** (`japp-ai-provider-services`) resolves the effective config for a provider instance at generation-call time (never at startup), backed by a DB lookup with a 30s in-memory cache, invalidated on admin writes.
- **Credential encryption**: `SpringSecurityAiCredentialEncryptor` uses Spring Security Crypto (`Encryptors.text`), keyed by env var `AI_CREDENTIALS_ENCRYPTION_KEY`. The key is never persisted to the database and never logged. If unset, encryption/decryption throws but the app still starts (only providers needing credentials fail).
- **Adapter registry pattern**: `CoverLetterGenerationAdapterRegistry`, `CvProfileExtractionAdapterRegistry`, and `JobExtractionAdapterRegistry` (all in `japp-generation-services`, adapter classes grouped under `service/provider/{cv,job}/...`) auto-collect Spring beans implementing the adapter interfaces, keyed by `AdapterType`. Adding a new provider wire protocol means adding a new adapter bean, not touching registry code. Current adapters: Gemini, OpenAI-compatible, Anthropic Messages, and a built-in Placeholder (no external call, deterministic) for each of cover-letter generation, CV-profile extraction, and job-posting extraction (`JobExtractionService`, backing `POST /api/v1/jobs/extract` — the paste-to-import feature on the job form).
- **Live external job search is a separate, unrelated concept**: `japp-job-search-services`' Adzuna/Jooble/JSearch adapters (behind `ExternalJobSearchAdapter`, `GET /api/v1/job-search`) search public job-posting APIs and never call an AI provider — don't confuse them with the AI extraction adapters above, and don't route them through `ProviderSettingsResolver`/`GenerationRequestService`.
- **Generation request persistence**: every generation call is persisted via `GenerationRequest` (entity + DAO + repository), with status `PENDING → IN_PROGRESS → COMPLETED/FAILED`, job/CV snapshots, resolved provider instance, and error message. Do not add a code path that calls a provider adapter without going through `GenerationRequestService` and this persistence.
- **No providerId on a request** falls back to the built-in Placeholder instance — this is intentional, not a bug to "fix" by requiring a providerId.
- Never log or return provider API keys/credentials in any response, error message, or log statement. Never hardcode a provider API key.
- Reuse the existing adapter/registry/resolver path for any new provider or new generation feature. Do not create a parallel ad-hoc HTTP-calling path to an AI service.

## 6. REST API (`japp-rest`)

Base path: `/api/v1/**`. Confirmed current controllers:

`AuthController` (`/auth`), `UserController` (`/users`), `AdminUserController` (`/admin/users`), `CompanyController` (`/companies`), `JobController` (`/jobs`), `ApplicationController` (`/applications`), `CvController` (`/cv`), `CoverLetterController` (`/cover-letters`), `GenerationRequestController` (`/generation-requests`), `DashboardController` (`/dashboard`), `AiProviderController` (`/ai/providers`), `AdminAiProviderController` (`/admin/ai/providers`), `TagController` (`/tags`), `ReminderController` (`/reminders`), `SearchController` (`/search` — searches the caller's own tracked jobs/companies/applications/cover letters), `JobSearchController` (`/job-search` — live external listings, see §5).

Controllers should delegate business logic to appropriate services; do not place substantial business logic directly inside a controller.

`AbstractController` exists but is empty — don't treat it as a meaningful base class or extend it expecting shared behavior.

When modifying a DTO: find all backend usages, find all frontend usages, check serialization/deserialization, check validation, check tests, update affected consumers. Do not silently introduce breaking API changes.

## 7. Security / Authentication

- Stateless JWT (`SessionCreationPolicy.STATELESS`), CSRF disabled (stateless API), CORS from `app.cors.allowed-origins` (`CORS_ALLOWED_ORIGINS` env var, default `http://localhost:4200`).
- Only `/api/v1/auth/**` and `/error` are `permitAll()`; everything else requires a valid JWT.
- Login: `POST /api/v1/auth/login`. Registration: `POST /api/v1/auth/register`. Header: `Authorization: Bearer <token>`.
- `@EnableMethodSecurity` is on — admin-only operations are enforced with method-level checks server-side. **Any client-side admin/role check (e.g. an Angular guard) is UX only and must never be treated as the actual security boundary** — the server-side check is authoritative and must exist independently.
- Passwords are BCrypt-hashed.
- `AdminSeeder` (`japp-core`) creates a bootstrap admin from `BOOTSTRAP_ADMIN_EMAIL` / `BOOTSTRAP_ADMIN_PASSWORD` env vars, with local-dev defaults and a startup warning if left default — don't remove that warning as "unnecessary."
- No `AuthenticationEntryPoint` is configured, so both "unauthenticated" and "authenticated but forbidden" requests can return 403 (differentiated on the frontend by response body shape — see `ui/ui/CLAUDE.md`). This is real, relied-upon behavior, not an oversight to silently "fix" without checking the frontend interceptor first.
- Never weaken, bypass, or disable security mechanisms to make a feature work. Never hard-code JWT secrets, passwords, API keys, tokens, or credentials.

## 8. Exception Handling

Exception handling is **per-feature scoped**, not global. Each feature area has its own `@RestControllerAdvice`-style handler in `japp-rest` (e.g. `JobsExceptionHandler`, `CoverLetterExceptionHandler`, `ApplicationExceptionHandler`, `AdminAiProviderExceptionHandler`, `UserProfileExceptionHandler`, `GenerationRequestExceptionHandler`, `CvExceptionHandler`, `AdminUserExceptionHandler`, `AuthExceptionHandler`, `TagExceptionHandler`, `ReminderExceptionHandler`). `SearchController` and `JobSearchController` currently have no dedicated handler — neither throws a domain exception today. There is no global/root exception handler. When adding a new domain's error handling, follow this per-feature pattern rather than introducing a global handler, and don't introduce a second incompatible error-handling mechanism.

Domain exceptions live in `japp-commons` (`de.jeb.japp.commons.exceptions.<domain>`).

## 9. Database

- PostgreSQL, schema owned by **Flyway**. `spring.jpa.hibernate.ddl-auto: validate` — Hibernate will refuse to start if the schema doesn't match entities. Never rely on Hibernate auto-DDL for a schema change; never manually modify the schema as a replacement for a migration.
- Migrations live in `japp-core/src/main/resources/db/migration/`, naming convention `V<n>__snake_case_description.sql`. Current migrations (verify against the directory before trusting this list — it has drifted before): `V1__baseline`, `V2__add_application_tracking_and_job_salary`, `V3__add_cv_extraction_fields`, `V4__dynamic_ai_provider_instances`, `V5__cv_profile_generation`, `V6__user_management`, `V7__cv_profile_skills_and_languages`, `V8__tags`, `V9__reminder_dismissals`, `V10__interview_stages` (replaces `application.interview_date` with a multi-round `interview_stage` table), `V11__password_reset_and_email_verification`, `V12__user_avatar`.
- Before creating a migration: inspect existing migrations, inspect current entities, inspect DAO usage, determine whether the change is actually required. Never modify an already-applied migration unless explicitly requested.
- **Database safety**: never perform destructive operations (DROP DATABASE, DROP TABLE, TRUNCATE, destructive DELETE/UPDATE) against a real database unless explicitly requested. Prefer read-only inspection first when using database tools.
- Local Postgres is a native install (not Dockerized — there is no Dockerfile/docker-compose anywhere in this repo). Default connection: `localhost:5432/japp`, overridable via `DB_URL`/`DB_USERNAME`/`DB_PASSWORD`.

## 10. Secrets and Sensitive Information

Never hard-code secrets: passwords, JWT secrets, API keys (including AI provider keys), OAuth secrets, database credentials, access tokens, private keys. Do not commit secrets to Git.

Single `application.yml` in `japp-core` (no `application-dev.yml`/`application-prod.yml` split currently). All secrets come from environment variables with local-dev defaults inline: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `BOOTSTRAP_ADMIN_EMAIL`, `BOOTSTRAP_ADMIN_PASSWORD`, `CORS_ALLOWED_ORIGINS`, `AI_CREDENTIALS_ENCRYPTION_KEY`, plus per-provider vars like `GEMINI_API_KEY` (root `.env`, gitignored). Never print, log, or expose actual secret/credential values.

## 11. Dependency Management

Backend dependencies are managed through Maven. Before adding a dependency: check whether the functionality already exists, check existing dependencies, check whether an existing library can solve the problem, add a new dependency only when justified. Do not add dependencies merely for convenience.

Frontend dependencies are managed through the existing Angular package manager configuration (see `ui/ui/CLAUDE.md` for the approved UI stack). Do not replace the existing package manager or dependency strategy without explicit instruction.

## 12. Code Quality

Prefer readable code, small focused classes, clear names, strong typing, existing project conventions, minimal duplication, explicit error handling, maintainable abstractions.

Avoid unnecessary abstractions, speculative architecture, premature optimization, large refactors unrelated to the task, duplicated implementations, dead code, commented-out old implementations.

## 13. Existing Architecture Has Priority

Do not redesign the architecture unless explicitly asked. If the existing project uses a specific service pattern, repository/DAO pattern, DTO pattern, exception-handling pattern, validation approach, or naming convention, follow it. Consistency with the existing codebase is more important than introducing a theoretically cleaner architecture. If the existing architecture has a significant problem, explain it before performing a large architectural change. This project's module boundaries (§3–4) are a deliberate, working layering — changing them is an architectural decision, not a cleanup.

## 14. Testing

- Backend: JUnit 5 (Jupiter) + Mockito, mostly plain unit tests with mocked DAOs — no `@SpringBootTest`/Testcontainers observed in the current suite. Tests exist per-module for most feature-service modules (`japp-job-service`, `japp-generation-services`, `japp-dashboard-services`, `japp-user-Service`, `japp-ai-provider-services`, `japp-cv-parser`, `japp-application-services`). There is currently **no** test module for `japp-rest`, `japp-core`, `japp-security-Service`, `japp-file-storage`, or `japp-dao` — this is a known gap, not a convention to imitate by skipping tests for new modules.
- After modifying backend code: run relevant unit tests, run relevant integration tests when appropriate, compile the affected Maven modules.
- After modifying frontend code: run the appropriate Angular build/test commands (see `ui/ui/CLAUDE.md`).
- For API changes: verify both backend behavior and frontend integration. For UI changes: verify the affected user flow when possible.

## 15. Build Commands

- Backend: `mvn clean install` from the repo root is the standard build (Spring Boot 4.0.5, Java 21). The runnable/packaged module is `japp-core` (it owns `spring-boot-maven-plugin`). No custom Maven profiles exist. Do not automatically skip tests; only use `-DskipTests` when explicitly requested, debugging a build/dependency issue, or when tests are genuinely unrelated and the reason is explained.
- Frontend: commands depend on `ui/ui/package.json` — inspect it before assuming a command exists (see `ui/ui/CLAUDE.md`).

## 16. MCP Usage

MCP tools are development tools, not a replacement for understanding the codebase. Use them when they provide useful project context, prefer read-only inspection before modification, verify information from authoritative project sources, do not blindly trust external data.

Configured MCP servers (`.mcp.json`): `playwright`, `github` (via Docker), `postgres`. Locally, only `playwright` is currently enabled (`.claude/settings.local.json`) — `github` and `postgres` are defined but not enabled in this environment; verify availability before assuming either is reachable. Use only the MCP server relevant to the task.

There is no CI pipeline in this repo (no `.github/workflows`) — don't reference CI steps that don't exist.

Ignore `Agents.md` at the repo root — it's leftover generic React/TypeScript boilerplate from a template and does not describe this project. Do not follow it.

When using MCP: inspect, confirm, make the smallest appropriate change, test, review. Do not perform destructive external operations without explicit user approval.

### GitHub MCP

Use GitHub to inspect issues, pull requests, repository information, discussions, commit history. Before implementing an issue: read the issue, inspect the relevant source code, understand the requested behavior, implement the smallest appropriate change, test it. Do not close issues, merge PRs, push changes, or perform other irreversible GitHub operations unless explicitly requested.

### Browser / Playwright MCP

When browser automation is available, use it to verify actual user flows when appropriate (start the app, open the relevant page, perform the action, verify the result, inspect errors if the flow fails). Do not assume successful compilation means the UI works correctly. See `ui/ui/CLAUDE.md` §30–31 for the current state of frontend testing — there is no committed automated Playwright suite in this repo, only the MCP tool for ad hoc verification; do not claim "Playwright tests pass" when none exist.

## 17. Git Rules

Before making substantial changes: inspect `git status`, inspect relevant existing changes, do not overwrite unrelated user modifications. Treat existing uncommitted changes as intentional unless there is clear evidence otherwise.

Never reset, checkout, revert, or discard user changes unless explicitly requested. Do not use destructive Git commands (`git reset --hard`, `git clean -fd`, etc.) unless explicitly instructed. Do not reformat the entire project unnecessarily or perform broad automated refactoring without permission.

Keep changes focused on the requested task. Before committing, review the diff.

## 18. Feature Development Workflow

**Step 1 — Understand**: identify relevant backend module, relevant frontend module, related DTOs, controllers, services, DAOs, database entities/migrations, existing tests.

**Step 2 — Plan**: explain briefly what needs to change, which files/modules are affected, whether the API contract changes, whether the database changes, how the feature will be tested. For small changes, do not over-plan.

**Step 3 — Implement**: implement the smallest change that satisfies the requirement, following existing conventions.

**Step 4 — Test**: run relevant tests/builds.

**Step 5 — Review**: inspect the git diff, API contract, error handling, security implications, unintended changes. Then report what changed and what was tested.

## 19. Bug-Fixing Workflow

1. Reproduce the issue if possible.
2. Identify the failing layer.
3. Inspect logs/errors.
4. Trace the request through the system (§20).
5. Identify the root cause.
6. Implement the smallest fix.
7. Add or update tests where appropriate.
8. Verify the original failure is resolved.

Do not patch symptoms when the root cause can be identified.

## 20. Debugging Priority

When debugging a full-stack issue, trace the request through:

```
Angular UI
  ↓
Angular service
  ↓
HTTP request
  ↓
Spring Controller (japp-rest)
  ↓
DTO / validation
  ↓
Feature service module (e.g. japp-job-service, japp-application-services, japp-generation-services)
  ↓
DAO (japp-dao)
  ↓
PostgreSQL
```

Note: `japp-core` is not part of this request path — it's the bootstrap module only, not where business logic runs. Inspect each boundary rather than assuming where the problem is.

## 21. Communication Rules

When explaining a change: state what was changed, explain why, mention affected modules/files, mention tests performed, mention remaining issues.

Do not claim that something was tested if it was not actually tested. Do not claim that a command succeeded unless it was actually executed. If something cannot be verified, say so explicitly.

## 22. Ambiguity

If requirements are ambiguous but the existing code provides a clear convention, follow the existing convention and mention the assumption briefly.

If the ambiguity could cause data loss, security problems, API-breaking changes, architectural changes, or destructive database changes, ask for clarification before proceeding.

## 23. Minimal Change Principle

Prefer the smallest change that correctly solves the requested problem. Do not refactor unrelated code, rename unrelated classes, change formatting across entire files, upgrade dependencies unnecessarily, redesign APIs unnecessarily, or introduce new architecture without a clear requirement. A successful change should minimize regression risk.

## 24. Definition of Done

A task is considered complete only when appropriate:

- implementation is complete
- backend compiles (`mvn clean install` on affected modules)
- frontend compiles/builds where touched
- relevant tests pass; new tests added for new backend logic
- API contracts (request/response DTOs, status codes, auth requirements) remain consistent between backend and frontend
- security has not been weakened; no secrets logged or hardcoded
- database migrations are included when the schema changed
- no unrelated files were modified
- the final diff has been reviewed

Not every task requires every check, but explain which checks were performed.

## 25. Priority Order

When instructions conflict, prioritize:

1. Explicit user requirements
2. Existing project architecture
3. Existing code conventions
4. This CLAUDE.md (and `ui/ui/CLAUDE.md` for frontend work)
5. General best practices

Never change existing architecture solely because another approach is theoretically better.

## 26. Final Rule

JAPP is an existing software system, not a blank project. Understand the existing implementation before introducing new patterns. Prefer consistency, correctness, type safety, security, testability, and minimal changes over speed or unnecessary abstraction.
