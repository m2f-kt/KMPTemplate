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
    || echo "No remaining references found"
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
