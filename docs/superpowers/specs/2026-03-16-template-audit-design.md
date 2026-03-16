# Template Project Audit & Improvement Design

**Date**: 2026-03-16
**Status**: Approved
**Approach**: Surgical Passes (4 phases, enhance-first-then-clean)

## Context

This project is a KMP + Compose Multiplatform whitelabel template application with a Ktor backend. It targets Android, iOS, Web (WASM), and Desktop (JVM). The goal of this audit is to ensure the template is clean, clear, and best-in-class — so anyone forking it can start developing features immediately with zero extra setup.

### Decisions

- **Feature strategy**: Full showcase (B) — keep all features (auth, admin, dashboard, documents, profile, privacy, AI, files) as reference implementations. Forkers delete what they don't need.
- **Priority order**: Enhance first, then clean — set up tooling and governance before doing the cleanup pass.
- **Setup.sh build verification**: Mandatory (not optional) — `./gradlew build` runs as final confirmation after setup.

## Current State Summary

### Strengths
- 40+ well-organized modules with consistent contract/impl/wire pattern
- Excellent Gradle automation (devSetup, devUp, testAll, checkSetup, verifySetup, seedData)
- Solid Claude Code integration (4 hooks, 3 agents, 9 skills)
- Modern dependency versions (Kotlin 2.3.10, Compose 1.10.1, Ktor 3.4.0)
- Comprehensive .env.example with all service configurations
- Sophisticated setup.sh whitelabel wizard (289 lines, platform-aware)

### Gaps Identified
- Missing repository governance: no .editorconfig, LICENSE, CONTRIBUTING.md, CODEOWNERS
- Missing GitHub templates: no PR template, no issue templates
- Missing developer tooling: no shared IDE run configs, no git hooks
- setup.sh incomplete: doesn't handle Docker container names (minio, mailhog), OAuth scheme, SMTP domain, app display name, iOS bundle ID, .mcp.json, README, CLAUDE.md
- Project-specific artifacts polluting template: `.planning/` (9 GDPR tasks), `docs/superpowers/` (GDPR specs)
- Sample content: hardcoded IP in TerminalCard preview, unresolved TODO in PasswordResetService
- CI gaps: no detekt job, no security scanning, client tests only cover 3 of 9+ modules
- No devcontainer for Codespaces/remote development

---

## Phase 1: Tooling & Governance

**Goal**: Add all missing repository governance and developer tooling.
**Risk**: Low

### 1.1 Repository Governance Files

#### `.editorconfig`
```ini
root = true

[*]
charset = utf-8
end_of_line = lf
insert_final_newline = true
trim_trailing_whitespace = true

[*.{kt,kts}]
indent_style = space
indent_size = 4
max_line_length = 120

[*.{yml,yaml}]
indent_style = space
indent_size = 2

[*.md]
trim_trailing_whitespace = false
```

#### `LICENSE`
Apache 2.0 license file.

#### `CONTRIBUTING.md`
Contents:
- Prerequisites (same as README)
- How to add a client feature module (references `/create-app-module` skill)
- How to add a server feature module (references `/create-server-module` skill)
- Testing expectations: 80% coverage, TDD workflow, Kotest assertions only
- PR process: conventional commits, code review checklist
- Architecture overview reference (link to docs/ARCHITECTURE.md)

#### `CODEOWNERS`
```
* @owner
```
Placeholder for forkers to customize with their team structure.

### 1.2 GitHub Templates

#### `.github/pull_request_template.md`
Sections: Summary (bullet points), Changes (what changed and why), Testing (checklist), Checklist (tests pass, no hardcoded secrets, coverage maintained).

#### `.github/ISSUE_TEMPLATE/bug_report.md`
Fields: Description, Steps to reproduce, Expected behavior, Actual behavior, Environment (OS, JDK, platform target).

#### `.github/ISSUE_TEMPLATE/feature_request.md`
Fields: Problem statement, Proposed solution, Alternatives considered, Additional context.

