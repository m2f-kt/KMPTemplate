#!/usr/bin/env bash
set -euo pipefail

# Terminal colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Platform-aware sed in-place
if [[ "$(uname)" == "Darwin" ]]; then
    sed_inplace() { sed -i '' "$@"; }
else
    sed_inplace() { sed -i "$@"; }
fi

echo ""
echo -e "${GREEN}> terminal setup_project${NC}"
echo "// Project configuration wizard"
echo ""

# --- Interactive prompts ---

read -p "  Project name (e.g., MyApp): " PROJECT_NAME
read -p "  Package name (e.g., com.company.app): " PACKAGE_NAME
read -p "  Database name (e.g., myapp_db): " DB_NAME

echo ""
read -p "  App display name (e.g., My App) [default: $PROJECT_NAME]: " DISPLAY_NAME
DISPLAY_NAME="${DISPLAY_NAME:-$PROJECT_NAME}"

echo ""

# --- Input validation ---

if [[ -z "$PROJECT_NAME" ]]; then
    echo -e "${RED}error:${NC} Project name cannot be empty"
    exit 1
fi

if ! [[ "$PROJECT_NAME" =~ ^[a-zA-Z0-9\ _-]+$ ]]; then
    echo -e "${RED}error:${NC} Project name must be alphanumeric (spaces, hyphens, underscores allowed)"
    exit 1
fi

if ! [[ "$PACKAGE_NAME" =~ ^[a-z][a-z0-9]*(\.[a-z][a-z0-9]*)+$ ]]; then
    echo -e "${RED}error:${NC} Package name must match pattern: com.company.app (lowercase, at least two segments)"
    exit 1
fi

if [[ -z "$DB_NAME" ]]; then
    echo -e "${RED}error:${NC} Database name cannot be empty"
    exit 1
fi

if ! [[ "$DB_NAME" =~ ^[a-zA-Z0-9_]+$ ]]; then
    echo -e "${RED}error:${NC} Database name must be alphanumeric with underscores only (no spaces)"
    exit 1
fi

if [[ -z "$DISPLAY_NAME" ]]; then
    echo -e "${RED}error:${NC} Display name cannot be empty"
    exit 1
fi

# --- Derived values ---

PACKAGE_PATH="${PACKAGE_NAME//./\/}"

OLD_PACKAGE="com.m2f.template"
OLD_PACKAGE_PATH="com/m2f/template"

OLD_SERVER_PACKAGE="com.m2f.server"
OLD_SERVER_PATH="com/m2f/server"

OLD_CORE_PACKAGE="com.m2f.core"
OLD_CORE_PATH="com/m2f/core"

# Extract first two segments from new package for server/core mapping
# e.g., com.company.app -> com.company
IFS='.' read -ra PACKAGE_SEGMENTS <<< "$PACKAGE_NAME"
FIRST_TWO="${PACKAGE_SEGMENTS[0]}.${PACKAGE_SEGMENTS[1]}"
FIRST_TWO_PATH="${PACKAGE_SEGMENTS[0]}/${PACKAGE_SEGMENTS[1]}"

NEW_SERVER_PACKAGE="${FIRST_TWO}.server"
NEW_SERVER_PATH="${FIRST_TWO_PATH}/server"

NEW_CORE_PACKAGE="${FIRST_TWO}.core"
NEW_CORE_PATH="${FIRST_TWO_PATH}/core"

PROJECT_NAME_LOWER=$(echo "$PROJECT_NAME" | tr '[:upper:]' '[:lower:]' | tr ' ' '-')

OLD_DB_NAME="application"

# --- Dry-run summary ---

echo "status: preview"
echo "  project_name:   $PROJECT_NAME"
echo "  package:        $OLD_PACKAGE -> $PACKAGE_NAME"
echo "  server_package: $OLD_SERVER_PACKAGE -> $NEW_SERVER_PACKAGE"
echo "  core_package:   $OLD_CORE_PACKAGE -> $NEW_CORE_PACKAGE"
echo "  database:       $OLD_DB_NAME -> $DB_NAME"
echo "  display_name:   $DISPLAY_NAME"
echo "  root_project:   template -> $PROJECT_NAME_LOWER"
echo ""

read -p "  Proceed? (y/N): " CONFIRM
[[ "$CONFIRM" != "y" && "$CONFIRM" != "Y" ]] && echo "Aborted." && exit 0

echo ""
echo "status: renaming..."
echo ""

# --- Rename operations ---

# 1. Update package references in Kotlin files (.kt)
echo "  [1/14] Updating package references in .kt files..."
find . -name "*.kt" \
    -not -path "./.gradle/*" \
    -not -path "*/build/*" \
    -not -path "./.planning/*" \
    -not -path "./.claude/*" \
    -exec grep -l "com\.m2f" {} + 2>/dev/null | while read -r file; do
    # Order matters: replace most specific first
    sed_inplace "s/${OLD_PACKAGE//./\\.}/${PACKAGE_NAME}/g" "$file"
    sed_inplace "s/${OLD_SERVER_PACKAGE//./\\.}/${NEW_SERVER_PACKAGE}/g" "$file"
    sed_inplace "s/${OLD_CORE_PACKAGE//./\\.}/${NEW_CORE_PACKAGE}/g" "$file"
