---
status: complete
phase: 22-developer-onboarding
source: git commit 966a9c2
started: 2026-03-08T12:00:00Z
updated: 2026-03-08T12:45:00Z
---

## Current Test

[testing complete]

## Tests

### 1. Cold Start Smoke Test
expected: Kill any running server/service. Clear ephemeral state. Start the application from scratch. Server boots without errors, seed/migration completes, and the /health endpoint returns live data.
result: issue
reported: "devSetup failed — postgres unhealthy because 02-seed-dev-data.sql in init-scripts referenced non-existent users table during container init. Also config cache warning on checkSetup."
severity: blocker
fix-applied: "Moved seed script to dev-scripts/, added notCompatibleWithConfigurationCache to checkSetup/verifySetup. devSetup now succeeds."

### 2. .env File Support
expected: Create a .env file from .env.example, fill in values, and start the server. Environment variables from .env are picked up without needing to export them manually. System env vars take precedence over .env values.
result: pass

### 3. .env.example Completeness
expected: .env.example exists at project root with all required environment variables documented by section (database, MinIO, SMTP, etc.). Comments explain each variable's purpose.
result: pass

### 4. Gradle checkSetup Task
expected: Run ./gradlew checkSetup. It checks prerequisites (Java version, Docker, etc.) and reports what's missing or confirms everything is ready.
result: pass

### 5. Gradle devUp Task
expected: Run ./gradlew devUp. It starts Docker containers (database, MinIO, SMTP) needed for local development.
result: pass

### 6. Gradle seedData Task
expected: Run ./gradlew seedData. It seeds the database with demo data including a dev user (dev@example.com / password).
result: pass

### 7. Gradle devSetup Task
expected: Run ./gradlew devSetup. It runs the full setup sequence (devUp + seedData + any other initialization) in one command.
result: pass

### 8. Health Endpoint
expected: Hit GET /health. Returns JSON showing connectivity status for database, MinIO, and SMTP. Each service shows healthy/unhealthy status.
result: pass

### 9. README Quick Start
expected: README.md contains a clear quick start section with numbered steps to get the project running from scratch. Includes architecture overview and command reference.
result: pass

### 10. Architecture Documentation
expected: docs/ARCHITECTURE.md exists with module structure, data flow diagrams, and an "add a feature" guide explaining how to extend the codebase.
result: pass

### 11. Getting Started Guide
expected: docs/GETTING-STARTED.md exists with step-by-step walkthrough from clone to running server, including troubleshooting section for common issues.
result: pass

### 12. Seed Data
expected: After running seed, a demo user (dev@example.com / password) exists in the database and can be used to log in or authenticate against the API.
result: pass

## Summary

total: 12
passed: 11
issues: 1
pending: 0
skipped: 0

## Gaps

- truth: "devSetup runs successfully from clean state"
  status: fixed
  reason: "User reported: devSetup failed — postgres unhealthy because seed script in init-scripts referenced non-existent users table. Also config cache warning."
  severity: blocker
  test: 1
  root_cause: "02-seed-dev-data.sql placed in init-scripts/ (mounted to docker-entrypoint-initdb.d) ran before app created tables. Bogus bcrypt hash for 'password'. checkSetup not annotated for config cache. Port 8080 check didn't recognize own JVM server."
  artifacts:
    - path: "init-scripts/02-seed-dev-data.sql"
      issue: "Seed script ran during postgres init before users table existed"
    - path: "build.gradle.kts"
      issue: "checkSetup missing notCompatibleWithConfigurationCache, isOwnDockerContainer didn't detect JVM server"
    - path: "dev-scripts/seed-dev-data.sql"
      issue: "Bogus bcrypt hash didn't match 'password'"
  missing: []
