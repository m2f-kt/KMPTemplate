# Template Audit & Improvement Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Clean and improve the KMP whitelabel template so anyone forking it can start developing features with zero extra setup.

**Architecture:** 4 surgical phases — each independently committable. Enhance-first (tooling, governance, setup.sh), then clean (artifacts, TODOs, sample content), then polish (CI, devcontainer, README).

**Tech Stack:** Kotlin 2.3.10, Compose Multiplatform 1.10.1, Ktor 3.4.0, Gradle 8.x, GitHub Actions, Docker Compose

**Spec:** `docs/superpowers/specs/2026-03-16-template-audit-design.md`

---

## File Structure

### Phase 1 — New files
- `/.editorconfig` — Editor formatting rules
- `/LICENSE` — Apache 2.0 license
- `/CONTRIBUTING.md` — Contributor guide
- `/CODEOWNERS` — GitHub code ownership
- `/.github/pull_request_template.md` — PR template
- `/.github/ISSUE_TEMPLATE/bug_report.md` — Bug report template
- `/.github/ISSUE_TEMPLATE/feature_request.md` — Feature request template
- `/.idea/runConfigurations/Server_Run.xml` — IntelliJ run config
- `/.idea/runConfigurations/Web_App_Run.xml` — IntelliJ run config
- `/.idea/runConfigurations/All_Tests.xml` — IntelliJ run config
- `/.idea/runConfigurations/Coverage_Report.xml` — IntelliJ run config

### Phase 1 — Modified files
- `/.gitignore:8` — Add `!.idea/runConfigurations/` exception
- `/build.gradle.kts` — Add `installGitHooks` task

### Phase 2 — Modified files
- `/setup.sh` — Expand from 9 to 14 steps, add display name prompt, fix server module list

### Phase 2 — New files
- `/.claude/skills/setup-project/SKILL.md` — Setup project skill

### Phase 3 — Deleted directories
- `/.planning/` — Project-specific planning artifacts
- `/docs/superpowers/` — GDPR specs, plans, and this plan itself (last thing deleted)

### Phase 3 — Modified files
- `/server/auth/impl/src/main/kotlin/com/m2f/server/auth/service/PasswordResetService.kt:78-80` — Replace TODO with SLF4J logging
- `/app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/card/TerminalCard.kt:432` — Replace hardcoded IP

### Phase 4 — Modified files
- `/.github/workflows/ci.yml` — Add lint, security, expanded client test jobs
- `/README.md` — Add template usage section, badges, feature guide

### Phase 4 — New files
- `/.devcontainer/devcontainer.json` — Development container config

---

## Chunk 1: Phase 1 — Tooling & Governance

### Task 1: Create .editorconfig

**Files:**
- Create: `.editorconfig`

- [ ] **Step 1: Create .editorconfig**

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

[*.{xml,json}]
indent_style = space
indent_size = 4

[Makefile]
indent_style = tab
```

Write this to `/.editorconfig` at the project root.

- [ ] **Step 2: Verify file exists**

Run: `cat .editorconfig | head -5`
Expected: Shows `root = true` and charset settings.

---

### Task 2: Create LICENSE (Apache 2.0)

**Files:**
- Create: `LICENSE`

- [ ] **Step 1: Create LICENSE file**

Write the standard Apache License 2.0 text to `/LICENSE`. Use the year `2026` and copyright holder `Template Contributors`.

- [ ] **Step 2: Verify**

Run: `head -3 LICENSE`
Expected: Shows Apache License header.

---

### Task 3: Create CONTRIBUTING.md

**Files:**
- Create: `CONTRIBUTING.md`

- [ ] **Step 1: Create CONTRIBUTING.md**

Write the following content to `/CONTRIBUTING.md`:

```markdown
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
```

- [ ] **Step 2: Verify**

Run: `wc -l CONTRIBUTING.md`
Expected: ~80-90 lines.

---

### Task 4: Create CODEOWNERS

**Files:**
- Create: `CODEOWNERS`

- [ ] **Step 1: Create CODEOWNERS**

Write to `/CODEOWNERS`:
```
# Default code owners — customize for your team
* @owner
```

- [ ] **Step 2: Verify**

Run: `cat CODEOWNERS`
Expected: Shows the default owner line.

---

### Task 5: Create GitHub PR Template

**Files:**
- Create: `.github/pull_request_template.md`

- [ ] **Step 1: Create PR template**

Write to `/.github/pull_request_template.md`:

```markdown
## Summary

<!-- Brief description of what this PR does -->

-
-

## Changes

<!-- What changed and why -->

-
-

## Testing

- [ ] All tests pass (`./gradlew testAll`)
- [ ] Static analysis passes (`./gradlew detekt`)
- [ ] Coverage maintained at 80%+
- [ ] Manual testing performed (describe below)