done

# 2. Update package references in Gradle files (.kts)
echo "  [2/14] Updating package references in .gradle.kts files..."
find . -name "*.gradle.kts" \
    -not -path "./.gradle/*" \
    -not -path "*/build/*" \
    -exec grep -l "com\.m2f" {} + 2>/dev/null | while read -r file; do
    sed_inplace "s/${OLD_PACKAGE//./\\.}/${PACKAGE_NAME}/g" "$file"
    sed_inplace "s/${OLD_SERVER_PACKAGE//./\\.}/${NEW_SERVER_PACKAGE}/g" "$file"
    sed_inplace "s/${OLD_CORE_PACKAGE//./\\.}/${NEW_CORE_PACKAGE}/g" "$file"
done

# 3. Update settings.gradle.kts root project name
echo "  [3/14] Updating rootProject.name..."
sed_inplace "s/rootProject.name = \"template\"/rootProject.name = \"${PROJECT_NAME_LOWER}\"/g" settings.gradle.kts

# 4. Update docker-compose.yml database name
echo "  [4/14] Updating docker-compose.yml..."
sed_inplace "s/POSTGRES_DB: ${OLD_DB_NAME}/POSTGRES_DB: ${DB_NAME}/g" docker-compose.yml
sed_inplace "s/pg_isready -U postgres -d ${OLD_DB_NAME}/pg_isready -U postgres -d ${DB_NAME}/g" docker-compose.yml
sed_inplace "s/container_name: template-postgres/container_name: ${PROJECT_NAME_LOWER}-postgres/g" docker-compose.yml

# 5. Update server database configuration (DataSource.kt)
echo "  [5/14] Updating server database configuration..."
find . -name "DataSource.kt" \
    -not -path "./.gradle/*" \
    -not -path "*/build/*" | while read -r file; do
    sed_inplace "s|r2dbc:postgresql://localhost:5436/${OLD_DB_NAME}|r2dbc:postgresql://localhost:5436/${DB_NAME}|g" "$file"
    sed_inplace "s/R2DBC_DATABASE = \"${OLD_DB_NAME}\"/R2DBC_DATABASE = \"${DB_NAME}\"/g" "$file"
done

# 6. Move source directories to new package path
echo "  [6/14] Moving source directories..."