### 1.3 Developer Tooling

#### Shared IntelliJ Run Configurations (`.idea/runConfigurations/`)
- **Server_Run.xml** — `:server:run` Gradle task
- **Web_App_Run.xml** — `:composeApp:wasmJsBrowserDevelopmentRun` Gradle task
- **All_Tests.xml** — `testAll` Gradle task
- **Coverage_Report.xml** — `koverHtmlReport` Gradle task

#### Git Hooks via Gradle
Add `installGitHooks` task to root `build.gradle.kts`:
- Installs `.git/hooks/pre-commit` — runs `./gradlew detekt`
- Installs `.git/hooks/pre-push` — runs `./gradlew testAll`
- Auto-runs as part of `devSetup`

---

## Phase 2: Setup.sh Overhaul + Skill

**Goal**: Make setup.sh handle all whitelabel replacements. Create a Claude skill that wraps it with post-setup verification.
**Risk**: Medium

### 2.1 Expand setup.sh

Add a new interactive prompt:
- **App display name** (defaults to project name) — human-readable name for UI

New replacement operations to add:

| # | Item | Old value | New value | File(s) |
|---|------|-----------|-----------|---------|
| 10 | MinIO container name | `template-minio` | `{project-lower}-minio` | `docker-compose.yml` |
| 11 | MinIO init container name | `template-minio-init` | `{project-lower}-minio-init` | `docker-compose.yml` |
| 12 | MailHog container name | `template-mailhog` | `{project-lower}-mailhog` | `docker-compose.yml` |
| 13 | OAuth mobile scheme | `OAUTH_MOBILE_SCHEME=template` | `OAUTH_MOBILE_SCHEME={project-lower}` | `.env.example` |
| 14 | SMTP from domain | `noreply@template.local` | `noreply@{project-lower}.local` | `.env.example` |
| 15 | App display name | `"Template"` in strings.xml | `"{display-name}"` | `composeResources/values/strings.xml`, `values-es/strings.xml` |
| 16 | iOS bundle identifier | `com.m2f.template` | `{package-name}` | `iosApp/` config files |
| 17 | .mcp.json DB name | `application` | `{db-name}` | `.mcp.json` |
| 18 | CLAUDE.md project name | `"Template"` references | `"{project-name}"` | `CLAUDE.md` |
| 19 | README title + clone | `# Template`, `cd template` | `# {project-name}`, `cd {project-lower}` | `README.md` |

Update step counter from `[1/9]` to `[1/19]` and renumber.

### 2.2 Create `/setup-project` Claude Skill

Location: `.claude/skills/setup-project/SKILL.md`

Workflow:
1. Run `./setup.sh` via Bash (user interacts with prompts)
2. After setup.sh completes, do a post-setup sweep:
   - Grep for remaining `com.m2f`, `m2f`, `template` references (excluding `.git/`, `build/`, `.gradle/`)
   - Check docker-compose container names all use new project name
   - Verify `.env.example` values updated (OAuth scheme, SMTP domain)
   - Verify strings.xml app name updated
3. Report any stragglers and fix them
4. Run `./gradlew build` as mandatory final verification
5. Report success or failure with actionable next steps

---

## Phase 3: Cleanup Pass

**Goal**: Remove all project-specific artifacts and resolve quality issues.
**Risk**: Low

### 3.1 Remove Project-Specific Planning Artifacts
- Delete `.planning/` directory entirely (STATE.md, quick/ tasks 1-9, phases/)
- Delete `docs/superpowers/` directory (GDPR specs and plans)
- Note: this spec file itself lives in `docs/superpowers/specs/` — it will be deleted as part of the cleanup since it documents completed work

### 3.2 Resolve TODOs
- **PasswordResetService.kt:80** — `// TODO: Add proper logging` — Replace with actual Log4j logging. The server already has Log4j configured with JSON layout. Add `private val logger = LoggerFactory.getLogger(...)` and log the exception.
- **Models.kt:15** — `// TODO: When Koog adds outputDimensionality...` — Keep as-is. This is a legitimate upstream dependency note about Koog 0.6.2 limitations.