## Checklist

- [ ] No hardcoded secrets or credentials
- [ ] Error handling follows project patterns (Either/Raise)
- [ ] New code has tests
- [ ] Commit messages follow conventional commits format
```

- [ ] **Step 2: Verify**

Run: `cat .github/pull_request_template.md | head -3`
Expected: Shows `## Summary` header.

---

### Task 6: Create GitHub Issue Templates

**Files:**
- Create: `.github/ISSUE_TEMPLATE/bug_report.md`
- Create: `.github/ISSUE_TEMPLATE/feature_request.md`

- [ ] **Step 1: Create bug report template**

Write to `/.github/ISSUE_TEMPLATE/bug_report.md`:

```markdown
---
name: Bug Report
about: Report a bug
title: "[Bug] "
labels: bug
---

## Description

<!-- Clear description of the bug -->

## Steps to Reproduce

1.
2.
3.

## Expected Behavior

<!-- What should happen -->

## Actual Behavior

<!-- What actually happens -->

## Environment

- **OS**:
- **JDK**:
- **Platform target**: <!-- Android / iOS / Web / Desktop -->
- **Kotlin version**:
```

- [ ] **Step 2: Create feature request template**

Write to `/.github/ISSUE_TEMPLATE/feature_request.md`:

```markdown
---
name: Feature Request
about: Suggest an idea for this project
title: "[Feature] "
labels: enhancement
---

## Problem

<!-- What problem does this solve? -->

## Proposed Solution

<!-- How should it work? -->

## Alternatives Considered

<!-- What other approaches were considered? -->

## Additional Context

<!-- Screenshots, links, or other context -->
```

- [ ] **Step 3: Verify both templates exist**

Run: `ls .github/ISSUE_TEMPLATE/`
Expected: `bug_report.md` and `feature_request.md`

---

### Task 7: Add .gitignore exception for run configurations

**Files:**
- Modify: `.gitignore:8`

- [ ] **Step 1: Add exception after `.idea` line**

In `/.gitignore`, after line 8 (`.idea`), add:
```
!.idea/runConfigurations/
```

The result should be:
```
.idea
!.idea/runConfigurations/
```

- [ ] **Step 2: Verify**

Run: `grep -A1 "^\.idea$" .gitignore`
Expected: Shows `.idea` followed by `!.idea/runConfigurations/`

---

### Task 8: Create shared IntelliJ run configurations

**Files:**
- Create: `.idea/runConfigurations/Server_Run.xml`
- Create: `.idea/runConfigurations/Web_App_Run.xml`
- Create: `.idea/runConfigurations/All_Tests.xml`
- Create: `.idea/runConfigurations/Coverage_Report.xml`

- [ ] **Step 1: Create Server_Run.xml**

Write to `/.idea/runConfigurations/Server_Run.xml`:

```xml
<component name="ProjectRunConfigurationManager">
  <configuration default="false" name="Server Run" type="GradleRunConfiguration" factoryName="Gradle">
    <ExternalSystemSettings>
      <option name="executionName" />
      <option name="externalProjectPath" value="$PROJECT_DIR$" />
      <option name="externalSystemIdString" value="GRADLE" />
      <option name="scriptParameters" value="" />
      <option name="taskDescriptions">
        <list />
      </option>
      <option name="taskNames">
        <list>
          <option value=":server:run" />
        </list>
      </option>
      <option name="vmOptions" />
    </ExternalSystemSettings>
    <GradleScriptDebugEnabled>true</GradleScriptDebugEnabled>
    <method v="2" />
  </configuration>
</component>
```

- [ ] **Step 2: Create Web_App_Run.xml**

Same structure as Server_Run.xml but with:
- `name="Web App Run"`
- task: `:composeApp:wasmJsBrowserDevelopmentRun`

- [ ] **Step 3: Create All_Tests.xml**

Same structure but with:
- `name="All Tests"`
- task: `testAll`

- [ ] **Step 4: Create Coverage_Report.xml**

Same structure but with:
- `name="Coverage Report"`
- task: `koverHtmlReport`

- [ ] **Step 5: Verify all configs created**

Run: `ls .idea/runConfigurations/`
Expected: `All_Tests.xml`, `Coverage_Report.xml`, `Server_Run.xml`, `Web_App_Run.xml`

---

### Task 9: Add installGitHooks Gradle task

**Files:**
- Modify: `build.gradle.kts` (append after `verifySetup` task, around line 250)

- [ ] **Step 1: Add installGitHooks task**

Append to `/build.gradle.kts` after the `verifySetup` task (after line 250):

