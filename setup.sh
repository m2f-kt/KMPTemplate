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

OLD_DB_NAME="application"
OLD_PROJECT_NAME_LOWER="template"
OLD_OAUTH_SCHEME="template"

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

# Compose Multiplatform's auto-generated resource package strips separators from
# rootProject.name (e.g., "My-App" -> "myapp.<module>.generated.resources").
PROJECT_NAME_IDENT=$(echo "$PROJECT_NAME_LOWER" | tr -d '-_')

# OAuth callback scheme matches the lowercase project name.
NEW_OAUTH_SCHEME="$PROJECT_NAME_LOWER"

# --- Dry-run summary ---

echo "status: preview"
echo "  project_name:    $PROJECT_NAME"
echo "  package:         $OLD_PACKAGE -> $PACKAGE_NAME"
echo "  server_package:  $OLD_SERVER_PACKAGE -> $NEW_SERVER_PACKAGE"
echo "  core_package:    $OLD_CORE_PACKAGE -> $NEW_CORE_PACKAGE"
echo "  database:        $OLD_DB_NAME -> $DB_NAME"
echo "  display_name:    $DISPLAY_NAME"
echo "  root_project:    $OLD_PROJECT_NAME_LOWER -> $PROJECT_NAME_LOWER"
echo "  oauth_scheme:    $OLD_OAUTH_SCHEME -> $NEW_OAUTH_SCHEME"
echo "  generated_pkg:   ${OLD_PROJECT_NAME_LOWER}.<mod>.generated.resources -> ${PROJECT_NAME_IDENT}.<mod>.generated.resources"
echo ""

read -p "  Proceed? (y/N): " CONFIRM
[[ "$CONFIRM" != "y" && "$CONFIRM" != "Y" ]] && echo "Aborted." && exit 0

echo ""
echo "status: renaming..."
echo ""

TOTAL_STEPS=18

# --- Helpers ---

# Find files (by name glob) containing a grep pattern, then apply a sed expression
# to each matched file. Excludes the usual noise paths.
sed_in_matched_files() {
    local name_pattern="$1"
    local grep_pattern="$2"
    shift 2
    find . -name "$name_pattern" \
        -not -path "./.gradle/*" \
        -not -path "*/build/*" \
        -not -path "./.git/*" \
        -not -path "./.planning/*" \
        -not -path "./.claude/*" \
        -not -path "./graphify-out/*" \
        -exec grep -l "$grep_pattern" {} + 2>/dev/null | while read -r file; do
        sed_inplace "$@" "$file"
    done
}

