---
name: design-taste-frontend-v1
description: JAPP-specific frontend taste skill, adapted from the generic React/Tailwind v1 for this project's actual stack (Angular standalone + Angular Material M3 + SCSS + Lucide Angular) and its "calm, professional SaaS" visual philosophy. This is the authoritative taste skill for JAPP's frontend — it never overrides `ui/ui/CLAUDE.md`, only adds design judgment on top of it.
---

# High-Agency Frontend Skill (JAPP Edition)

## 0. Before You Design (Process)
* Ground every screen in JAPP's real subject matter (jobs, applications, CVs, cover letters, companies) and real data shapes — never generic placeholder content.
* Before building, briefly plan: what the screen's single job is, how information should be prioritized/grouped, and whether any deliberate visual choice is actually warranted here. Don't skip straight to code for anything non-trivial.
* Actively avoid the current default AI-generated-UI looks (cream background + high-contrast serif + terracotta accent; near-black background + single neon accent; brutalist broadsheet with hairline rules and dense columns) — none of these fit a clean, professional SaaS brief anyway.
* After building, self-critique against the rules below and against `ui/ui/CLAUDE.md` before calling the work done — see §8's pre-flight check.

## 1. ACTIVE BASELINE CONFIGURATION
* DESIGN_VARIANCE: 3 (1=Perfect Symmetry, 10=Artsy Chaos)
* MOTION_INTENSITY: 3 (1=Static/No movement, 10=Cinematic/Magic Physics)
* VISUAL_DENSITY: 4 (1=Art Gallery/Airy, 10=Pilot Cockpit/Packed Data)

**AI Instruction:** JAPP's UI philosophy (`ui/ui/CLAUDE.md` §3, §8) calls for a "clean, minimal, consistent, responsive, accessible, visually calm, information-focused" SaaS look and explicitly warns against "excessive gradients/shadows/rounded cards/animations/visual noise." These baseline values (3, 3, 4) reflect that directly — restrained layout variance, restrained motion, normal information density. Do not ask the user to edit this file. Otherwise, ALWAYS listen to the user: adapt these values dynamically based on what they explicitly request in a given chat prompt, but treat a request to raise MOTION_INTENSITY or DESIGN_VARIANCE as a deliberate, scoped exception for that screen — not a new default for the rest of the app.

## 2. DEFAULT ARCHITECTURE & CONVENTIONS
JAPP's actual stack and constraints (`CLAUDE.md`, `ui/ui/CLAUDE.md`) always win over generic defaults. Adhere to these:

* **DEPENDENCY VERIFICATION [MANDATORY]:** Before importing ANY 3rd-party library, check `ui/ui/package.json` first. If it's missing, output the install command before providing code, and explain why an existing dependency (Angular, Angular Material/CDK, Lucide Angular, RxJS, `@ngx-translate`) doesn't already cover it. **Never** assume a library exists.
* **Framework & Interactivity:** Angular, standalone components/directives/pipes only — no NgModules. Use `inject()`-style DI, Angular Signals, and RxJS where appropriate. Components handle presentation/interaction/local state; business logic and API calls live in services.
* **Styling Policy:** SCSS is the only custom styling technology. Prefer component-scoped SCSS, shared SCSS variables/mixins, and CSS custom properties layered on Angular Material's M3 theming (`mat.theme()` in `styles.scss`).
    * **PROHIBITED FRAMEWORKS [CRITICAL]:** Never introduce Tailwind CSS, Bootstrap, PrimeNG, DaisyUI, Materialize, or any other CSS/component framework, and never write utility-class styling. This is a hard constraint from `ui/ui/CLAUDE.md` §2/§33, not a style preference.