```kotlin
tasks.register("installGitHooks") {
    group = "dev"
    description = "Install git hooks for code quality checks (opt-in)"
    doLast {
        val hooksDir = file(".git/hooks")
        if (!hooksDir.exists()) {
            throw GradleException(".git/hooks directory not found. Is this a git repository?")
        }

        // Pre-commit: run detekt
        val preCommit = file(".git/hooks/pre-commit")
        preCommit.writeText(
            """
            |#!/usr/bin/env bash
            |set -euo pipefail
            |echo "Running detekt..."
            |./gradlew detekt --no-daemon
            |echo "Detekt passed ✅"
            """.trimMargin()
        )
        preCommit.setExecutable(true)

        // Pre-push: run all tests
        val prePush = file(".git/hooks/pre-push")
        prePush.writeText(
            """
            |#!/usr/bin/env bash
            |set -euo pipefail
            |echo "Running tests..."
            |./gradlew testAll --no-daemon
            |echo "All tests passed ✅"
            """.trimMargin()
        )
        prePush.setExecutable(true)

        println("\n  Git hooks installed ✅")
        println("  - pre-commit: runs detekt")
        println("  - pre-push: runs testAll\n")
    }
}
```

- [ ] **Step 2: Verify task registered**

Run: `./gradlew tasks --group=dev 2>&1 | grep installGitHooks`
Expected: Shows `installGitHooks - Install git hooks for code quality checks (opt-in)`

---

### Task 10: Commit Phase 1

- [ ] **Step 1: Stage all Phase 1 files**

```bash
git add \
  .editorconfig \
  LICENSE \
  CONTRIBUTING.md \
  CODEOWNERS \
  .github/pull_request_template.md \
  .github/ISSUE_TEMPLATE/bug_report.md \
  .github/ISSUE_TEMPLATE/feature_request.md \
  .gitignore \
  .idea/runConfigurations/ \
  build.gradle.kts
```

- [ ] **Step 2: Commit**

```bash
git commit -m "chore: add repository governance and developer tooling

- Add .editorconfig for consistent formatting
- Add Apache 2.0 LICENSE
- Add CONTRIBUTING.md with feature workflow guide
- Add CODEOWNERS placeholder
- Add GitHub PR and issue templates
- Add shared IntelliJ run configurations
- Add installGitHooks Gradle task (opt-in)"
```

- [ ] **Step 3: Verify clean state**

Run: `git status`
Expected: Clean working directory.

---

## Chunk 2: Phase 2 — Setup.sh Overhaul + Skill

### Task 11: Add display name prompt to setup.sh

**Files:**
- Modify: `setup.sh:24-27` (prompts section)

- [ ] **Step 1: Add display name prompt**

In `/setup.sh`, after line 26 (`read -p "  Database name (e.g., myapp_db): " DB_NAME`), add:

```bash
echo ""
read -p "  App display name (e.g., My App) [default: $PROJECT_NAME]: " DISPLAY_NAME
DISPLAY_NAME="${DISPLAY_NAME:-$PROJECT_NAME}"
```

- [ ] **Step 2: Add display name validation**

After the DB_NAME validation block (after line 55), add:

```bash
if [[ -z "$DISPLAY_NAME" ]]; then
    echo -e "${RED}error:${NC} Display name cannot be empty"
    exit 1
fi
```

- [ ] **Step 3: Add display name to dry-run summary**

In the dry-run summary section (around line 88-95), add after the `root_project` line:
```bash
echo "  display_name:   $DISPLAY_NAME"
```

---

### Task 12: Fix server module package move gap

**Files:**
- Modify: `setup.sh:206-224` (step 7 — server package moves)

The existing loop uses flat paths (`server/${mod}/src/`) but all server modules use the contract/impl/wire submodule structure (`server/${mod}/{contract,impl,wire}/src/`). The loop must iterate over submodules to actually find the directories.

- [ ] **Step 1: Replace the server module loop (lines 206-214)**

In `/setup.sh`, replace:
```bash
# Move com.m2f.server -> new server package
for mod in auth ai; do
    for variant in main test; do
        src="server/${mod}/src/${variant}/kotlin"
        if [[ -d "${src}/${OLD_SERVER_PATH}" ]]; then
            move_package_dir "$src" "$OLD_SERVER_PATH" "$NEW_SERVER_PATH"
        fi
    done
done
```

With:
```bash
# Move com.m2f.server -> new server package
for mod in auth ai groups files privacy; do
    for sub in contract impl wire; do
        for variant in main test; do
            src="server/${mod}/${sub}/src/${variant}/kotlin"
            if [[ -d "${src}/${OLD_SERVER_PATH}" ]]; then
                move_package_dir "$src" "$OLD_SERVER_PATH" "$NEW_SERVER_PATH"
            fi
        done
    done
done
```