# Move a Kotlin source tree from one package path to another.
# CRITICAL: returns immediately when source == destination — without this guard
# the rm -rf below would delete every file under that path.
move_package_dir() {
    local base_dir="$1"
    local old_path="$2"
    local new_path="$3"

    if [[ "$old_path" == "$new_path" ]]; then
        return 0
    fi

    local full_old="${base_dir}/${old_path}"
    local full_new="${base_dir}/${new_path}"

    if [[ -d "$full_old" ]]; then
        mkdir -p "$full_new"
        if ls -A "$full_old" >/dev/null 2>&1; then
            cp -R "$full_old"/* "$full_new"/ 2>/dev/null || true
            cp -R "$full_old"/.[!.]* "$full_new"/ 2>/dev/null || true
            rm -rf "$full_old"
        fi
        local parent
        parent=$(dirname "$full_old")
        while [[ "$parent" != "$base_dir" ]] && [[ -d "$parent" ]] && [[ -z "$(ls -A "$parent" 2>/dev/null)" ]]; do
            rmdir "$parent" 2>/dev/null || break
            parent=$(dirname "$parent")
        done
    fi
}

# --- Rename operations ---

# 1. Update package references in Kotlin source files
echo "  [1/${TOTAL_STEPS}] Updating com.m2f.* package refs in .kt files..."
sed_in_matched_files "*.kt" "com\.m2f" \
    -e "s/${OLD_PACKAGE//./\\.}/${PACKAGE_NAME}/g" \
    -e "s/${OLD_SERVER_PACKAGE//./\\.}/${NEW_SERVER_PACKAGE}/g" \
    -e "s/${OLD_CORE_PACKAGE//./\\.}/${NEW_CORE_PACKAGE}/g"

# 2. Update package references in Gradle Kotlin DSL files (incl. hardcoded source paths
#    such as core/sdk's outDir.resolve("com/m2f/template/sdk")).
echo "  [2/${TOTAL_STEPS}] Updating com.m2f.* package refs in .gradle.kts files..."
sed_in_matched_files "*.gradle.kts" "com\.m2f\|${OLD_PACKAGE_PATH}" \
    -e "s/${OLD_PACKAGE//./\\.}/${PACKAGE_NAME}/g" \
    -e "s/${OLD_SERVER_PACKAGE//./\\.}/${NEW_SERVER_PACKAGE}/g" \
    -e "s/${OLD_CORE_PACKAGE//./\\.}/${NEW_CORE_PACKAGE}/g" \
    -e "s|${OLD_PACKAGE_PATH}|${PACKAGE_PATH}|g"

# 3. Update settings.gradle.kts root project name
echo "  [3/${TOTAL_STEPS}] Updating rootProject.name..."
sed_inplace "s/rootProject.name = \"${OLD_PROJECT_NAME_LOWER}\"/rootProject.name = \"${PROJECT_NAME_LOWER}\"/g" settings.gradle.kts

# 4. Update docker-compose.yml database name
echo "  [4/${TOTAL_STEPS}] Updating docker-compose.yml database name..."
sed_inplace "s/POSTGRES_DB: ${OLD_DB_NAME}/POSTGRES_DB: ${DB_NAME}/g" docker-compose.yml
sed_inplace "s/pg_isready -U postgres -d ${OLD_DB_NAME}/pg_isready -U postgres -d ${DB_NAME}/g" docker-compose.yml

# 5. Update server database configuration (DataSource.kt + root build.gradle.kts seedData task)
echo "  [5/${TOTAL_STEPS}] Updating server database configuration..."
find . -name "DataSource.kt" \
    -not -path "./.gradle/*" \
    -not -path "*/build/*" | while read -r file; do
    sed_inplace "s|r2dbc:postgresql://localhost:5436/${OLD_DB_NAME}|r2dbc:postgresql://localhost:5436/${DB_NAME}|g" "$file"
    sed_inplace "s/R2DBC_DATABASE = \"${OLD_DB_NAME}\"/R2DBC_DATABASE = \"${DB_NAME}\"/g" "$file"
done
# seedData task in root build.gradle.kts: "docker exec -i ... psql -U postgres -d <DB> < ..."
sed_inplace "s/-d ${OLD_DB_NAME} </-d ${DB_NAME} </g" build.gradle.kts || true

# 6. Move source directories to new package path
echo "  [6/${TOTAL_STEPS}] Moving source directories (${OLD_PACKAGE_PATH} -> ${PACKAGE_PATH})..."

# Auto-discover all Kotlin source-set roots dynamically.
SOURCE_SETS=()
while IFS= read -r dir; do
    SOURCE_SETS+=("$dir")
done < <(find . -path "*/src/*/kotlin" -not -path "./.gradle/*" -not -path "*/build/*" -not -path "./.git/*" -type d | sed 's|^\./||' | sort)

for src in "${SOURCE_SETS[@]}"; do
    if [[ -d "${src}/${OLD_PACKAGE_PATH}" ]]; then
        move_package_dir "$src" "$OLD_PACKAGE_PATH" "$PACKAGE_PATH"
    fi
done

# 7. Move server/core package directories. No-op when first-two-segments unchanged
#    (move_package_dir guards old==new internally).
echo "  [7/${TOTAL_STEPS}] Moving server/core package directories..."

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

for mod in config database security; do
    for sub in contract impl wire; do
        for variant in main test; do
            src="server/core/${mod}/${sub}/src/${variant}/kotlin"
            if [[ -d "${src}/${OLD_CORE_PATH}" ]]; then
                move_package_dir "$src" "$OLD_CORE_PATH" "$NEW_CORE_PATH"
            fi
        done
    done
    for variant in main test; do
        src="server/core/${mod}/src/${variant}/kotlin"
        if [[ -d "${src}/${OLD_CORE_PATH}" ]]; then
            move_package_dir "$src" "$OLD_CORE_PATH" "$NEW_CORE_PATH"
        fi
    done
done

for variant in main test; do
    src="server/core/src/${variant}/kotlin"
    if [[ -d "${src}/${OLD_CORE_PATH}" ]]; then
        move_package_dir "$src" "$OLD_CORE_PATH" "$NEW_CORE_PATH"
    fi
done

# 8. Update AndroidManifest.xml: package references AND the OAuth deep-link scheme attribute
echo "  [8/${TOTAL_STEPS}] Updating AndroidManifest.xml (package + OAuth scheme)..."
find . -name "AndroidManifest.xml" \
    -not -path "./.gradle/*" \
    -not -path "*/build/*" | while read -r file; do
    if grep -q "com\.m2f\|android:scheme=\"${OLD_OAUTH_SCHEME}\"" "$file" 2>/dev/null; then
        sed_inplace "s/${OLD_PACKAGE//./\\.}/${PACKAGE_NAME}/g" "$file"
        sed_inplace "s/android:scheme=\"${OLD_OAUTH_SCHEME}\"/android:scheme=\"${NEW_OAUTH_SCHEME}\"/g" "$file"
    fi
done

# 9. Delete .iml files (IDE will regenerate)
echo "  [9/${TOTAL_STEPS}] Removing .iml files (IDE will regenerate)..."
find . -name "*.iml" -not -path "./.gradle/*" -not -path "*/build/*" -delete 2>/dev/null || true

# 10. Update Docker container names everywhere they appear (compose, root build, docs)
echo "  [10/${TOTAL_STEPS}] Updating Docker container names..."
for f in docker-compose.yml build.gradle.kts docs/ARCHITECTURE.md docs/GETTING-STARTED.md; do
    if [[ -f "$f" ]]; then
        sed_inplace "s/${OLD_PROJECT_NAME_LOWER}-postgres/${PROJECT_NAME_LOWER}-postgres/g" "$f"
        sed_inplace "s/${OLD_PROJECT_NAME_LOWER}-minio-init/${PROJECT_NAME_LOWER}-minio-init/g" "$f"
        sed_inplace "s/${OLD_PROJECT_NAME_LOWER}-minio/${PROJECT_NAME_LOWER}-minio/g" "$f"
        sed_inplace "s/${OLD_PROJECT_NAME_LOWER}-mailhog/${PROJECT_NAME_LOWER}-mailhog/g" "$f"
    fi
done

# 11. Update OAuth/SMTP defaults in .env.example, server Env.kt defaults, and Kotlin OAuth handlers
echo "  [11/${TOTAL_STEPS}] Updating OAuth/SMTP defaults and deep-link refs..."
sed_inplace "s/OAUTH_MOBILE_SCHEME=${OLD_OAUTH_SCHEME}/OAUTH_MOBILE_SCHEME=${NEW_OAUTH_SCHEME}/g" .env.example
sed_inplace "s/noreply@${OLD_PROJECT_NAME_LOWER}\.local/noreply@${PROJECT_NAME_LOWER}.local/g" .env.example
# Server-side defaults baked into Env.kt
sed_in_matched_files "Env.kt" "OAUTH_MOBILE_SCHEME\|${OLD_PROJECT_NAME_LOWER}\.local" \
    -e "s/env(\"OAUTH_MOBILE_SCHEME\") ?: \"${OLD_OAUTH_SCHEME}\"/env(\"OAUTH_MOBILE_SCHEME\") ?: \"${NEW_OAUTH_SCHEME}\"/g" \
    -e "s|noreply@${OLD_PROJECT_NAME_LOWER}\.local|noreply@${PROJECT_NAME_LOWER}.local|g"
# Mobile OAuth deep-link literals in Kotlin source
sed_in_matched_files "*.kt" "${OLD_OAUTH_SCHEME}://auth/callback" \
    -e "s|${OLD_OAUTH_SCHEME}://auth/callback|${NEW_OAUTH_SCHEME}://auth/callback|g"

# 12. Update app display name (Android strings + Compose strings + JVM window + WASM HTML)
echo "  [12/${TOTAL_STEPS}] Updating app display name..."
find . -path "*/androidMain/res/values/strings.xml" \
    -not -path "./.gradle/*" \
    -not -path "*/build/*" | while read -r file; do
    sed_inplace "s|<string name=\"app_name\">[^<]*</string>|<string name=\"app_name\">${DISPLAY_NAME}</string>|g" "$file"
done
find . -path "*/composeResources/values*/strings.xml" \
    -not -path "./.gradle/*" \
    -not -path "*/build/*" | while read -r file; do
    if grep -q "app_name" "$file" 2>/dev/null; then
        sed_inplace "s|<string name=\"app_name\">[^<]*</string>|<string name=\"app_name\">${DISPLAY_NAME}</string>|g" "$file"
    fi
done
# JVM Window title in composeApp/src/jvmMain/.../main.kt
sed_in_matched_files "main.kt" "title = \"${OLD_PROJECT_NAME_LOWER}\"" \
    -e "s|title = \"${OLD_PROJECT_NAME_LOWER}\"|title = \"${DISPLAY_NAME}\"|g"
# WASM <title>
if [[ -f "composeApp/src/wasmJsMain/resources/index.html" ]]; then
    sed_inplace "s|<title>${OLD_PROJECT_NAME_LOWER}</title>|<title>${DISPLAY_NAME}</title>|g" composeApp/src/wasmJsMain/resources/index.html
fi

# 13. Update iOS configuration (Config.xcconfig + Xcode project + per-user schemes)
echo "  [13/${TOTAL_STEPS}] Updating iOS configuration..."
if [[ -f "iosApp/Configuration/Config.xcconfig" ]]; then
    sed_inplace "s/PRODUCT_NAME=${OLD_PROJECT_NAME_LOWER}/PRODUCT_NAME=${PROJECT_NAME_LOWER}/g" iosApp/Configuration/Config.xcconfig
    sed_inplace "s|PRODUCT_BUNDLE_IDENTIFIER=com\.m2f\.template\.template|PRODUCT_BUNDLE_IDENTIFIER=${PACKAGE_NAME}|g" iosApp/Configuration/Config.xcconfig
fi
# Xcode project file and any user schemes still reference the bundled product as `<oldname>.app`.
find iosApp -type f \( -name "project.pbxproj" -o -name "*.xcscheme" \) 2>/dev/null | while read -r file; do
    sed_inplace "s/${OLD_PROJECT_NAME_LOWER}\.app/${PROJECT_NAME_LOWER}.app/g" "$file"
done

# 14. Update tooling (.mcp.json + README.md + devcontainer.json)
echo "  [14/${TOTAL_STEPS}] Updating tooling configuration..."
if [[ -f ".mcp.json" ]]; then
    sed_inplace "s|localhost:5436/${OLD_DB_NAME}|localhost:5436/${DB_NAME}|g" .mcp.json
fi
if [[ -f "README.md" ]]; then
    sed_inplace "s/^# Template$/# ${PROJECT_NAME}/g" README.md
    sed_inplace "s/cd ${OLD_PROJECT_NAME_LOWER}/cd ${PROJECT_NAME_LOWER}/g" README.md
fi
if [[ -f ".devcontainer/devcontainer.json" ]]; then
    sed_inplace "s/\"name\": \"KMP Template\"/\"name\": \"${DISPLAY_NAME}\"/g" .devcontainer/devcontainer.json
fi

# 15. Update Compose-generated resource imports.
#     Compose Multiplatform auto-generates a resource package
#       <rootProject.nameLowerNoSep>.<modulePath>.generated.resources
#     Existing imports look like `import template.app.designsystem.generated.resources.Res`.
echo "  [15/${TOTAL_STEPS}] Updating Compose-generated resource imports..."
sed_in_matched_files "*.kt" "^import ${OLD_PROJECT_NAME_LOWER}\." \
    -e "s|^import ${OLD_PROJECT_NAME_LOWER}\.|import ${PROJECT_NAME_IDENT}.|g"
# packageOfResClass overrides in build.gradle.kts also encode the prefix
sed_in_matched_files "*.gradle.kts" "packageOfResClass = \"${OLD_PROJECT_NAME_LOWER}\." \
    -e "s|packageOfResClass = \"${OLD_PROJECT_NAME_LOWER}\.|packageOfResClass = \"${PROJECT_NAME_IDENT}.|g"

# 16. Verification — only flag old refs that should not exist after a successful rename.
echo ""
echo "  [16/${TOTAL_STEPS}] Verifying remaining old references..."

REMAINING=$(grep -rn "${OLD_PACKAGE//./\\.}\|\b${OLD_OAUTH_SCHEME}://\|${OLD_PROJECT_NAME_LOWER}-postgres\|${OLD_PROJECT_NAME_LOWER}-minio\|${OLD_PROJECT_NAME_LOWER}-mailhog" \
    --include="*.kt" --include="*.kts" --include="*.xml" --include="*.json" \
    --include="*.yml" --include="*.md" --include="*.xcconfig" \
    . 2>/dev/null \
    | grep -v ".gradle/" \
    | grep -v "build/" \
    | grep -v ".planning/" \
    | grep -v ".claude/" \
    | grep -v "graphify-out/" \
    | grep -v "setup.sh" \
    || true)

if [[ -n "$REMAINING" ]]; then
    echo -e "${YELLOW}warning:${NC} old references still present:"
    echo "$REMAINING"
else
    echo "  -> rename complete"
fi

# 17. Optional sanity: print residual bare 'template' literals in Kotlin source for review.
echo ""
echo "  [17/${TOTAL_STEPS}] Scanning for residual 'template' literals (informational)..."
LITERAL=$(grep -rn "\b${OLD_PROJECT_NAME_LOWER}\b" \
    --include="*.kt" --include="*.kts" \
    . 2>/dev/null \
    | grep -v ".gradle/" | grep -v "build/" | grep -v "graphify-out/" \
    | grep -v "io\.ktor\.server\.application\." \
    | grep -v "com\.android\.application" \
    | grep -v "id(\"application\")" \
    | grep -v "applicationDefaultJvmArgs" \
    | grep -v "compose\.desktop\.application" \
    || true)
if [[ -n "$LITERAL" ]]; then
    echo -e "${YELLOW}note:${NC} bare 'template' word survives in:"
    echo "$LITERAL" | head -20
else
    echo "  -> no bare 'template' literals found"
fi

# 18. Build verification (mandatory). Excludes the pre-existing duplicate-jar
#     failure in :server:distZip / :server:distTar (template-level Gradle issue,
#     unrelated to renaming).
echo ""
echo "  [18/${TOTAL_STEPS}] Verifying build (this may take a few minutes)..."
BUILD_LOG=$(mktemp -t template-setup-build.XXXXXX)
if ./gradlew build -x :server:distZip -x :server:distTar > "$BUILD_LOG" 2>&1; then
    echo "  -> build OK"
else
    echo -e "${RED}error:${NC} Build failed. Last 40 lines of output:"
    tail -40 "$BUILD_LOG"
    echo ""
    echo "Full log retained at: $BUILD_LOG"
    exit 1
fi

# --- Completion ---

echo ""
echo -e "${GREEN}> setup_complete${NC}"
echo "  project:      $PROJECT_NAME"
echo "  package:      $PACKAGE_NAME"
echo "  database:     $DB_NAME"
echo "  oauth_scheme: $NEW_OAUTH_SCHEME"
echo ""
echo "// Run 'docker-compose up -d' to start infrastructure"
echo "// Run './gradlew seedData' to seed demo data"
echo "// Run './gradlew :server:run' to start the backend"
echo ""
