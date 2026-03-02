# Template

Kotlin Multiplatform full-stack template with authentication, AI agents, file storage, and email — targeting Android, iOS, Web (WASM), Desktop (JVM), and Ktor server.

## Quick Start

```bash
git clone <repo-url> && cd template
./gradlew devSetup          # Check prerequisites + start Docker services
./gradlew seedData           # Create demo user (dev@example.com / password)
./gradlew :server:run        # Start the API server on :8080
./gradlew :composeApp:wasmJsBrowserDevelopmentRun  # Start web app
```

## Prerequisites

- **JDK 11+** — [Download](https://adoptium.net/)
- **Docker Desktop** — [Download](https://docs.docker.com/get-docker/) (includes Docker Compose)

Run `./gradlew checkSetup` to verify prerequisites.

## Architecture Overview

```
composeApp (Compose Multiplatform UI)
├── app:auth          — Login, registration, password reset screens
├── app:admin         — Admin panel (user management, invitations)
├── app:dashboard     — Main dashboard
├── app:documents     — Document management UI
├── app:profile       — User profile screens
├── app:designsystem  — Shared UI components, theming
└── core:sdk          — API client (Ktor) → server

server (Ktor backend)
├── server:auth       — JWT auth, OAuth, password reset, user management
├── server:groups     — Group CRUD, membership, invitations
├── server:files      — S3/MinIO file upload and retrieval
├── server:ai         — AI agents (chat, assistant), RAG pipeline
└── server:core
    ├── config        — Environment configuration (Env.kt, dotenv)
    ├── database      — R2DBC setup, migrations, vector column types
    └── security      — JWT validation, authentication

shared                — DTOs, request/response models (multiplatform)
core:models           — Domain models shared across all modules
core:mvi              — MVI ViewModel base classes
core:testing          — Test utilities and DSL
core:storage          — Multiplatform key-value storage
```

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for detailed module documentation.

## Dev Commands

| Command | Description |
|---|---|
| `./gradlew devSetup` | First-time setup (checks + Docker) |
| `./gradlew devUp` | Start Docker services |
| `./gradlew seedData` | Insert demo user |
| `./gradlew checkSetup` | Verify prerequisites |
| `./gradlew verifySetup` | Verify all services running |
| `./gradlew testAll` | Run all tests |
| `./gradlew :server:run` | Start the server |
| `./gradlew :composeApp:wasmJsBrowserDevelopmentRun` | Start web app |

## Services

| Service | Port | UI |
|---|---|---|
| PostgreSQL (pgvector) | 5436 | — |
| MinIO (S3) | 9002 (API) | Console: [localhost:9003](http://localhost:9003) |
| MailHog (SMTP) | 1025 | Web UI: [localhost:8025](http://localhost:8025) |
| Server | 8080 | API: [localhost:8080/health](http://localhost:8080/health) |

Default credentials: MinIO `minioadmin`/`minioadmin`, PostgreSQL `postgres`/`postgres`, Demo user `dev@example.com`/`password`

## Documentation

- **[Getting Started](docs/GETTING-STARTED.md)** — Step-by-step setup walkthrough with troubleshooting
- **[Architecture](docs/ARCHITECTURE.md)** — Module structure, data flow, and how to add a feature

## Configuration

Copy `.env.example` to `.env` to customize configuration (optional — defaults work for local dev):

```bash
cp .env.example .env
```

The server loads environment variables from `.env` via [dotenv-kotlin](https://github.com/cdimascio/dotenv-kotlin), falling back to system environment variables. See `.env.example` for all available settings.

## Tech Stack

- **Kotlin 2.3** / **Kotlin Multiplatform**
- **Ktor 3.4** — Server + HTTP client
- **Compose Multiplatform 1.10** — Shared UI (Android, iOS, Web, Desktop)
- **Exposed R2DBC 1.0** — Reactive database access
- **Arrow** — Functional programming (Either, Raise)
- **Koin** — Dependency injection
- **Koog 0.6** — AI agent framework (JetBrains)
- **pgvector** — Vector similarity search for RAG