- [ ] **Step 2: Similarly fix the core module loop (lines 216-224)**

Replace:
```bash
# Move com.m2f.core -> new core package
for mod in config database security; do
    for variant in main test; do
        src="server/core/${mod}/src/${variant}/kotlin"
```

With:
```bash
# Move com.m2f.core -> new core package
for mod in config database security; do
    for sub in contract impl wire; do
        for variant in main test; do
            src="server/core/${mod}/${sub}/src/${variant}/kotlin"
```

Also keep the existing flat path loop as a fallback (some core modules may not have submodules):
```bash
    # Also check flat structure (some core modules don't have submodules)
    for variant in main test; do
        src="server/core/${mod}/src/${variant}/kotlin"
        if [[ -d "${src}/${OLD_CORE_PATH}" ]]; then
            move_package_dir "$src" "$OLD_CORE_PATH" "$NEW_CORE_PATH"
        fi
    done
```

- [ ] **Step 3: Verify the change**

Run: `grep -A3 "for mod in" setup.sh`
Expected: Shows both loops with the `for sub in contract impl wire` inner loop.

---

### Task 13: Add new setup.sh steps 10-14

**Files:**
- Modify: `setup.sh` (after existing step 9, before post-rename verification)

- [ ] **Step 1: Update all existing step counters from [x/9] to [x/14]**

In `/setup.sh`, replace all occurrences of `/9]` with `/14]`:
- `[1/9]` → `[1/14]`
- `[2/9]` → `[2/14]`
- ... through `[9/9]` → `[9/14]`

- [ ] **Step 2: Add step 10 — Docker container names**

After the existing step 9 block (after the `.iml` deletion, around line 246), add:

```bash
# 10. Update remaining Docker container names
echo "  [10/14] Updating Docker container names..."
sed_inplace "s/container_name: template-minio$/container_name: ${PROJECT_NAME_LOWER}-minio/g" docker-compose.yml
sed_inplace "s/container_name: template-minio-init$/container_name: ${PROJECT_NAME_LOWER}-minio-init/g" docker-compose.yml
sed_inplace "s/container_name: template-mailhog$/container_name: ${PROJECT_NAME_LOWER}-mailhog/g" docker-compose.yml
```

- [ ] **Step 3: Add step 11 — OAuth & SMTP config**

```bash
# 11. Update OAuth and SMTP configuration
echo "  [11/14] Updating OAuth and SMTP configuration..."
sed_inplace "s/OAUTH_MOBILE_SCHEME=template/OAUTH_MOBILE_SCHEME=${PROJECT_NAME_LOWER}/g" .env.example
sed_inplace "s/noreply@template.local/noreply@${PROJECT_NAME_LOWER}.local/g" .env.example
```

- [ ] **Step 4: Add step 12 — App display name**

```bash
# 12. Update app display name in string resources
echo "  [12/14] Updating app display name..."
# Android res strings.xml (value: "template")
find . -path "*/androidMain/res/values/strings.xml" \
    -not -path "./.gradle/*" \
    -not -path "*/build/*" | while read -r file; do
    sed_inplace "s|<string name=\"app_name\">template</string>|<string name=\"app_name\">${DISPLAY_NAME}</string>|g" "$file"
done
# Compose resources strings.xml (value: "terminal" — fixing inconsistency)
find . -path "*/commonMain/composeResources/values/strings.xml" \
    -not -path "./.gradle/*" \
    -not -path "*/build/*" | while read -r file; do
    sed_inplace "s|<string name=\"app_name\">terminal</string>|<string name=\"app_name\">${DISPLAY_NAME}</string>|g" "$file"
done
# Spanish translations
find . -path "*/composeResources/values-es/strings.xml" \
    -not -path "./.gradle/*" \
    -not -path "*/build/*" | while read -r file; do
    if grep -q "app_name" "$file" 2>/dev/null; then
        sed_inplace "s|<string name=\"app_name\">[^<]*</string>|<string name=\"app_name\">${DISPLAY_NAME}</string>|g" "$file"
    fi
done
```

- [ ] **Step 5: Add step 13 — iOS config**

```bash
# 13. Update iOS configuration
echo "  [13/14] Updating iOS configuration..."
if [[ -f "iosApp/Configuration/Config.xcconfig" ]]; then
    sed_inplace "s/PRODUCT_NAME=template/PRODUCT_NAME=${PROJECT_NAME_LOWER}/g" iosApp/Configuration/Config.xcconfig
    sed_inplace "s|PRODUCT_BUNDLE_IDENTIFIER=com.m2f.template.template|PRODUCT_BUNDLE_IDENTIFIER=${PACKAGE_NAME}|g" iosApp/Configuration/Config.xcconfig
fi
```

