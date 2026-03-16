# Contributing

## Prerequisites

- **JDK 11+** — [Download](https://adoptium.net/)
- **Docker Desktop** — [Download](https://docs.docker.com/get-docker/)

Run `./gradlew checkSetup` to verify your environment.

## Getting Started

1. Fork this repository
2. Run `./setup.sh` to configure project name, package, and database
3. Run `./gradlew devSetup` to start Docker services
4. Run `./gradlew seedData` to create a demo user

## Adding Features

### Client Feature Module

Use the Claude skill: `/create-app-module`

This scaffolds the 3-submodule structure (contract/impl/wire) with:
- Route definition
- ViewModel with MVI pattern
- Compose Screen with callbacks
- Koin module and navigation wiring
- Test file with ViewModelTest base

### Server Feature Module

Use the Claude skill: `/create-server-module`

This scaffolds:
- Route definitions with `conduit`/`conduitAuth` helpers
- Service with Arrow Raise context
- Repository with Exposed R2DBC
- Table definitions
- Koin module
- Database migrations

### Full-Stack Feature

Use `/feature` to scaffold both client and server modules together.

## Architecture

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for detailed module documentation.

## Code Standards

### Testing

- **Minimum coverage**: 80% (enforced by Kover)
- **Framework**: Kotest assertions only (`shouldBe`, `shouldBeRight`, etc.) — never JUnit
- **Workflow**: TDD — write tests first, implement, refactor
- **Test DSL**: `viewModel.test { intent(...); model(...); event(...) }`
- **Fakes**: `fakeSdk { auth { login { ... } } }`

### Patterns

- **MVI**: Intent → take() → sendMutation/sendEvent → reduce() → Model
- **Error handling**: `Either<AppError, T>` on client, `context(raise: Raise<DomainError>)` on server
- **Immutability**: Always create new objects, never mutate
- **Serialization**: `@Serializable` on all DTOs

### Commit Messages

```
<type>: <description>
```

Types: `feat`, `fix`, `refactor`, `docs`, `test`, `chore`, `perf`, `ci`

## Pull Request Process

1. Create a feature branch from `master`
2. Write tests first (TDD)
3. Implement the feature
4. Ensure all tests pass: `./gradlew testAll`
5. Ensure static analysis passes: `./gradlew detekt`
6. Ensure 80%+ coverage: `./gradlew koverHtmlReport`
7. Open a PR with a clear description