* **ANTI-EMOJI POLICY [CRITICAL]:** NEVER use emojis in code, markup, text content, or alt text. Use Lucide Angular icons or clean SVG primitives instead. Emojis are BANNED.
* **Responsiveness & Spacing:**
  * Use JAPP's existing spacing tokens (`--japp-spacing-*` in `_tokens.scss`) rather than inventing new magic numbers.
  * Contain page layouts using an existing max-width convention or a proper SCSS variable, not an arbitrary one-off value.
  * **Viewport Stability [CRITICAL]:** NEVER size a full-height section with `100vh`. Use `100dvh` to prevent layout jumping in mobile browsers (iOS Safari).
  * **Grid over Flex-Math:** NEVER use complex flexbox percentage math. Use CSS Grid for reliable multi-column structures.
  * Use Angular CDK's `BreakpointObserver` only where actual behavioral changes (not just layout) are required — pure layout responsiveness stays in CSS media queries (`ui/ui/CLAUDE.md` §12).
* **Icons:** Use Lucide Angular (`@lucide/angular`) exclusively — no other icon library, and no hand-drawn SVG when a Lucide icon already exists. Keep icon stroke width consistent across the app. Icon-only buttons always need an `aria-label`.

## 3. DESIGN ENGINEERING DIRECTIVES (Bias Correction)
LLMs have statistical biases toward specific UI cliché patterns. Proactively counter them using JAPP's SCSS/token system — never utility classes:

**Rule 1: Deterministic Typography**
* **Headings:** Drive the type scale from SCSS/token variables (size, weight, tracking, line-height), not one-off inline values. Lean on weight and color for hierarchy rather than raw size — JAPP explicitly avoids an oversized, screaming H1.
* **TECHNICAL UI RULE:** This is a dashboard/software UI — no serif fonts, anywhere. Verify JAPP's current type stack in `styles.scss`/`_tokens.scss` before assuming a specific face; use a monospace face only for aligned numeric/tabular data.
* **Body/Paragraphs:** Comfortable line length and line-height; use the existing muted-text token for secondary copy, never a hard-coded gray.

**Rule 2: Color Calibration**
* **Constraint:** Max 1 accent color per screen context, kept desaturated enough to sit calmly next to Material's neutral surfaces.
* **THE LILA BAN:** No "AI Purple/Blue" glow aesthetic — no purple button glows, no neon gradients.
* **COLOR CONSISTENCY:** Never fluctuate between warm and cool neutrals within the same view. Always use `var(--japp-*)` semantic tokens (`ui/ui/CLAUDE.md` §9), never a fixed color that breaks in dark mode (§10).

**Rule 3: Layout Restraint**
* Centered or left-aligned single-column headers are entirely appropriate at JAPP's default DESIGN_VARIANCE — this is a calm SaaS tool, not a marketing site, so asymmetry-for-its-own-sake is not a goal here. Prioritize clear hierarchy and scannability. Reserve a bolder asymmetric treatment for the rare case where the user explicitly asks for something distinctive (e.g. a one-off landing/marketing page) and raises DESIGN_VARIANCE for that screen specifically.

**Rule 4: Materiality, Elevation, and "Anti-Card Overuse"**
* Use Angular Material's elevation system deliberately — not every grouping needs a card. Prefer hairline dividers (`border-top`, a `divide`-style border between rows) or spacing alone when elevation isn't communicating real hierarchy, matching `ui/ui/CLAUDE.md` §8's "subtle elevation, restrained borders."
* Use a card only when elevation communicates real hierarchy (e.g. a distinct actionable unit on a dashboard) — don't box in every piece of data by default.

**Rule 5: Interactive UI States**
* **Mandatory Generation:** Never ship a component with only its happy-path state. Every data-bearing component needs:
  * **Loading:** A skeleton matching the real layout's dimensions, or the existing local `loading` signal pattern (`ui/ui/CLAUDE.md` §23–25) — don't invent a generic spinner where a skeleton is feasible.
  * **Empty States:** A clear empty state with an actionable next step.
  * **Error States:** Inline, specific error text via `describeApiError()` and `ToastService` — never a raw stack trace or backend implementation detail.
  * **Tactile Feedback:** Subtle `:active` feedback (slight scale/translate) on primary actions, kept restrained per the low MOTION_INTENSITY baseline.

