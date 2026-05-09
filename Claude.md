# JAPP Project Overview

JAPP is a full-stack enterprise-style application.

Architecture:

- Backend: Java Spring Boot multi-module Maven project
- Frontend: Next.js React application inside `/ui`
- Authentication: JWT-based auth
- Communication: REST API

The frontend MUST follow backend contracts exactly.
Do not invent fields, endpoints, or DTO structures.

---

# Project Structure

## Backend Modules

### japp-rest

Contains REST controllers and API endpoints.

Example:

- AuthController
- User endpoints
- Admin endpoints

This module exposes:
`/api/v1/**`

---

### japp-security-Service

Contains:

- JWT authentication
- authorization
- auth services
- security filters
- password handling

Authentication is token-based.

---

### japp-model

Contains:

- DTOs
- request/response models
- shared domain models

Frontend TypeScript interfaces should match DTOs exactly.

---

### japp-dao

Contains:

- repositories
- persistence layer
- database access

---

### japp-core

Contains:

- business logic
- service orchestration
- internal application logic

---

### japp-file-storage

Handles:

- uploads
- file management
- storage access

---

### japp-user-Service

Contains:

- user-related services
- user business logic

---

### japp-cv-parser

Handles CV parsing and extraction logic.

---

# Frontend

Location:
`/ui`

Stack:

- Next.js
- React
- TypeScript

Frontend rules:

- Use TypeScript everywhere
- Never use `any`
- Use reusable components
- Keep API logic separated
- Prefer functional components
- Use async/await
- Handle loading/error states

---

# API Base URL

Backend base URL:

http://localhost:8080/api/v1

---

# Authentication

Authentication uses JWT.

Login endpoint:
POST `/api/v1/auth/login`

Register endpoint:
POST `/api/v1/auth/register`

Frontend must:

- store JWT token
- attach Authorization header
- handle unauthorized responses

Authorization header format:

Bearer <token>

---

# Important Rules

When generating frontend:

- Read backend DTOs first
- Read controllers before generating API calls
- Do NOT assume backend behavior
- Match validation exactly
- Match field names exactly
- Keep frontend modular

---

# Development Workflow

Backend:

- IntelliJ
- Spring Boot

Frontend:

- VS Code
- Next.js

AI assistant should understand both backend and frontend context before generating code.