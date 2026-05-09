# AI Agent Instructions

Before generating frontend code:

1. Read related backend controller
2. Read request DTO
3. Read response DTO
4. Read authentication flow
5. Read existing frontend structure

Never:

- invent API endpoints
- invent DTO fields
- change backend contracts

Always:

- use TypeScript
- create reusable components
- separate API layer from UI layer
- implement loading/error handling

---

# Frontend Architecture

Suggested structure:

src/
├── app/
├── components/
├── services/
├── hooks/
├── types/
├── utils/
└── contexts/

---

# API Rules

All API calls should be centralized.

Example:
services/authService.ts

Avoid calling fetch directly inside components.

Use:

- axios OR centralized fetch wrapper

---

# Authentication Rules

After login:

- save token
- save user data if returned
- add Authorization header globally

Handle:

- expired token
- unauthorized requests
- logout flow

---

# Backend Priority

Backend is the source of truth.

If frontend conflicts with backend:
backend wins.

---

# Coding Style

Prefer:

- clean architecture
- strongly typed code
- modularity
- readability
- reusable components

Avoid:

- giant components
- duplicated logic
- inline business logic
- magic strings

---

# UI Expectations

UI should:

- be responsive
- have proper loading states
- have error states
- use consistent spacing
- follow accessible practices

---

# Expected AI Workflow

When asked to build a feature:

1. Read backend controller
2. Read DTOs
3. Understand response structure
4. Generate TS interfaces
5. Create API layer
6. Create React components
7. Create hooks/state management
8. Implement UX states