**Rule 6: Data & Form Patterns**
* Labels above inputs, helper text present in markup where useful, error text below the input, consistent spacing between fields — matches JAPP's typed Angular Reactive Forms convention (`ui/ui/CLAUDE.md` §22). Frontend validation mirrors backend DTO validation; it never replaces it.

**Rule 7: Content & Copy**
* Words are interface, not decoration. Write from the end user's side of the screen: name controls by what people do ("Save changes," not "Submit"), and keep an action's name consistent through its whole flow — a "Publish" button produces a "Published" toast, not a generic "Success."
* Use plain, active, specific language; avoid AI copywriting filler ("Elevate", "Seamless", "Unleash", "Next-Gen").
* Errors state what happened and how to fix it, in the interface's voice — no apologizing, no vagueness.
* Every user-facing string goes into both `en.json` and `de.json` (`ui/ui/CLAUDE.md` §11) — never hard-code copy in a template.

## 4. Motion (Use Sparingly — Default Baseline Is Low)
JAPP's default MOTION_INTENSITY is 3: short, purposeful CSS `:hover`/`:active` transitions only — no automatic or perpetual motion by default. Only reach for anything richer if the user explicitly raises MOTION_INTENSITY for a specific screen, and even then keep it scoped:
* Prefer plain CSS transitions or `@angular/animations` over a JS animation library. Add a dedicated animation library only if the requirement genuinely can't be met otherwise, and only after clearing the dependency-verification rule in §2.
* If richer motion is explicitly requested, isolate it to the single component that needs it rather than letting it spread across the page — this also protects mobile performance.
* For layout reordering/resizing, use Angular's animation triggers rather than manually tweening `top`/`left`/`width`.
* For a staggered reveal, if requested: a short CSS `animation-delay: calc(var(--index) * 60ms)` cascade is enough for JAPP's tone — avoid elaborate scroll-choreography that reads as a marketing site rather than a work tool.

## 5. PERFORMANCE GUARDRAILS
* **DOM Cost:** Any decorative filter/noise effect must sit on a fixed, `pointer-events: none` layer — never inside a scrolling container.
* **Hardware Acceleration:** Animate exclusively via `transform` and `opacity`, never `top`/`left`/`width`/`height`.
* **Z-Index Restraint:** Reserve z-index for systemic layers (sticky topbar, dialogs/overlays, menus) — never sprinkle arbitrary z-index values.

## 6. TECHNICAL REFERENCE (Dial Definitions)

### DESIGN_VARIANCE (Level 1-10)
* **1-3 (JAPP default — Predictable):** Consistent grid alignment, symmetrical spacing, centered or left-aligned headers as the content calls for — the calm, professional SaaS look JAPP wants by default.
* **4-7 (Offset):** Occasional overlap or varied proportions for one screen that specifically needs more visual interest (e.g. a dashboard summary hero). Use deliberately, not as a new baseline.
* **8-10 (Asymmetric):** Reserved for an explicitly requested marketing/landing-style treatment outside JAPP's normal authenticated app shell.
* **MOBILE:** Anything above the 1-3 range must cleanly collapse to a single, full-width column below JAPP's mobile breakpoint — no horizontal scrolling, no broken asymmetry.

### MOTION_INTENSITY (Level 1-10)
* **1-3 (JAPP default — Static/Restrained):** CSS `:hover`/`:active` only, short purposeful transitions (roughly 150–250ms, standard easing).
* **4-7 (Fluid CSS):** A deliberate, scoped transition for one specific flow (a modal open/close, a drag-reorder) — use `@angular/animations` or CSS, not a JS animation library, unless justified.
* **8-10 (Advanced Choreography):** Only for an explicitly requested, isolated, non-default treatment. Never the default for JAPP's authenticated app screens.

