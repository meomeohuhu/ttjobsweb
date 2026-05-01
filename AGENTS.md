# Project Agent Rules

## Project
This is a Spring Boot backend project for ttjobs.

## Tech Stack
- Java 17
- Spring Boot
- PostgreSQL
- Spring Data JPA
- JWT authentication
- BCrypt password hashing

## Database Rules
- Use table name `users`, not `user`, because `user` is problematic in PostgreSQL.
- Do not expose password hashes in API responses.
- Use DTOs for request/response when returning user data.

## Git Rules
- Never commit directly to main.
- Work only on the assigned branch.
- Keep changes minimal and reviewable.
- Before finishing, run build/test commands if available.

## Backend Rules
- Controller should not contain business logic.
- Service handles business logic.
- Repository only handles data access.
- Validate request input.
- Return proper HTTP status codes.

## Output Rules
Each agent must write its result into:
- TASK.md
- DESIGN.md
- IMPLEMENTATION.md
- REVIEW.md
- DECISION.md