move_package_dir() {
    local base_dir="$1"
    local old_path="$2"
    local new_path="$3"

    local full_old="${base_dir}/${old_path}"
    local full_new="${base_dir}/${new_path}"

    if [[ -d "$full_old" ]]; then
        mkdir -p "$full_new"
        # Move all contents (files and subdirectories)
        if ls -A "$full_old" >/dev/null 2>&1; then
            # Use cp + rm for reliability across platforms
            cp -R "$full_old"/* "$full_new"/ 2>/dev/null || true
            cp -R "$full_old"/.[!.]* "$full_new"/ 2>/dev/null || true
            rm -rf "$full_old"
        fi
        # Clean up empty parent directories
        local parent
        parent=$(dirname "$full_old")
        while [[ "$parent" != "$base_dir" ]] && [[ -d "$parent" ]] && [[ -z "$(ls -A "$parent" 2>/dev/null)" ]]; do
            rmdir "$parent" 2>/dev/null || break
            parent=$(dirname "$parent")
        done
    fi
}

# Build list of source set directories to move
# Auto-discovers all modules dynamically -- no manual updates needed when modules are added.
# This finds every src/*/kotlin directory in the project, covering:
#   - composeApp/src/{commonMain,androidMain,iosMain,jvmMain,wasmJsMain,commonTest}/kotlin
#   - app/*/src/{commonMain,...}/kotlin  (auth, dashboard, designsystem, profile, any future modules)
#   - core/*/src/{commonMain,...}/kotlin (models, sdk, storage, any future modules)
#   - server/src/{main,test}/kotlin
#   - server/*/src/{main,test}/kotlin   (auth, ai, any future modules)
#   - server/core/*/src/{main,test}/kotlin (config, database, security, any future modules)
#   - shared/src/{commonMain,...}/kotlin
#   - androidApp/src/main/kotlin
SOURCE_SETS=()
while IFS= read -r dir; do
    SOURCE_SETS+=("$dir")
done < <(find . -path "*/src/*/kotlin" -not -path "./.gradle/*" -not -path "*/build/*" -not -path "./.git/*" -type d | sed 's|^\./||' | sort)

# Move com.m2f.template -> new package
for src in "${SOURCE_SETS[@]}"; do
    if [[ -d "${src}/${OLD_PACKAGE_PATH}" ]]; then
        move_package_dir "$src" "$OLD_PACKAGE_PATH" "$PACKAGE_PATH"
    fi
done

# 7. Handle server packages (com.m2f.server, com.m2f.core)
echo "  [7/14] Moving server package directories..."

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

# Move com.m2f.core -> new core package
for mod in config database security; do
    for sub in contract impl wire; do
        for variant in main test; do
            src="server/core/${mod}/${sub}/src/${variant}/kotlin"
            if [[ -d "${src}/${OLD_CORE_PATH}" ]]; then
                move_package_dir "$src" "$OLD_CORE_PATH" "$NEW_CORE_PATH"
            fi
        done
    done
    # Also check flat structure (some core modules don't have submodules)
    for variant in main test; do
        src="server/core/${mod}/src/${variant}/kotlin"
        if [[ -d "${src}/${OLD_CORE_PATH}" ]]; then
            move_package_dir "$src" "$OLD_CORE_PATH" "$NEW_CORE_PATH"
        fi
    done
done

# server/core itself
for variant in main test; do
    src="server/core/src/${variant}/kotlin"
    if [[ -d "${src}/${OLD_CORE_PATH}" ]]; then
        move_package_dir "$src" "$OLD_CORE_PATH" "$NEW_CORE_PATH"
    fi
done

# 8. Update AndroidManifest.xml if it contains package references
echo "  [8/14] Updating AndroidManifest.xml..."
find . -name "AndroidManifest.xml" \
    -not -path "./.gradle/*" \
    -not -path "*/build/*" | while read -r file; do
    if grep -q "com\.m2f" "$file" 2>/dev/null; then
        sed_inplace "s/${OLD_PACKAGE//./\\.}/${PACKAGE_NAME}/g" "$file"
    fi
done

# 9. Delete .iml files (IDE will regenerate)
echo "  [9/14] Removing .iml files (IDE will regenerate)..."
find . -name "*.iml" -not -path "./.gradle/*" -not -path "*/build/*" -delete 2>/dev/null || true

# 10. Update remaining Docker container names
echo "  [10/14] Updating Docker container names..."
sed_inplace "s/container_name: template-minio$/container_name: ${PROJECT_NAME_LOWER}-minio/g" docker-compose.yml
sed_inplace "s/container_name: template-minio-init$/container_name: ${PROJECT_NAME_LOWER}-minio-init/g" docker-compose.yml
sed_inplace "s/container_name: template-mailhog$/container_name: ${PROJECT_NAME_LOWER}-mailhog/g" docker-compose.yml

# 11. Update OAuth and SMTP configuration
echo "  [11/14] Updating OAuth and SMTP configuration..."
sed_inplace "s/OAUTH_MOBILE_SCHEME=template/OAUTH_MOBILE_SCHEME=${PROJECT_NAME_LOWER}/g" .env.example
sed_inplace "s/noreply@template.local/noreply@${PROJECT_NAME_LOWER}.local/g" .env.example

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

# 13. Update iOS configuration
echo "  [13/14] Updating iOS configuration..."
if [[ -f "iosApp/Configuration/Config.xcconfig" ]]; then
    sed_inplace "s/PRODUCT_NAME=template/PRODUCT_NAME=${PROJECT_NAME_LOWER}/g" iosApp/Configuration/Config.xcconfig
    sed_inplace "s|PRODUCT_BUNDLE_IDENTIFIER=com.m2f.template.template|PRODUCT_BUNDLE_IDENTIFIER=${PACKAGE_NAME}|g" iosApp/Configuration/Config.xcconfig
fi

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

# --- Post-rename verification ---

echo ""
echo "status: verifying..."

REMAINING=$(grep -r "com\.m2f\.template\|com\.m2f\.server\|com\.m2f\.core" \
    --include="*.kt" --include="*.kts" --include="*.xml" \
    -l . 2>/dev/null \
    | grep -v ".gradle/" \
    | grep -v "build/" \
    | grep -v ".planning/" \
    | grep -v ".claude/" \
    || true)

if [[ -n "$REMAINING" ]]; then
    echo -e "${YELLOW}warning:${NC} old package references found in:"
    echo "$REMAINING"
else
    echo "status: package_rename complete"
fi

# --- Build verification (mandatory) ---

echo ""
echo "status: building..."
./gradlew build --no-daemon 2>&1 | tail -20
BUILD_EXIT=$?
if [[ $BUILD_EXIT -ne 0 ]]; then
    echo -e "${RED}error:${NC} Build failed. Please fix the issues above."
    exit 1
fi

# --- Completion ---

echo ""
echo -e "${GREEN}> setup_complete${NC}"
echo "  project:  $PROJECT_NAME"
echo "  package:  $PACKAGE_NAME"
echo "  database: $DB_NAME"
echo ""
echo "// Run './gradlew build' to verify"
echo "// Run 'docker-compose up -d' to start the database"
echo ""