- [ ] **Step 6: Add step 14 — Tooling config**

```bash
# 14. Update tooling configuration
echo "  [14/14] Updating tooling configuration..."
# .mcp.json database name
if [[ -f ".mcp.json" ]]; then
    sed_inplace "s|localhost:5436/${OLD_DB_NAME}|localhost:5436/${DB_NAME}|g" .mcp.json
fi
# README.md title and clone instructions
if [[ -f "README.md" ]]; then
    sed_inplace "s/^# Template$/# ${PROJECT_NAME}/g" README.md
    sed_inplace "s/cd template/cd ${PROJECT_NAME_LOWER}/g" README.md
fi
# Note: CLAUDE.md does not contain "Template" string literals that need renaming.
# The package references in CLAUDE.md (com.m2f.template) are already handled by step 2.
```

- [ ] **Step 7: Verify no additional changes needed for build.gradle.kts**

The `verifySetup` and `seedData` tasks in `build.gradle.kts` already reference `template-` prefixed container names, which match the default docker-compose.yml. No changes needed — setup.sh step 2 handles `.gradle.kts` files, so these references will be updated when the forker runs setup.

Note: `.mcp.json` already uses `application` as the default DB name (matching docker-compose default). Step 14 above handles renaming it during setup.

- [ ] **Step 8: Make build verification mandatory**

In `/setup.sh`, replace the optional build verification block (lines 269-276):
```bash
# --- Optional build verification ---

echo ""
read -p "  Run build verification? (y/N): " BUILD_CHECK
if [[ "$BUILD_CHECK" == "y" || "$BUILD_CHECK" == "Y" ]]; then
    echo "status: building..."
    ./gradlew build --no-daemon 2>&1 | tail -5
fi
```

With mandatory build:
```bash
# --- Build verification (mandatory) ---

echo ""
echo "status: building..."
./gradlew build --no-daemon 2>&1 | tail -20
BUILD_EXIT=$?
if [[ $BUILD_EXIT -ne 0 ]]; then
    echo -e "${RED}error:${NC} Build failed. Please fix the issues above."
    exit 1
fi
```

- [ ] **Step 9: Verify setup.sh step count**

Run: `grep -c '\[.*\/14\]' setup.sh`
Expected: `14`

---

### Task 14: Create /setup-project Claude skill

**Files:**
- Create: `.claude/skills/setup-project/SKILL.md`

- [ ] **Step 1: Create the skill file**

Write to `/.claude/skills/setup-project/SKILL.md`:

```markdown
---
name: setup-project
description: Initialize the template for a new project. Runs setup.sh wizard and then performs a post-setup sweep to verify completeness, fixing any remaining references. Use when forking this template to start a new project.
allowed-tools: Bash, Read, Grep, Edit, Write
---

# Setup Project

## Purpose

Configures this template for a new project by running the interactive setup wizard and performing a comprehensive post-setup verification.

## Workflow

### Step 1: Run the setup wizard

Run the interactive setup script:

```bash
./setup.sh
```

The user will provide:
- **Project name** (e.g., MyApp)
- **Package name** (e.g., com.company.app)
- **Database name** (e.g., myapp_db)
- **App display name** (e.g., My App)

Wait for the script to complete all 14 steps.

### Step 2: Post-setup verification sweep

After setup.sh completes, perform these checks:

1. **Grep for remaining old references:**

```bash
grep -r "com\.m2f\.\|m2f\|template" \
    --include="*.kt" --include="*.kts" --include="*.xml" \
    --include="*.json" --include="*.yml" --include="*.md" \
    --include="*.sh" --include="*.xcconfig" \
    -l . 2>/dev/null \
    | grep -v ".git/" \
    | grep -v "build/" \
    | grep -v ".gradle/" \
    | grep -v "node_modules/" \
    || echo "No remaining references found ✅"
```

2. **Verify docker-compose container names** don't contain `template-`:

```bash
grep "container_name:" docker-compose.yml
```

3. **Verify .env.example** OAuth scheme and SMTP domain updated:

```bash
grep "OAUTH_MOBILE_SCHEME\|SMTP_FROM" .env.example
```

4. **Verify strings.xml app name** updated:

```bash
grep "app_name" composeApp/src/androidMain/res/values/strings.xml
grep "app_name" composeApp/src/commonMain/composeResources/values/strings.xml
```

5. **Verify iOS config** updated:

```bash
cat iosApp/Configuration/Config.xcconfig
```

### Step 3: Fix any stragglers

If the sweep finds remaining old references:
- Analyze each occurrence
- Fix them using Edit tool
- Re-run the grep to confirm they're resolved

### Step 4: Build verification (mandatory)

Run a full build to confirm everything compiles:

```bash
./gradlew build --no-daemon
```

This MUST pass. If it fails:
- Read the error output
- Fix the compilation issues
- Re-run until green

### Step 5: Report results

Summarize:
- All replacements completed
- Any stragglers found and fixed
- Build result (pass/fail)
- Next steps: `./gradlew devSetup` → `./gradlew seedData` → start developing
```