### 3.3 Clean Sample/Placeholder Content
- **TerminalCard.kt:432** — Replace hardcoded `"HOST: 192.168.1.42\nPORT: 8080\nLATENCY: 12ms"` with generic preview text or use string resources
- **dev-scripts/seed-dev-data.sql** — Verify uses generic demo data, not project-specific

### 3.4 Final Verification Sweep
- Grep entire project for `m2f` references outside of `.git/` and `build/` directories
- Verify `.claude/` internal files (hooks JS, agents, skills) don't contain hardcoded package references
- Ensure setup.sh verification step catches all remaining references

---

## Phase 4: CI & DX Polish

**Goal**: Enhance CI pipeline, add devcontainer, polish README for template distribution.
**Risk**: Low

### 4.1 Enhance CI Workflow

#### Add detekt job
New job in `.github/workflows/ci.yml`:
```yaml
lint:
  name: Static Analysis
  runs-on: ubuntu-latest
  steps:
    - uses: actions/checkout@v4
    - uses: actions/setup-java@v4
      with: { distribution: temurin, java-version: 17 }
    - uses: gradle/actions/setup-gradle@v4
    - run: ./gradlew detekt --no-daemon
```

#### Add dependency security scanning
New job using OWASP DependencyCheck or Trivy:
```yaml
security:
  name: Dependency Security Scan
  runs-on: ubuntu-latest
  steps:
    - uses: actions/checkout@v4
    - uses: aquasecurity/trivy-action@master
      with:
        scan-type: fs
        scan-ref: .
        severity: CRITICAL,HIGH
```

#### Expand client test coverage
Update `test-client` job to include all app feature modules:
```
:app:auth:impl:jvmTest
:app:admin:impl:jvmTest
:app:dashboard:impl:jvmTest
:app:documents:impl:jvmTest
:app:profile:impl:jvmTest
:app:privacy:impl:jvmTest
```

### 4.2 Devcontainer

Create `.devcontainer/devcontainer.json`:
- Base image: JDK 17
- Features: Docker-in-Docker
- Post-create command: `./gradlew devSetup`
- VS Code extensions: Kotlin Language, Gradle for Java
- Forward ports: 8080, 5436, 9002, 9003, 1025, 8025

### 4.3 README Improvements

Add to the top of README.md:

#### "Using This Template" section
1. Fork this repository
2. Run `./setup.sh` to configure project name, package, and database
3. Run `./gradlew devSetup` to start infrastructure
4. Start building features with `/create-app-module` and `/create-server-module` Claude skills

#### Badge placeholders
```markdown
[![CI](../../actions/workflows/ci.yml/badge.svg)](../../actions/workflows/ci.yml)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
```

#### "Adding a Feature" quick reference
- Client: `/create-app-module` → creates contract/impl/wire scaffold
- Server: `/create-server-module` → creates routes/service/repository scaffold
- Full stack: `/feature` → combines both

### 4.4 .mcp.json Cleanup
- Ensure `.mcp.json` uses `application` as default DB name (matches docker-compose default)
- setup.sh Phase 2 will handle renaming this during project setup

---

## Implementation Order

```
Phase 1 (Tooling & Governance)
  └─► commit: "chore: add repository governance and developer tooling"
Phase 2 (Setup.sh + Skill)
  └─► commit: "feat: expand setup.sh whitelabel wizard and add setup-project skill"
Phase 3 (Cleanup)
  └─► commit: "chore: remove project-specific artifacts and resolve TODOs"
Phase 4 (CI & DX Polish)
  └─► commit: "ci: add security scanning, detekt, devcontainer, and README polish"
```

Each phase is independently committable and revertable. Phases build on each other but each leaves the project in a working state.
