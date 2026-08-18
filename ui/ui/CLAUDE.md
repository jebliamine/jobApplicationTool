# JAPP Angular UI — Claude Code Instructions

## 1. Purpose

This directory contains the Angular frontend for JAPP.

JAPP is a full-stack application for managing:

- Job applications
- Companies
- Job descriptions
- CVs
- Cover letters
- AI-generated application documents
- User settings

The frontend communicates with the Spring Boot backend through REST APIs.

The backend API is the authoritative source of truth.

The frontend MUST NOT invent:

- API endpoints
- DTO fields
- request structures
- response structures
- validation rules
- authentication behavior
- authorization behavior
- business logic

Before implementing API-related functionality, inspect the corresponding backend implementation.

---

# 2. Technology Stack

The frontend uses:

- Angular
- TypeScript
- Angular Material
- Angular CDK
- SCSS
- RxJS
- Angular Signals
- Angular Reactive Forms
- Lucide Angular
- Playwright

## Explicitly prohibited UI frameworks

Do NOT introduce:

- Tailwind CSS
- Bootstrap
- PrimeNG
- DaisyUI
- Materialize
- another CSS framework
- another component library

unless explicitly requested by the project owner.

Angular Material is the primary component library.

SCSS is the primary custom styling technology.

---

# 3. UI Philosophy

JAPP should look like a modern professional SaaS application.

The visual design should be:

- modern
- clean
- professional
- minimal
- consistent
- responsive
- accessible
- visually calm
- information-focused

Avoid making the application look like a generic Angular Material demo.

Angular Material provides the component foundation.

The JAPP design system must provide the application's own:

- color system
- typography
- spacing
- elevation
- border radius
- component styling
- layout patterns
- navigation patterns

The goal is:

```text
Angular Material
       +
JAPP Design System
       +
SCSS
       =
Modern JAPP UI
```
---

# 4. Component Library

Angular Material is the primary component library.

Prefer Angular Material components for standard interactive UI elements.

Examples:

Buttons
Inputs
Form fields
Selects
Checkboxes
Radio buttons
Date pickers
Dialogs
Menus
Toolbars
Sidenav
Tables
Paginator
Tabs
Cards
Snackbars
Progress indicators
Tooltips

Use Angular CDK when functionality is not directly provided by Angular Material.

Do not recreate standard UI components manually when Angular Material already provides an appropriate component.

---

# 5. Icons

Use Lucide Angular for application icons.

Prefer recognizable, simple icons.

Do not use:

emoji as UI icons
arbitrary Unicode symbols
manually drawn SVG icons when an appropriate Lucide icon exists

Icons should have consistent sizing and visual weight.

Icons must not replace accessible text when the meaning is ambiguous.

For icon-only buttons, provide an accessible label.

Example:

<button
  mat-icon-button
  aria-label="Delete application"
>
  <lucide-icon name="trash-2" />
</button>

---

# 6. SCSS

SCSS is the primary custom styling technology.

Do not use Tailwind CSS.

Do not introduce utility-class CSS frameworks.

Prefer:

component-scoped SCSS
shared SCSS variables
CSS custom properties
Angular Material theming
reusable layout classes only where genuinely useful

Avoid excessive global CSS.

A component should own its component-specific styling.

---

# 7. Design System

JAPP must have a consistent design system.

Centralize important design decisions such as:

colors
typography
spacing
border radius
shadows
transitions
layout dimensions

Prefer CSS custom properties for values that need to change between themes.

Example:

:root {
  --japp-spacing-xs: 4px;
  --japp-spacing-sm: 8px;
  --japp-spacing-md: 16px;
  --japp-spacing-lg: 24px;
  --japp-spacing-xl: 32px;


  --japp-radius-sm: 6px;
  --japp-radius-md: 10px;
  --japp-radius-lg: 16px;
}

Do not create an unnecessarily complicated design-token system.

Expand the design system as the application grows.

---

# 8. Modern Visual Design

Avoid the default "Angular starter application" appearance.

Avoid excessive:

gradients
shadows
rounded cards
animations
decorative elements
visual noise

Use whitespace and hierarchy to organize information.

Prefer:

clear page titles
meaningful sections
subtle elevation
consistent spacing
restrained borders
clear primary actions
strong typography hierarchy

UI should feel like a production SaaS application rather than a component showcase.

---

# 9. Color System

Define a JAPP-specific color system.

The color system must support:

primary color
secondary/accent color
success
warning
error
informational states
background
surface
text
muted text
borders

Do not hard-code arbitrary colors throughout components.

Prefer centralized theme variables.

Ensure sufficient contrast in both light and dark themes.

---

# 10. Light and Dark Mode

JAPP MUST support:

Light mode
Dark mode
System mode

The default behavior should respect the user's operating-system preference.

The user must be able to explicitly select:

Light
Dark
System

The explicit selection should be persisted.

Theme switching must not require a page reload.

Both themes must be treated as first-class designs.

Do not design the light theme first and "fix dark mode later."

---

# 11. Dark Mode Requirements

Every major UI element must be checked in both themes.

This includes:

navigation
cards
forms
dialogs
tables
dropdowns
menus
buttons
inputs
validation messages
loading states
empty states
error states
success states

Do not use fixed colors that become unreadable in dark mode.

Prefer semantic theme variables.

For example:

background: var(--japp-surface);
color: var(--japp-text-primary);
border-color: var(--japp-border);

rather than arbitrary hard-coded colors.

 ---

# 12. Responsive Design

JAPP MUST be fully usable on:

desktop
laptop
tablet
mobile

Responsive behavior must be considered when implementing the component, not added afterward.

Use:

CSS Flexbox
CSS Grid
CSS media queries
Angular CDK BreakpointObserver where behavioral changes are required

Do not use JavaScript for simple CSS layout problems.

---

# 13. Mobile Design

Mobile is a first-class platform.

Do not simply shrink the desktop UI.

When necessary, redesign the layout for smaller screens.

Example:

Desktop:

┌──────────────┬──────────────────────────┐
│              │                          │
│   Sidebar    │       Main Content       │
│              │                          │
└──────────────┴──────────────────────────┘

Mobile:

┌──────────────────────────┐
│       Top Navigation     │
├──────────────────────────┤
│                          │
│       Main Content       │
│                          │
└──────────────────────────┘

Navigation should become appropriate for mobile.

Large desktop tables should have an explicit mobile strategy.

Possible strategies:

responsive columns
horizontal scrolling when justified
alternative card/list presentation
hiding secondary information
opening detail views

Never allow important information to become inaccessible on mobile.

---

# 14. Accessibility

Accessibility is mandatory.

Use semantic HTML.

Support:

keyboard navigation
visible focus states
screen readers
sufficient color contrast
accessible form labels
accessible dialogs
accessible navigation
accessible icon buttons

Use ARIA only when necessary.

Prefer native semantic HTML.

Do not use color alone to communicate important information.

For example, an error should not be communicated only by a red border.

---

# 15. Angular Architecture

Use modern Angular architecture.

Prefer:

standalone components
standalone directives
standalone pipes
dependency injection
Signals
RxJS

Avoid introducing NgModules unless required by the project or a dependency.

Components should primarily handle:

presentation
user interaction
local UI state

Business logic should live in services.

API communication should live in dedicated services.

---

# 16. Feature-Based Structure

Prefer feature-oriented organization.

Recommended structure:

src/app/


├── core/
│   ├── auth/
│   ├── guards/
│   ├── interceptors/
│   ├── services/
│   └── models/
│
├── shared/
│   ├── components/
│   ├── directives/
│   ├── pipes/
│   └── models/
│
├── layout/
│   ├── shell/
│   ├── navbar/
│   ├── sidebar/
│   └── footer/
│
├── features/
│   ├── auth/
│   ├── dashboard/
│   ├── cv/
│   ├── jobs/
│   ├── applications/
│   ├── cover-letters/
│   └── settings/
│
├── app.config.ts
├── app.routes.ts
└── app.ts

This structure is a guideline, not an absolute requirement.

Do not create unnecessary abstractions merely to follow the structure.

---

# 17. Feature Structure

A feature can contain its own:

components
models
services
routes
state

Example:

features/
└── applications/


    ├── application-list/
    ├── application-detail/
    ├── application-form/
    ├── applications.service.ts
    └── applications.models.ts

Keep feature-specific code inside the feature.

Do not put feature-specific functionality into shared.

shared should contain genuinely reusable functionality.

---

# 18. TypeScript

Use strict TypeScript.

Never use:

any

unless absolutely unavoidable and explicitly documented.

Avoid:

as any

Do not use type assertions to bypass compiler errors.

Prefer:

interfaces
type aliases
strongly typed observables
strongly typed forms
typed API responses


---

# 19. Backend Contract

The Spring Boot backend is the source of truth.

Before implementing an API integration:

Read the controller.
Read the request DTO.
Read the response DTO.
Read validation annotations.
Read service behavior.
Check authentication requirements.
Check relevant persistence models if necessary.

Do not invent endpoints.

Do not invent fields.

Do not rename API fields for convenience.

If frontend and backend disagree, stop and identify the mismatch.

Do not silently compensate for the mismatch.

---

# 20. API Services

Keep HTTP calls inside services.

Preferred architecture:

Component
    ↓
Feature Service
    ↓
HttpClient
    ↓
REST API

Do not place substantial HTTP logic directly inside components.

Use strongly typed HTTP requests.

---

# 21. Authentication

Authentication uses JWT.

Backend endpoints:

POST /api/v1/auth/login
POST /api/v1/auth/register

Authorization:

Authorization: Bearer <token>

Use an HTTP interceptor for attaching the JWT.

Do not duplicate token logic throughout API services.

Handle authentication errors centrally where appropriate.

---

# 22. Forms

Use Angular Reactive Forms.

Prefer strongly typed forms.

Forms must:

match backend DTOs
reproduce relevant backend validation
show clear validation messages
show loading state
prevent duplicate submissions
display server validation errors

Frontend validation does not replace backend validation.

---

# 23. Loading States

All asynchronous user operations should provide appropriate feedback.

Examples:

Loading
Saving
Uploading
Generating
Deleting

Prevent duplicate actions where appropriate.

Avoid blocking the entire application for a small local operation.

---

# 24. Error States

Handle:

validation errors
network errors
authentication errors
authorization errors
server errors
unexpected errors

Present errors in user-friendly language.

Do not expose stack traces or internal backend details to users.

---

# 25. Empty States

Every important list should have a meaningful empty state.

Example:

No job applications yet.


Create your first application to get started.

Where appropriate, provide a clear call-to-action.

Do not leave users with an unexplained empty screen.

---

# 26. Component Design

Keep components focused.

Avoid components containing all of:

API communication
business logic
complex state
validation
navigation
large templates

Extract logic when complexity increases.

Do not prematurely create abstractions.

---

# 27. State Management

Do not introduce a global state-management library unless necessary.

Start with:

Angular Signals
services
RxJS

Do not introduce NgRx merely because it is popular.

Introduce global state management only when the application's complexity justifies it.

---

# 28. Routing

Use Angular Router.

Use feature-oriented routes.

Use route guards for protected areas.

Prefer lazy-loaded feature routes where appropriate.

Do not duplicate authorization logic throughout components.

---

# 29. Performance

Prefer:

lazy-loaded routes
efficient observables
Signals where appropriate
pagination for large datasets
optimized images
avoiding unnecessary subscriptions

Do not prematurely optimize.

Optimize when there is a real performance requirement.

30. UI Testing

Use Playwright for end-to-end browser testing.

Test real user workflows.

Important workflows include:

registration
login
logout
CV upload
job creation
application creation
cover-letter generation
settings
theme switching
responsive/mobile behavior

Do not test implementation details when user-level behavior can be tested instead.

---

# 31. Playwright MCP

Use the Playwright MCP server when browser interaction or UI verification provides value.

Examples:

inspecting the current UI
verifying a new page
checking responsive behavior
testing forms
testing navigation
checking dark/light mode
reproducing browser-specific problems

Do not use Playwright MCP for backend-only tasks.

Do not invoke it unnecessarily.

---

# 32. MCP Efficiency

Available MCP servers may include:

GitHub
Playwright
Angular

Use only the MCP server relevant to the task.

Examples:

Backend-only task:

Do not use Playwright.

GitHub-only task:

Use GitHub MCP if repository information is required.

UI task:

Use Playwright when browser verification is useful.

Avoid unnecessary MCP tool calls because they increase context and token usage.

---

# 33. Dependency Rules

Before installing a new npm package:

Check whether Angular already provides the functionality.
Check Angular Material/CDK.
Check whether an existing project dependency provides it.
Determine whether the dependency is genuinely necessary.
Explain the reason before adding it.

Do not add UI libraries casually.

The approved primary UI stack is:

Angular
Angular Material
Angular CDK
SCSS
RxJS
Angular Signals
Reactive Forms
Lucide Angular
Playwright

Do not add Tailwind CSS.

Do not add Bootstrap.

Do not add PrimeNG.

Do not add competing UI frameworks.

---

# 34. Development Workflow

For every feature:

Step 1 — Inspect

Understand existing implementation before changing it.

Step 2 — Backend Contract

If the feature communicates with the backend:

inspect controller
inspect DTO
inspect service
inspect persistence layer
inspect migrations where relevant
Step 3 — Plan

Before significant changes, describe:

files to modify
architecture
API interaction
UI structure
testing strategy
Step 4 — Implement

Implement incrementally.

Do not modify unrelated files.

Do not perform unrelated refactoring.

Step 5 — Verify

Run appropriate:

Angular build
TypeScript checks
unit tests
Playwright tests
Step 6 — Review

Inspect:

git diff

Ensure there are no unrelated changes.

---

# 35. Existing Code Has Priority

Before creating new functionality:

Search existing code.
Reuse existing services.
Reuse existing components.
Follow established project conventions.
Avoid duplicate implementations.

Do not create a new solution when an appropriate existing solution already exists.

---

# 36. Do Not Invent Requirements

If requirements are unclear:

Inspect existing code.
Inspect backend contracts.
Inspect documentation.
Identify reasonable assumptions.
Clearly state assumptions.

Do not silently invent business behavior.

For major architectural decisions, explain trade-offs before implementation.

---

# 37. Current Project State

The Angular frontend is currently in an early development stage.

The backend is only partially implemented.

Therefore:

do not build the entire frontend at once
do not invent missing backend APIs
build features incrementally
establish reusable UI patterns
establish the design system early
keep the backend contract authoritative
avoid premature abstraction

Build JAPP as incremental vertical features.

Example:

Feature
   │
   ├── Backend
   │
   ├── API contract
   │
   ├── Angular UI
   │
   └── Tests


---

# 38. Critical Rule

When implementing a feature, always prefer:

Understand
    ↓
Inspect existing code
    ↓
Verify backend contract
    ↓
Plan
    ↓
Implement
    ↓
Test
    ↓
Review

Never:

Guess
  ↓
Generate large amounts of code
  ↓
Hope it works

The goal is a maintainable, professional, modern Angular SaaS application.