- [ ] **Step 2: Verify skill file**

Run: `head -5 .claude/skills/setup-project/SKILL.md`
Expected: Shows frontmatter with name and description.

---

### Task 15: Commit Phase 2

- [ ] **Step 1: Stage Phase 2 files**

```bash
git add setup.sh .claude/skills/setup-project/SKILL.md
```

- [ ] **Step 2: Commit**

```bash
git commit -m "feat: expand setup.sh whitelabel wizard and add setup-project skill

- Add app display name prompt (step 12)
- Add Docker container name replacements for minio/mailhog (step 10)
- Add OAuth scheme and SMTP domain replacements (step 11)
- Add iOS Config.xcconfig updates (step 13)
- Add .mcp.json, CLAUDE.md, README.md updates (step 14)
- Fix server module package move gap (add groups, files, privacy)
- Create /setup-project Claude skill with post-setup verification
- Expand from 9 to 14 steps"
```

- [ ] **Step 3: Verify clean state**

Run: `git status`
Expected: Clean working directory.

---

## Chunk 3: Phase 3 — Cleanup Pass

### Task 16: Delete .planning/ directory

**Files:**
- Delete: `.planning/` (entire directory)

- [ ] **Step 1: Verify contents before deletion**

Run: `find .planning -type f | head -20`
Expected: Shows STATE.md, quick task files, phase tracking files.

- [ ] **Step 2: Delete the directory**

```bash
rm -rf .planning
```

- [ ] **Step 3: Verify deletion**

Run: `ls .planning 2>&1`
Expected: `No such file or directory`

---

### Task 17: Resolve PasswordResetService TODO

**Files:**
- Modify: `server/auth/impl/src/main/kotlin/com/m2f/server/auth/service/PasswordResetService.kt:1,78-80`

- [ ] **Step 1: Add SLF4J import**

In `/server/auth/impl/src/main/kotlin/com/m2f/server/auth/service/PasswordResetService.kt`, add after the existing imports (after line 23):

```kotlin
import org.slf4j.LoggerFactory
```

- [ ] **Step 2: Add logger field**

Add a companion-level logger. After line 25 (`private val RESET_TOKEN_EXPIRY = 1.hours`), add:

```kotlin
private val logger = LoggerFactory.getLogger(PasswordResetService::class.java)
```

- [ ] **Step 3: Replace TODO with actual logging**

> Note: After adding the import and logger field above, line numbers will have shifted. Use content-based matching (Edit tool), not line numbers.

Find and replace this block (search by content):
```kotlin
            } catch (_: Exception) {
                // Log failure but don't expose to user (security: don't reveal email existence)
                // TODO: Add proper logging when logging infrastructure exists
            }
```

Replace with:
```kotlin
            } catch (e: Exception) {
                // Log failure but don't expose to user (security: don't reveal email existence)
                logger.error("Failed to send password reset email", e)
            }
```

- [ ] **Step 4: Verify no TODO remains**

Run: `grep -n "TODO" server/auth/impl/src/main/kotlin/com/m2f/server/auth/service/PasswordResetService.kt`
Expected: No output (no TODOs in this file).

---

### Task 18: Clean TerminalCard preview content

**Files:**
- Modify: `app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/card/TerminalCard.kt:432`

- [ ] **Step 1: Replace hardcoded IP with generic preview text**

In `/app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/card/TerminalCard.kt`, replace line 432:

```kotlin
                TerminalText("HOST: 192.168.1.42\nPORT: 8080\nLATENCY: 12ms")
```

With:
```kotlin
                TerminalText("HOST: server.local\nPORT: 8080\nLATENCY: 12ms")
```

- [ ] **Step 2: Verify change**

Run: `grep "192.168" app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/card/TerminalCard.kt`
Expected: No output (no hardcoded IP remaining).

---

### Task 19: Final verification sweep

- [ ] **Step 1: Check for m2f references in non-source files**

Run: `grep -r "m2f" --include="*.md" --include="*.json" --include="*.yml" --include="*.sh" --include="*.xml" --include="*.xcconfig" -l . | grep -v ".git/" | grep -v "build/" | grep -v ".gradle/" | grep -v ".planning/"`

Review each result. Files that **should** contain `m2f` references (because setup.sh will rename them):
- Source `.kt` and `.kts` files — OK (setup.sh handles these)
- `CLAUDE.md` — OK (setup.sh step 14 handles this)
- `setup.sh` itself — OK (contains the old package names as replacement targets)

