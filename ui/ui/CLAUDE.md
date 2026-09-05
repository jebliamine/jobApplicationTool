# JAPP Angular UI — Claude Code Instructions

## 1. Purpose

This directory (`ui/ui`) contains the Angular frontend for JAPP, a full-stack application for managing job applications, companies, job descriptions, CVs, cover letters, AI-generated application documents, and user settings.

The frontend communicates with the Spring Boot backend through REST APIs under `/api/v1/**`. The backend is the authoritative source of truth — see the root `CLAUDE.md` for backend module structure, the AI provider/generation architecture, and API-contract rules.

The frontend MUST NOT invent API endpoints, DTO fields, request/response structures, validation rules, authentication/authorization behavior, or business logic. Before implementing API-related functionality, inspect the corresponding backend controller/DTO/service.

there is a skill under (`ui/ui/.agent/skills/design-taste-frontend-v1`) refer to it also for mdetailed ui rules.

---

## 2. Technology Stack

Confirmed current stack (Angular 22.x, TypeScript ~6.0):

- Angular (standalone components, no NgModules)
- Angular Material (M3) + Angular CDK
- SCSS
- RxJS
- Angular Signals
- Angular Reactive Forms (typed)
- Lucide Angular (`@lucide/angular`)
- `@ngx-translate/core` + `@ngx-translate/http-loader` for i18n (see §11 below)
- Playwright MCP server (browser verification tool — see §31; there is currently **no** Playwright test suite/config committed in this repo, only the MCP tool)

### Explicitly prohibited UI frameworks

Do NOT introduce Tailwind CSS, Bootstrap, PrimeNG, DaisyUI, Materialize, another CSS framework, or another component library, unless explicitly requested by the project owner.

Angular Material is the primary component library. SCSS is the primary custom styling technology.

---

## 3. UI Philosophy

JAPP should look like a modern, professional SaaS application: clean, minimal, consistent, responsive, accessible, visually calm, information-focused. Avoid the generic Angular Material demo look.

```
Angular Material + JAPP Design System + SCSS = Modern JAPP UI
```

---

## 4. Component Library

Prefer Angular Material components for standard interactive elements (buttons, inputs, form fields, selects, checkboxes, radio buttons, date pickers, dialogs, menus, toolbars, sidenav, tables, paginator, tabs, cards, snackbars, progress indicators, tooltips). Use Angular CDK when Material doesn't provide the needed functionality. Do not hand-roll a component Material already provides.

---

## 5. Icons

Use Lucide Angular (`@lucide/angular`) for icons — confirmed in active use across the app (~40 components import individual tree-shaken icon components, e.g. `LucideCircleAlert`). Do not use emoji or arbitrary Unicode as UI icons, and don't hand-draw SVG icons when a Lucide icon exists. For icon-only buttons, always provide an accessible label:

```html
<button mat-icon-button aria-label="Delete application">
  <lucide-icon name="trash-2" />
</button>
```

---

## 6. SCSS

SCSS is the primary custom styling technology. No Tailwind, no utility-class frameworks. Prefer component-scoped SCSS, shared SCSS variables, CSS custom properties, and Angular Material theming. A component owns its component-specific styling; avoid excessive global CSS.

---

## 7. Design System / Design Tokens

The app uses a two-layer token system, already implemented — extend it, don't replace it:

- `_tokens.scss` defines `--japp-*` semantic tokens layered on top of Material's own `--mat-sys-*` M3 tokens (`mat.theme()` in `styles.scss`), plus independent spacing/radius/shadow/transition/layout tokens.
- `_theme-colors.scss` holds a hand-tuned dark palette that intentionally overrides the raw M3-generated dark surfaces (there's a comment in the code explaining why — read it before changing dark-mode colors).

Example token style:

```scss
:root {
  --japp-spacing-xs: 4px;
  --japp-spacing-sm: 8px;
  --japp-spacing-md: 16px;
  --japp-radius-sm: 6px;
  --japp-radius-md: 10px;
}
```

Do not create an unnecessarily complicated token system, but do keep new colors/spacing centralized here rather than hard-coded in components.

---

## 8. Modern Visual Design

Avoid the default "Angular starter application" look and excessive gradients/shadows/rounded cards/animations/visual noise. Use whitespace and hierarchy. Prefer clear page titles, meaningful sections, subtle elevation, consistent spacing, restrained borders, clear primary actions, strong typography hierarchy.

---

## 9. Color System

A JAPP-specific color system covers primary, secondary/accent, success, warning, error, informational, background, surface, text, muted text, and borders — centralized as theme variables (see §7), not hard-coded per component. Ensure sufficient contrast in both light and dark themes.

---

## 10. Light, Dark, and System Mode

Implemented via `ThemeService` (`core/theme/theme.service.ts`): a signal-based `mode` (`light | dark | system`) persisted to `localStorage['japp-theme']`, an `effectiveTheme` computed against `matchMedia('(prefers-color-scheme: dark)')`, applied via an `effect()` that sets `document.documentElement.dataset.theme` — no reload required. Default respects OS preference; explicit selection persists. Both themes are first-class — check every major UI element (nav, cards, forms, dialogs, tables, dropdowns, menus, inputs, validation/loading/empty/error/success states) in both themes. Use semantic theme variables (`var(--japp-surface)`, `var(--japp-text-primary)`, `var(--japp-border)`), never fixed colors that break in dark mode.

---

## 11. Localization (i18n)

The app uses `@ngx-translate/core` + `@ngx-translate/http-loader`. Translation files: `ui/ui/public/i18n/{en,de}.json`. `LanguageService` (`core/language/language.service.ts`) mirrors `ThemeService`'s pattern exactly — signal + `effect()` + `localStorage` persistence — and falls back to the browser's language if unset. Currently supported: `en`, `de`.

When adding user-facing text, add translation keys to both `en.json` and `de.json` rather than hard-coding strings, following the existing key structure in those files.

---

## 12. Responsive Design

JAPP must be fully usable on desktop, laptop, tablet, and mobile. Consider responsive behavior while implementing a component, not afterward. Use CSS Flexbox/Grid/media queries, and `BreakpointObserver` (Angular CDK) only where actual behavioral changes (not just layout) are required.

---

## 13. Mobile Design

Mobile is a first-class platform, not a shrunk desktop UI. Redesign layout for smaller screens when necessary (e.g. sidebar → top navigation). Large desktop tables need an explicit mobile strategy (responsive columns, horizontal scroll when justified, card/list presentation, or detail views) — never let important information become inaccessible on mobile.

---

## 14. Accessibility

Mandatory. Use semantic HTML; support keyboard navigation, visible focus states, screen readers, sufficient color contrast, accessible form labels/dialogs/navigation/icon buttons. Use ARIA only when native semantic HTML isn't enough. Never communicate important information with color alone (e.g. an error needs more than a red border).

---

## 15. Angular Architecture

Standalone components/directives/pipes only — no NgModules unless a dependency requires one (none currently do; the entire `src/app` tree is standalone). Use `inject()`-style DI, Signals, and RxJS where appropriate. Components handle presentation, user interaction, and local UI state; business logic and API calls live in services.

---

## 16–17. Structure

Feature-oriented organization (guideline, not a rigid requirement):

```
src/app/
├── core/        (auth, guards, interceptors, services, models, theme, language, http)
├── shared/      (genuinely reusable components/directives/pipes/models only)
├── layout/      (user-shell, admin-shell, topbar, admin-sidebar, user-nav, user-menu, nav-search, notification-bell, theme-toggle)
├── features/    (auth, dashboard, cv, jobs, companies, applications, cover-letters, settings, admin/*)
├── app.config.ts
├── app.routes.ts
└── app.ts
```

Two routed shells exist today, split at the route level (not conditional rendering): `layout/user-shell/` for authenticated user routes, `layout/admin-shell/` for admin routes. Keep feature-specific code inside its feature; `shared/` is for things genuinely reused across features, not a dumping ground.

---

## 18. TypeScript

Use strict typing. Never use `any` unless truly unavoidable and explicitly documented. Never use `as any` or type assertions to bypass compiler errors. Prefer interfaces/type aliases, strongly typed observables, typed reactive forms (`FormGroup<...>`, `FormControl<T>` with `nonNullable: true`), and typed API responses.

---

## 19. Backend Contract

The Spring Boot backend is the source of truth (see root `CLAUDE.md` §6–9 for the current controller list and AI provider architecture). Before implementing an API integration: read the controller, request DTO, response DTO, validation annotations, service behavior, auth requirements, and relevant persistence model if needed. Never invent endpoints or fields, never rename API fields for convenience. If frontend and backend disagree, stop and identify the mismatch rather than silently compensating for it.

---

## 20. API Services

```
Component → Feature Service → HttpClient → REST API
```

One service per feature (`ApplicationService`, `GenerationService`, `CoverLetterService`, `AiProviderService`, `UserService`, etc.), each building its URL from `environment.apiUrl` + resource path. Keep substantial HTTP logic out of components. Use the shared `describeApiError()` helper (`core/http/describe-api-error.ts`) for consistent user-facing error text rather than inlining error parsing per component.

`environment.ts` currently has no `environment.prod.ts` counterpart — a single environment file is in use; don't assume a prod/dev split exists without checking `angular.json`'s configurations first.

---

## 21. Authentication

JWT. Backend endpoints: `POST /api/v1/auth/login`, `POST /api/v1/auth/register`. Header: `Authorization: Bearer <token>`.

A single functional `authInterceptor` (`core/interceptors/auth.interceptor.ts`) attaches the token and handles session-expiry redirects centrally — don't duplicate token-attachment logic in individual services.

**Known backend quirk, already handled — don't "fix" it without understanding why**: the backend has no `AuthenticationEntryPoint` configured, so both an anonymous/invalid-token request and an authenticated-but-forbidden (ownership) request can return HTTP 403. The interceptor distinguishes them by response body shape: a bodyless 403 is treated as session-expired (triggers logout + redirect to `/login`); a 403 carrying `{message}` is treated as a normal forbidden-action error and shown to the user. If you touch this interceptor, preserve that distinction.

Route guards (`core/guards/`) are UX convenience only, not the security boundary — the server enforces auth/role checks independently (see root `CLAUDE.md` §7). `authGuard` checks `AuthService.isAuthenticated()` and triggers `UserService.ensureLoaded()`; `guestGuard`/`publicGuard` redirect authenticated users away from public/auth pages; `adminGuard` waits on `UserService`'s loading signal before reading `currentUser()?.role === 'ADMIN'`, to avoid a race on fresh page loads where the profile hasn't loaded yet.

---

## 22. Forms

Angular Reactive Forms, strongly typed, matching backend request DTOs closely. Forms must reproduce relevant backend validation, show clear validation messages and loading state, prevent duplicate submissions, and display server validation errors. Frontend validation does not replace backend validation.

---

## 23–25. Loading / Error / Empty States

No shared global loading/error/empty component exists — the current, consistent pattern is per-component: a local `signal<boolean>` for loading/submitting, a `signal<string|null>` for `serverError`, `ToastService` (`core/ui/toast.service.ts`) for transient success/error messages, and `describeApiError()` for turning `HttpErrorResponse` into user-facing text. Follow this pattern for new features rather than introducing a different one. Every important list needs a meaningful empty state with a clear call-to-action where appropriate; never expose stack traces or internal backend details to users.

---

## 26. Component Design

Keep components focused on presentation, interaction, and local state. Extract logic (into a service) when a component starts accumulating substantial API calls, business logic, complex state, and validation together — but don't prematurely extract before that complexity actually shows up.

---

## 27. State Management

Signals + services + RxJS is the current and sufficient approach — no NgRx or other global state library is in use. Don't introduce one merely because it's popular; only when complexity genuinely justifies it, and discuss it first.

---

## 28. Routing

Angular Router, feature-oriented, fully lazy-loaded via `loadComponent()` (confirmed — every route in `app.routes.ts` lazy-loads). Route guards protect authenticated/admin areas (see §21). Don't duplicate authorization logic inside components that a guard already covers.

---

## 29. Performance

Lazy-loaded routes, efficient observables, Signals where appropriate, pagination for large datasets, optimized images, avoiding unnecessary subscriptions. Don't prematurely optimize — only when there's a real performance requirement.

---

## 30. Testing

Test runner: **`@angular/build:unit-test`** (esbuild/Vitest-based, via `angular.json`'s `test` target — not Karma), with Jasmine-style BDD syntax (`describe`/`it`/`expect`, `TestBed`) and `HttpTestingController` for HTTP-service specs. Run with `npm test` (`ng test`).

There is currently no committed Playwright test suite or config in this repo (no `playwright.config.*`, no e2e spec files) — Playwright is available only as an MCP tool for ad hoc browser verification (§31), not as an automated regression suite. Do not claim "Playwright tests pass" — there are none to run. If the project owner asks for a real e2e suite, that's a separate, explicit task (adding `@playwright/test`, config, and spec files), not something to assume already exists.

For UI changes, verify the affected user flow manually via the Playwright MCP tool when it's available and useful (see §31), and say explicitly whether you did or didn't.

---

## 31. Playwright MCP

Use the Playwright MCP server when browser interaction or UI verification provides real value: inspecting the current UI, verifying a new page, checking responsive behavior, testing forms/navigation, checking dark/light mode, reproducing a browser-specific problem. Don't use it for backend-only tasks, and don't invoke it unnecessarily (it costs context/tokens per call). Don't claim browser verification happened if it wasn't actually used in this session.

---

## 32. MCP Efficiency

Available MCP servers for this repo (`.mcp.json`): `playwright`, `github` (via Docker), `postgres`. Locally, only `playwright` is currently enabled (`.claude/settings.local.json`) — `github` and `postgres` are configured but not enabled in this environment; verify availability before assuming either is reachable. Use only the MCP server relevant to the task at hand.

---

## 33. Dependency Rules

Before installing a new npm package: check whether Angular, Angular Material/CDK, or an existing dependency already covers it; determine whether it's genuinely necessary; explain the reason before adding it. Don't add UI libraries casually.

Approved primary stack: Angular, Angular Material, Angular CDK, SCSS, RxJS, Angular Signals, Reactive Forms, Lucide Angular, `@ngx-translate`. No Tailwind, Bootstrap, PrimeNG, or competing UI/component frameworks.

---

## 34. Development Workflow

1. **Inspect** — understand existing implementation before changing it.
2. **Backend contract** — if the feature touches the backend, inspect the controller, DTO, service, persistence layer, and migrations where relevant (root `CLAUDE.md` has the current module map).
3. **Plan** — for significant changes, state files to modify, architecture, API interaction, UI structure, testing strategy.
4. **Implement** — incrementally; don't touch unrelated files or do unrelated refactoring.
5. **Verify** — run the Angular build, TypeScript checks, and unit tests (`npm test`); use Playwright MCP for a real UI flow when useful and available.
6. **Review** — inspect `git diff`, ensure no unrelated changes.

---

## 35. Existing Code Has Priority

Search existing code, reuse existing services/components, follow established conventions, avoid duplicate implementations. Don't build a new solution when an appropriate one already exists.

---

## 36. Do Not Invent Requirements

If requirements are unclear: inspect existing code, backend contracts, and docs; identify reasonable assumptions and state them clearly. Don't silently invent business behavior. For major architectural decisions, explain trade-offs before implementing.

---

## 37. Current Project State

The frontend is well past an early scaffold: it has full auth flows, two routed shells (user + admin), a working theme system, i18n, and feature areas covering dashboard, CV, jobs, companies, applications (including a board view), cover letters (including AI generation), tags, reminders, cross-entity search, live external job search, and settings/admin (including AI provider management). The backend it talks to is a mature 18-module system with a real AI provider/generation architecture (see root `CLAUDE.md` — verify the module count there before repeating it elsewhere, it has drifted before).

This means: don't assume backend endpoints are missing or "not implemented yet" — check first. It's still appropriate to build new features incrementally and keep the backend contract authoritative, but "the backend is only partially implemented" is no longer a safe default assumption for this project — verify against the actual controller before assuming an endpoint doesn't exist.

---

## 38. Critical Rule

Understand → Inspect existing code → Verify backend contract → Plan → Implement → Test → Review.

Never: Guess → Generate large amounts of code → Hope it works.

The goal is a maintainable, professional, modern Angular SaaS application.
