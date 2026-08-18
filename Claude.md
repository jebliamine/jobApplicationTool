# JAPP — Claude Code Project Instructions

## 1. Project Overview

JAPP is a full-stack enterprise-style job application management system.

The application consists of:

- Java / Spring Boot backend
- Multi-module Maven architecture
- Angular frontend
- PostgreSQL database
- JWT-based authentication
- REST API
- Local file storage
- CV parsing functionality

The backend and frontend are developed as one integrated application.

The backend API contract is the source of truth for frontend integration.

Never invent:

- API endpoints
- DTO fields
- request structures
- response structures
- authentication behavior
- validation rules
- database fields
- business rules

If something is unclear, inspect the existing implementation before making assumptions.

# 2. Core Development Principle

Before modifying code, understand the existing architecture and implementation.

Prefer:

1. Inspect
2. Understand
3. Plan
4. Implement
5. Test
6. Review

Do not immediately start changing files based only on the user's description.

For non-trivial changes:

- identify the relevant modules
- inspect existing implementations
- inspect related DTOs
- inspect controllers/services/repositories
- inspect frontend consumers
- inspect tests
- identify dependencies between components
- then implement the smallest appropriate change

# 3. Project Structure

The repository is structured approximately as follows:

japp/
│
├── pom.xml
│
├── japp-rest/
│ └── REST controllers and API endpoints
│
├── japp-security-Service/
│ └── authentication and authorization
│
├── japp-model/
│ └── DTOs and shared models
│
├── japp-dao/
│ └── repositories and persistence
│
├── japp-core/
│ └── business logic and orchestration
│
├── japp-file-storage/
│ └── file storage functionality
│
├── japp-user-Service/
│ └── user-related business logic
│
├── japp-cv-parser/
│ └── CV parsing and extraction
│
└── ui/
└── ui/
└── Angular application

Always verify the actual repository structure before assuming a file or module exists.

# 4. Backend Architecture

## japp-rest

Responsible for:

- REST controllers
- HTTP request handling
- HTTP response handling
- API endpoints
- request validation at the API boundary

API base path:

/api/v1/**

Examples include:

- authentication endpoints
- user endpoints
- admin endpoints
- job application endpoints
- CV-related endpoints

Controllers should delegate business logic to appropriate services.

Do not place substantial business logic directly inside controllers.

## japp-security-Service

Responsible for:

- JWT authentication
- authorization
- authentication services
- security filters
- password handling
- authentication-related configuration

Authentication is token-based.

Do not weaken, bypass, or disable security mechanisms to make a feature work.

Never hard-code:

- JWT secrets
- passwords
- API keys
- tokens
- credentials

## japp-model

Contains:

- DTOs
- request models
- response models
- shared domain models

DTOs exposed by REST endpoints define the API contract.

When modifying a DTO:

1. Find all backend usages.
2. Find all frontend usages.
3. Check serialization/deserialization.
4. Check validation.
5. Check tests.
6. Update affected consumers.

Do not silently introduce breaking API changes.

## japp-dao

Responsible for:

- repositories
- persistence
- database access
- JPA/Hibernate integration

Repositories should remain focused on persistence concerns.

Do not move business logic into repositories unless the existing architecture explicitly requires it.

## japp-core

Responsible for:

- business logic
- service orchestration
- application/domain operations
- coordination between components

Business rules should primarily live in the appropriate service/core layer rather than controllers.

## japp-file-storage

Responsible for:

- file uploads
- file management
- storage access

Current storage implementation:

- local filesystem
- storage directory: `storage`

The storage implementation is expected to change in a future development phase.

Do not introduce cloud/object storage unless explicitly requested.

## japp-user-Service

Responsible for:

- user-related services
- user business logic
- user operations

## japp-cv-parser

Responsible for:

- CV parsing
- CV text extraction
- CV-related processing

Keep CV parsing concerns isolated from unrelated application logic.

# 5. Frontend Architecture

Frontend location:

`/ui/ui`

Technology:

- Angular
- TypeScript

The frontend communicates with the backend through REST APIs.

The backend API is the source of truth.

# 6. Angular Development Rules

Use:

- TypeScript
- Angular components
- Angular services
- Angular dependency injection
- Angular routing
- Angular reactive forms where appropriate
- strongly typed interfaces/models

Do NOT use:

- `any`
- unnecessary global state
- duplicated API logic
- hard-coded API responses
- invented DTO fields
- duplicated business logic

Prefer strict typing.

If an API response is unknown, inspect the backend DTO/controller instead of using `any` as a workaround.

# 7. Angular Component Responsibilities

Keep components focused on:

- UI state
- user interaction
- presentation
- coordination with services

API communication should normally be implemented through Angular services rather than directly inside components.

Avoid large components containing:

- API calls
- complex business logic
- data transformation
- validation logic
- UI rendering logic

Separate responsibilities when appropriate.

# 8. Frontend API Integration

Before implementing or modifying an API call:

1. Find the corresponding backend controller.
2. Identify the endpoint.
3. Inspect HTTP method.
4. Inspect request DTO.
5. Inspect response DTO.
6. Inspect validation requirements.
7. Inspect authentication requirements.
8. Then implement the Angular service.

Never infer an API contract from naming alone.

For example, do not assume:

GET `/api/v1/applications`

exists simply because an `Application` entity exists.

Verify the actual controller.

# 9. API Base URL

Development backend:

`http://localhost:8080/api/v1`

Use the project's existing Angular environment/configuration mechanism for API URLs.

Do not scatter hard-coded URLs throughout components or services.

# 10. Authentication

Authentication uses JWT.

Login:

POST `/api/v1/auth/login`

Registration:

POST `/api/v1/auth/register`

Authorization header:

`Authorization: Bearer <token>`

Frontend responsibilities include:

- storing the authentication token according to the existing application design
- attaching the token to authenticated requests
- handling unauthorized responses
- protecting authenticated routes where appropriate

Before changing authentication behavior, inspect the existing security implementation.

# 11. Backend-Frontend Contract

This is one of the most important JAPP rules.

The backend defines the API contract.

For every API integration, verify:

- endpoint
- HTTP method
- path parameters
- query parameters
- request body
- response body
- DTO fields
- nullable fields
- validation
- HTTP status codes
- authentication requirements
- error responses

Frontend models should reflect backend DTOs.

Do not create a frontend interface that contradicts the backend DTO.

If the backend DTO changes, inspect and update all affected frontend consumers.

# 12. Database Rules

The application uses PostgreSQL.

Persistence uses:

- JPA
- Hibernate
- Spring Data repositories

Database schema changes must be handled through the project's existing migration mechanism.

If Flyway is present:

- create a Flyway migration for schema changes
- do not manually modify the database schema as a replacement for migrations
- never modify an already-applied migration unless explicitly requested
- use the existing migration naming convention

Before creating a migration:

1. Inspect existing migrations.
2. Inspect current entities.
3. Inspect repository usage.
4. Determine whether the change is actually required.

# 13. Database Safety

Never perform destructive database operations unless explicitly requested.

Do not execute:

- DROP DATABASE
- DROP TABLE
- TRUNCATE
- destructive DELETE
- destructive UPDATE

against production or unknown environments.

When using database-related tools, prefer read-only inspection first.

Never expose or print:

- database passwords
- credentials
- connection secrets
- tokens

# 14. Dependency Management

Backend dependencies are managed through Maven.

Before adding a dependency:

1. Check whether the functionality already exists.
2. Check existing dependencies.
3. Check whether an existing library can solve the problem.
4. Add a new dependency only when justified.

Do not add dependencies merely for convenience.

Frontend dependencies are managed through the existing Angular package manager configuration.

Do not replace the existing package manager or dependency strategy without explicit instruction.

# 15. Code Quality

Prefer:

- readable code
- small focused classes
- clear names
- strong typing
- existing project conventions
- minimal duplication
- explicit error handling
- maintainable abstractions

Avoid:

- unnecessary abstractions
- speculative architecture
- premature optimization
- large refactors unrelated to the task
- duplicated implementations
- dead code
- commented-out old implementations

# 16. Existing Architecture Has Priority

Do not redesign the architecture unless explicitly asked.

If the existing project uses a specific:

- service pattern
- repository pattern
- DTO pattern
- exception handling pattern
- validation approach
- Angular structure
- naming convention

follow it.

Consistency with the existing codebase is more important than introducing a theoretically cleaner architecture.

If the existing architecture has a significant problem, explain it before performing a large architectural change.

# 17. Error Handling

Before implementing new error handling:

1. Inspect existing exception classes.
2. Inspect global exception handlers.
3. Inspect existing API error responses.
4. Follow the established pattern.

Do not introduce a second incompatible error-handling mechanism.

# 18. Testing

After modifying backend code:

- run relevant unit tests
- run relevant integration tests when appropriate
- compile the affected Maven modules

After modifying frontend code:

- run the appropriate Angular build/test/lint commands available in the project

For API changes:

- verify both backend behavior and frontend integration

For UI changes:

- verify the affected user flow when possible.

# 19. Build Commands

Use the project's existing Maven configuration.

Typical backend commands may include:

`mvn clean install`

or:

`mvn clean install -DskipTests`

Do not automatically skip tests.

Only use `-DskipTests` when:

- explicitly requested
- debugging a build/dependency issue
- tests are unrelated and the reason is explained

Frontend commands depend on the project's package configuration.

Inspect `package.json` before assuming a command exists.

# 20. Git Rules

Before making substantial changes:

- inspect `git status`
- inspect relevant existing changes
- do not overwrite unrelated user modifications

Never reset, checkout, revert, or discard user changes unless explicitly requested.

Do not use destructive Git commands such as:

`git reset --hard`

or:

`git clean -fd`

unless explicitly instructed.

Keep changes focused on the requested task.

Before committing, review the diff.

# 21. MCP Usage

MCP tools are development tools, not a replacement for understanding the codebase.

When MCP tools are available:

- use them when they provide useful project context
- prefer read-only inspection before modification
- verify information from authoritative project sources
- do not blindly trust external data

Potential development MCP integrations include:

- GitHub
- PostgreSQL
- browser automation / Playwright
- documentation systems
- issue trackers

When using MCP:

1. Inspect.
2. Confirm.
3. Make the smallest appropriate change.
4. Test.
5. Review.

Do not perform destructive external operations without explicit user approval.

# 22. Browser / Playwright Usage

When browser automation is available:

Use it to verify actual user flows when appropriate.

For example:

1. Start the Angular application.
2. Open the relevant page.
3. Inspect the UI.
4. Perform the user action.
5. Verify the result.
6. Inspect errors if the flow fails.

Do not assume that successful compilation means the UI works correctly.

Browser testing should normally target the local development environment.

# 23. GitHub Usage

When GitHub MCP is available:

Use GitHub to inspect:

- issues
- pull requests
- repository information
- relevant discussions
- commit history

Before implementing an issue:

1. Read the issue.
2. Inspect the relevant source code.
3. Understand the requested behavior.
4. Implement the smallest appropriate change.
5. Test it.

Do not close issues, merge PRs, push changes, or perform other irreversible GitHub operations unless explicitly
requested.

# 24. Working With User Changes

The user may have uncommitted work.

Always inspect:

`git status`

before making substantial modifications.

Treat existing uncommitted changes as intentional unless there is clear evidence otherwise.

Do not:

- overwrite unrelated changes
- revert unrelated changes
- reformat the entire project unnecessarily
- perform broad automated refactoring without permission

# 25. Secrets and Sensitive Information

Never hard-code secrets.

This includes:

- passwords
- JWT secrets
- API keys
- OAuth secrets
- database credentials
- access tokens
- private keys

Do not commit secrets to Git.

Use the project's existing:

- environment variables
- configuration files
- secret management
- local development configuration

mechanisms.

# 26. Feature Development Workflow

For a new feature, follow this workflow:

### Step 1 — Understand

Identify:

- relevant backend module
- relevant frontend module
- related DTOs
- controllers
- services
- repositories
- database entities/migrations
- existing tests

### Step 2 — Plan

Explain briefly:

- what needs to change
- which files/modules are affected
- whether the API contract changes
- whether the database changes
- how the feature will be tested

For small changes, do not over-plan.

### Step 3 — Implement

Implement the smallest change that satisfies the requirement.

Follow existing conventions.

### Step 4 — Test

Run relevant tests/builds.

### Step 5 — Review

Inspect:

- Git diff
- API contract
- error handling
- security implications
- unintended changes

Then report what changed and what was tested.

# 27. Bug-Fixing Workflow

For a bug:

1. Reproduce the issue if possible.
2. Identify the failing layer.
3. Inspect logs/errors.
4. Trace the request through the system.
5. Identify the root cause.
6. Implement the smallest fix.
7. Add or update tests where appropriate.
8. Verify the original failure is resolved.

Do not patch symptoms when the root cause can be identified.

# 28. Debugging Priority

When debugging a full-stack issue, trace the request through:

Angular UI
↓
Angular service
↓
HTTP request
↓
Spring Controller
↓
DTO / validation
↓
Service / core
↓
DAO / repository
↓
PostgreSQL

Inspect each boundary rather than assuming where the problem is.

# 29. Communication Rules

When explaining a change:

- state what was changed
- explain why
- mention affected modules/files
- mention tests performed
- mention remaining issues

Do not claim that something was tested if it was not actually tested.

Do not claim that a command succeeded unless it was actually executed.

If something cannot be verified, say so explicitly.

# 30. Ambiguity

If requirements are ambiguous but the existing code provides a clear convention:

- follow the existing convention
- mention the assumption briefly

If the ambiguity could cause:

- data loss
- security problems
- API breaking changes
- architectural changes
- destructive database changes

ask for clarification before proceeding.

# 31. Minimal Change Principle

Prefer the smallest change that correctly solves the requested problem.

Do not:

- refactor unrelated code
- rename unrelated classes
- change formatting across entire files
- upgrade dependencies unnecessarily
- redesign APIs unnecessarily
- introduce new architecture without a clear requirement

A successful change should minimize regression risk.

# 32. Definition of Done

A task is considered complete only when appropriate:

- implementation is complete
- backend compiles
- frontend compiles/builds
- relevant tests pass
- API contracts remain consistent
- security has not been weakened
- database migrations are included when required
- no unrelated files were modified
- the final diff has been reviewed

Not every task requires every check, but explain which checks were performed.

# 33. Priority Order

When instructions conflict, prioritize:

1. Explicit user requirements
2. Existing project architecture
3. Existing code conventions
4. This CLAUDE.md
5. General best practices

Never change existing architecture solely because another approach is theoretically better.

# 34. Final Rule

JAPP is an existing software system, not a blank project.

Understand the existing implementation before introducing new patterns.

Prefer consistency, correctness, type safety, security, testability, and minimal changes over speed or unnecessary
abstraction.