Flag any files that setup.sh does NOT handle.

- [ ] **Step 2: Verify .claude/ hooks don't have hardcoded packages**

Run: `grep -r "com.m2f\|m2f" .claude/ --include="*.js" --include="*.sh" --include="*.md" -l`

Review results. Skills and agents that reference `com.m2f.template` patterns as examples are fine — they serve as templates. But hooks (`.js`, `.sh`) should not have hardcoded package references that would break after renaming.

- [ ] **Step 3: Verify seed data is generic**

The file `/dev-scripts/seed-dev-data.sql` uses `dev@example.com` / `Dev User` — these are generic and appropriate for a template. No changes needed.

---

### Task 20: Commit Phase 3

- [ ] **Step 1: Stage Phase 3 changes**

```bash
git rm -r .planning
git add server/auth/impl/src/main/kotlin/com/m2f/server/auth/service/PasswordResetService.kt
git add app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/card/TerminalCard.kt
git status  # Review what will be committed
```

Verify the staged changes include:
- Deleted `.planning/` files
- Modified `PasswordResetService.kt`
- Modified `TerminalCard.kt`

- [ ] **Step 2: Commit**

```bash
git commit -m "chore: remove project-specific artifacts and resolve TODOs

- Delete .planning/ directory (GDPR task tracking)
- Replace logging TODO with SLF4J in PasswordResetService
- Replace hardcoded IP in TerminalCard preview
- Verify no stale references in hooks or config"
```

- [ ] **Step 3: Verify clean state**

Run: `git status`
Expected: Clean working directory.

---

## Chunk 4: Phase 4 — CI & DX Polish

### Task 21: Add detekt and security scanning to CI

**Files:**
- Modify: `.github/workflows/ci.yml`

- [ ] **Step 1: Add lint job**

In `/.github/workflows/ci.yml`, add a new job after the existing `test-server` job (after line 31):

```yaml
  lint:
    name: Static Analysis
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: 17
      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4
      - name: Run detekt
        run: ./gradlew detekt --no-daemon
```

- [ ] **Step 2: Add security scanning job**

Add after the lint job:

```yaml
  security:
    name: Dependency Security Scan
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Run Trivy vulnerability scanner
        uses: aquasecurity/trivy-action@master
        with:
          scan-type: fs
          scan-ref: .
          severity: CRITICAL,HIGH
```

- [ ] **Step 3: Verify YAML syntax**

Run: `python3 -c "import yaml; yaml.safe_load(open('.github/workflows/ci.yml'))"`
Expected: No errors.

---

### Task 22: Expand client test coverage in CI

**Files:**
- Modify: `.github/workflows/ci.yml` (test-client job)

- [ ] **Step 1: Check which modules have tests**

Modules with confirmed test files:
- `app:auth:impl` — 3 tests (Login, Register, ForgotPassword)
- `app:admin:impl` — 2 tests (AdminPanel, RegisterMember)
- `app:dashboard:impl` — 1 test (Dashboard)
- `app:profile:impl` — 1 test (Profile)
- `app:privacy:impl` — 4 tests (ConsentGate, LegalDocument, PrivacySettings, AccountDeletion)

Modules WITHOUT tests (skip in CI):
- `app:documents:impl` — No tests

- [ ] **Step 2: Add app module tests to CI**

In the `test-client` job, update the "Run KMP JVM tests" step to include app modules. Replace the existing run command (lines 48-52):

```yaml
      - name: Run KMP JVM tests with coverage verification
        run: |
          ./gradlew \
            :core:models:jvmTest :core:models:koverVerify \
            :core:mvi:jvmTest   :core:mvi:koverVerify \
            :shared:jvmTest     :shared:koverVerify \
            :app:auth:impl:jvmTest \
            :app:admin:impl:jvmTest \
            :app:dashboard:impl:jvmTest \
            :app:profile:impl:jvmTest \
            :app:privacy:impl:jvmTest \
            --no-daemon
```

- [ ] **Step 3: Update coverage report generation to include app modules**

Update the HTML report step to include app modules:
```yaml
      - name: Generate KMP HTML coverage reports
        if: always()
        run: |
          ./gradlew \
            :core:models:koverHtmlReport \
            :core:mvi:koverHtmlReport \
            :shared:koverHtmlReport \
            :app:auth:impl:koverHtmlReport \
            :app:admin:impl:koverHtmlReport \
            :app:dashboard:impl:koverHtmlReport \
            :app:profile:impl:koverHtmlReport \
            :app:privacy:impl:koverHtmlReport \
            --no-daemon
```

Similarly update the XML report step with the same module list (replace `koverHtmlReport` with `koverXmlReport`).

