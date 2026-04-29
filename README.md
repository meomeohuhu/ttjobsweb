# Project TTJobs

This repository contains the backend and AI service for the TTJobs project.

## Project Structure

- `backend/`: Canonical Spring Boot backend module.
- `ai-service/`: AI service (Python/Flask).
- `frontend/`: Git submodule pointing to the frontend repository.

## Backend Development

The backend is a Spring Boot application managed with Maven.

### Canonical Backend Path

All backend commands should be executed from the `backend/` directory.

### Commands

To build, test, or run the backend:

```bash
cd backend
./mvnw test               # Run unit tests
./mvnw verify             # Build and run integration tests
./mvnw spring-boot:run    # Run the application locally
```

### Root Maven Wrapper

While there is a Maven wrapper (`mvnw`) at the project root, it is configured for the root `pom.xml`. For backend-specific development and to ensure all plugins and configurations are correctly applied, it is recommended to use the Maven wrapper within the `backend/` directory.

## Frontend Development

The `frontend/` directory is a Git submodule pointing to:
[https://github.com/meomeohuhu/frontend-ttjobs.git](https://github.com/meomeohuhu/frontend-ttjobs.git)

**Important:** Source changes for the frontend must be made in the dedicated frontend repository. Do not commit frontend source changes directly to this repository unless you are updating the submodule pointer.

## AI Service

The AI service is located in `ai-service/`. It is a Python-based service.