### VISUAL_DENSITY (Level 1-10)
* **1-3 (Art Gallery Mode):** Rarely appropriate for JAPP's information-focused screens.
* **4-7 (JAPP default — Daily App Mode):** Normal spacing for a standard SaaS work tool; most JAPP screens should sit here.
* **8-10 (Cockpit Mode):** Reserve for a genuinely data-dense view (a dense table or board) where the user asks for it. Use monospace for aligned numeric columns at this density.

## 7. AI TELLS (Forbidden Patterns)
Avoid these common AI design signatures unless explicitly requested:

### Visual & CSS
* **NO Neon/Outer Glows:** No default glow box-shadows. Use inner borders or subtle tinted shadows via Material elevation tokens.
* **NO Pure Black:** Never `#000000`. Use the existing dark-theme surface tokens (`_theme-colors.scss`) — don't invent a new near-black.
* **NO Oversaturated Accents:** Keep accents desaturated enough to sit calmly next to Material's neutral surfaces.
* **NO Excessive Gradient Text:** Avoid text-fill gradients for headings.
* **NO Custom Mouse Cursors:** Outdated, hurts accessibility and performance.

### Typography
* **NO Oversized, Screaming H1s:** Control hierarchy with weight and color, not just scale.
* **Serif Constraints:** No serif fonts anywhere in this dashboard/software UI.

### Layout & Spacing
* Align and space using JAPP's spacing tokens consistently — no ad hoc padding/margin values scattered per component.
* **NO Generic 3-Column Feature Rows** for any marketing-style content JAPP might add later — prefer a layout that reflects the actual content structure over a default template shape.

### Content & Data (The "Jane Doe" Effect)
* **NO Generic Placeholder Names:** Avoid "John Doe"/"Jane Doe"/"Acme Corp" in any example, demo, or seed content — use realistic, varied sample data appropriate to a job-application/CV/company domain.
* **NO Generic Avatars:** Don't default to a stock "egg" icon when initials or a styled Lucide icon is just as easy.
* **NO Fake Round Numbers:** Prefer organic, realistic sample data ("3 of 7 applications in progress," not "50%") in any demo/empty-state copy.
* **NO Filler Words:** Avoid AI copywriting clichés ("Elevate", "Seamless", "Unleash", "Next-Gen"). Use concrete verbs describing what the feature actually does.

### External Resources & Components
* **NO Broken Unsplash Links:** If a placeholder image is genuinely needed (e.g. a company logo placeholder), use a reliable generator (`https://picsum.photos/seed/{seed}/{w}/{h}`) or a generated initials avatar — never Unsplash.
* Prefer Angular Material components; only build a custom component when Material/CDK genuinely doesn't cover the need (`ui/ui/CLAUDE.md` §4).
* **Production-Ready Cleanliness:** Code must be clean and consistent with JAPP's existing conventions rather than introducing a second way to solve an already-solved problem.

## 8. FINAL PRE-FLIGHT CHECK
Evaluate your work against this matrix before calling it done:
- [ ] No Tailwind/Bootstrap/PrimeNG/DaisyUI or any other prohibited framework introduced.
- [ ] All colors/spacing/radii come from JAPP's existing `--japp-*` tokens or the Material theme — nothing hard-coded.
- [ ] Icons are Lucide Angular only, with `aria-label` on every icon-only button.
- [ ] Layout collapses cleanly to a single mobile column; any full-height section uses `100dvh`, not `100vh`.
- [ ] Motion stays within the current MOTION_INTENSITY baseline unless explicitly raised for this screen.
- [ ] Loading, empty, and error states are all implemented — not just the happy path.
- [ ] All user-facing copy lives in `en.json`/`de.json`, written in plain, active, end-user language.
- [ ] Both light and dark themes checked, using semantic tokens rather than fixed colors.
- [ ] Anything this skill doesn't cover has been checked against `ui/ui/CLAUDE.md`.