Update the artifact upload paths to include app module reports:
```yaml
      - name: Upload KMP coverage reports
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: kmp-coverage-reports
          path: |
            core/models/build/reports/kover/
            core/mvi/build/reports/kover/
            shared/build/reports/kover/
            app/auth/impl/build/reports/kover/
            app/admin/impl/build/reports/kover/
            app/dashboard/impl/build/reports/kover/
            app/profile/impl/build/reports/kover/
            app/privacy/impl/build/reports/kover/
```

---

### Task 23: Create devcontainer

**Files:**
- Create: `.devcontainer/devcontainer.json`

- [ ] **Step 1: Create devcontainer directory and config**

Write to `/.devcontainer/devcontainer.json`:

```json
{
  "name": "KMP Template",
  "image": "mcr.microsoft.com/devcontainers/java:17",
  "features": {
    "ghcr.io/devcontainers/features/docker-in-docker:2": {}
  },
  "postCreateCommand": "./gradlew devSetup",
  "forwardPorts": [8080, 5436, 9002, 9003, 1025, 8025],
  "portsAttributes": {
    "8080": { "label": "Server API" },
    "5436": { "label": "PostgreSQL" },
    "9002": { "label": "MinIO API" },
    "9003": { "label": "MinIO Console" },
    "1025": { "label": "MailHog SMTP" },
    "8025": { "label": "MailHog Web" }
  },
  "customizations": {
    "vscode": {
      "extensions": [
        "fwcd.kotlin",
        "vscjava.vscode-gradle"
      ]
    }
  }
}
```

- [ ] **Step 2: Verify JSON is valid**

Run: `python3 -c "import json; json.load(open('.devcontainer/devcontainer.json'))"`
Expected: No errors.

---

### Task 24: Improve README.md

**Files:**
- Modify: `README.md`

- [ ] **Step 1: Add badges at the top**

At the very top of `/README.md` (before `# Template`), add:

```markdown
[![CI](../../actions/workflows/ci.yml/badge.svg)](../../actions/workflows/ci.yml)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)

```

- [ ] **Step 2: Add "Using This Template" section**

After the `# Template` header and description paragraph, add:

```markdown
## Using This Template

1. **Fork** this repository
2. **Configure** your project: `./setup.sh` (sets project name, package, database, display name)
3. **Start infrastructure**: `./gradlew devSetup`
4. **Seed demo data**: `./gradlew seedData`
5. **Start developing**: Use Claude skills to scaffold features (see below)

```

- [ ] **Step 3: Add "Adding a Feature" section**

After the "Documentation" section, add:

```markdown
## Adding a Feature

Use Claude Code skills to scaffold new features following project conventions:

| Skill | What it creates |
|---|---|
| `/create-app-module` | Client feature (contract/impl/wire) with ViewModel, Screen, tests |
| `/create-server-module` | Server feature with routes, service, repository, migrations |
| `/feature` | Full-stack: both client and server modules |
| `/compose-screen` | Single Compose screen with callbacks pattern |
| `/ktor-endpoint` | Single Ktor route handler |
| `/mvi-viewmodel` | MVI ViewModel boilerplate |

```

- [ ] **Step 4: Verify README renders correctly**

Run: `head -30 README.md`
Expected: Shows badges, title, template usage section.

---

### Task 25: Commit Phase 4

- [ ] **Step 1: Stage Phase 4 files**

```bash
git add \
  .github/workflows/ci.yml \
  .devcontainer/devcontainer.json \
  README.md
```

- [ ] **Step 2: Commit**

```bash
git commit -m "ci: add security scanning, detekt, devcontainer, and README polish

- Add detekt static analysis CI job
- Add Trivy dependency security scanning CI job
- Expand client test coverage to all app modules with tests
- Add devcontainer for GitHub Codespaces / VS Code remote
- Add template usage guide, feature scaffolding table, and badges to README"
```

- [ ] **Step 3: Verify clean state**

Run: `git status`
Expected: Clean working directory.

---

### Task 26: Delete docs/superpowers/ (final cleanup)

> This is the last step — only execute after all 4 phases are verified.

**Files:**
- Delete: `docs/superpowers/` (specs, plans — including this plan)

- [ ] **Step 1: Verify all phases are committed**

Run: `git log --oneline -5`
Expected: Shows 4 phase commits plus the spec commits.

- [ ] **Step 2: Delete docs/superpowers/**

```bash
rm -rf docs/superpowers
```

- [ ] **Step 3: Commit**

```bash
git rm -r docs/superpowers
git commit -m "chore: remove audit spec and plan (completed)"
```

- [ ] **Step 4: Final verification**

Run: `git log --oneline -10`
Expected: Shows all audit commits in order.

Run: `git status`
Expected: Clean working directory.
