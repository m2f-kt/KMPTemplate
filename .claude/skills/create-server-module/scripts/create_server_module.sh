#!/bin/bash

# Script to create a new server feature module with 3 submodules (contract, impl, wire)
# Usage: ./create_server_module.sh --name "feature name"

# Find the project root by looking for settings.gradle.kts and server/ directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$SCRIPT_DIR"

while [[ ! -f "$PROJECT_ROOT/settings.gradle.kts" ]] || [[ ! -d "$PROJECT_ROOT/server" ]]; do
  if [[ "$PROJECT_ROOT" == "/" ]]; then
    echo "Error: Could not find project root"
    exit 1
  fi
  PROJECT_ROOT="$(dirname "$PROJECT_ROOT")"
done

cd "$PROJECT_ROOT" || exit 1
echo "Project root: $PROJECT_ROOT"

# Parse command line arguments
while [[ $# -gt 0 ]]; do
  case $1 in
    --name)
      feature_name="$2"
      shift
      shift
      ;;
    *)
      echo "Usage: $0 --name \"feature name\""
      exit 1
      ;;
  esac
done

if [[ -z "$feature_name" ]]; then
  echo "Usage: $0 --name \"feature name\""
  exit 1
fi

# Convert feature name to lowercase and remove spaces/hyphens for package name
package_name=$(echo "$feature_name" | tr '[:upper:]' '[:lower:]' | sed 's/[ -]//g')

# Convert feature name to PascalCase for class names
IFS=' -' read -ra words <<< "$feature_name"
class_name=""
for word in "${words[@]}"; do
  if [[ -z "$word" ]]; then
    continue
  fi
  word_lower=$(echo "$word" | tr '[:upper:]' '[:lower:]')
  first_char=$(echo "${word_lower:0:1}" | tr '[:lower:]' '[:upper:]')
  class_name+="${first_char}${word_lower:1}"
done

# Generate migration timestamp
migration_timestamp=$(date +%Y%m%d%H%M%S)

echo "Creating server feature module: $feature_name"
echo "Package name: com.m2f.server.$package_name"
echo "Class name prefix: $class_name"
echo ""

# ============================================================================
# 1. CREATE CONTRACT MODULE
# ============================================================================
echo "Creating CONTRACT module..."
mkdir -p "server/$package_name/contract/src/main/kotlin/com/m2f/server/$package_name/contract"

cat > "server/$package_name/contract/build.gradle.kts" << 'EOF'
plugins {
    id("server-module-convention")
}

group = "com.m2f.server"

dependencies {
    implementation(projects.core.models)
    implementation(projects.server.core.config)
}
EOF

cat > "server/$package_name/contract/src/main/kotlin/com/m2f/server/$package_name/contract/${class_name}Service.kt" << EOF
package com.m2f.server.$package_name.contract

interface ${class_name}Service {
    // Define service methods here
}
EOF

cat > "server/$package_name/contract/src/main/kotlin/com/m2f/server/$package_name/contract/${class_name}Errors.kt" << EOF
package com.m2f.server.$package_name.contract

import com.m2f.core.config.server.DomainError
import io.ktor.http.HttpStatusCode

class ${class_name}NotFound : DomainError {
    override val status = HttpStatusCode.NotFound
    override val code = "${package_name}_not_found"
    override val detail = "${class_name} not found"
}
EOF

cat > "server/$package_name/contract/.gitignore" << EOF
/build
EOF

echo "  Contract module created"

# ============================================================================
# 2. CREATE IMPLEMENTATION MODULE
# ============================================================================
echo "Creating IMPLEMENTATION module..."
mkdir -p "server/$package_name/impl/src/main/kotlin/com/m2f/server/$package_name/impl/service"
mkdir -p "server/$package_name/impl/src/main/kotlin/com/m2f/server/$package_name/impl/repository"
mkdir -p "server/$package_name/impl/src/main/kotlin/com/m2f/server/$package_name/impl/tables"
mkdir -p "server/$package_name/impl/src/main/kotlin/com/m2f/server/$package_name/impl/routes"
mkdir -p "server/$package_name/impl/src/main/kotlin/com/m2f/server/$package_name/impl/migrations"
mkdir -p "server/$package_name/impl/src/test/kotlin/com/m2f/server/$package_name/impl"

cat > "server/$package_name/impl/build.gradle.kts" << 'GRADLE_EOF'
plugins {
    id("server-module-convention")
}

group = "com.m2f.server"

dependencies {
    implementation(projects.server.FEATURE_ACCESSOR.contract)
    implementation(projects.core.models)
    implementation(projects.server.core.config)
    implementation(projects.server.core.database)
    implementation(projects.server.core.security)
    implementation(libs.bundles.fp)
    implementation(libs.bundles.ktor.core)
    implementation(libs.ktor.server.resources)
    implementation(libs.bundles.di)
    testImplementation(libs.bundles.testing.server)
}
GRADLE_EOF
sed -i '' "s/FEATURE_ACCESSOR/$package_name/g" "server/$package_name/impl/build.gradle.kts"

cat > "server/$package_name/impl/src/main/kotlin/com/m2f/server/$package_name/impl/service/${class_name}ServiceImpl.kt" << EOF
package com.m2f.server.$package_name.impl.service

import arrow.core.raise.Raise
import com.m2f.core.config.server.DomainError
import com.m2f.server.$package_name.contract.${class_name}Service

class ${class_name}ServiceImpl(
    private val repository: ${class_name}Repository,
) : ${class_name}Service {

}
EOF

cat > "server/$package_name/impl/src/main/kotlin/com/m2f/server/$package_name/impl/repository/${class_name}Repository.kt" << EOF
package com.m2f.server.$package_name.impl.repository

import org.jetbrains.exposed.sql.transactions.experimental.suspendTransaction
import org.jetbrains.exposed.r2dbc.R2dbcDatabase

class ${class_name}Repository(private val db: R2dbcDatabase) {

}
EOF

cat > "server/$package_name/impl/src/main/kotlin/com/m2f/server/$package_name/impl/tables/${class_name}Table.kt" << EOF
package com.m2f.server.$package_name.impl.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object ${class_name}sTable : Table("${package_name}s") {
    val id = uuid("id").autoGenerate()
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)
}
EOF

cat > "server/$package_name/impl/src/main/kotlin/com/m2f/server/$package_name/impl/routes/${class_name}Routes.kt" << EOF
package com.m2f.server.$package_name.impl.routes

import com.m2f.server.$package_name.contract.${class_name}Service
import io.ktor.server.routing.Route

fun Route.${package_name}Routes(service: ${class_name}Service) {

}
EOF

cat > "server/$package_name/impl/src/main/kotlin/com/m2f/server/$package_name/impl/migrations/${class_name}Migrations.kt" << EOF
package com.m2f.server.$package_name.impl.migrations

import com.m2f.core.config.server.migration.Migration
import com.m2f.core.config.server.migration.MigrationRegistry
import com.m2f.server.$package_name.impl.tables.${class_name}sTable
import org.jetbrains.exposed.sql.SchemaUtils

internal class Create${class_name}sTableMigration : Migration {
    override val version: String = "$migration_timestamp"
    override val description: String = "Create ${package_name}s table"

    override suspend fun migrate() {
        SchemaUtils.create(${class_name}sTable)
    }
}

fun register${class_name}Migrations() {
    MigrationRegistry.register(Create${class_name}sTableMigration())
}
EOF

cat > "server/$package_name/impl/src/test/kotlin/com/m2f/server/$package_name/impl/${class_name}RoutesTest.kt" << EOF
package com.m2f.server.$package_name.impl

class ${class_name}RoutesTest {
    // TODO: Add route tests using Ktor test host
}
EOF

cat > "server/$package_name/impl/.gitignore" << EOF
/build
EOF

echo "  Implementation module created"

# ============================================================================
# 3. CREATE WIRE MODULE
# ============================================================================
echo "Creating WIRE module..."
mkdir -p "server/$package_name/wire/src/main/kotlin/com/m2f/server/$package_name/wire"

cat > "server/$package_name/wire/build.gradle.kts" << 'GRADLE_EOF'
plugins {
    id("server-module-convention")
}

group = "com.m2f.server"

dependencies {
    api(projects.server.FEATURE_ACCESSOR.contract)
    implementation(projects.server.FEATURE_ACCESSOR.impl)
    implementation(libs.bundles.di)
}
GRADLE_EOF
sed -i '' "s/FEATURE_ACCESSOR/$package_name/g" "server/$package_name/wire/build.gradle.kts"

cat > "server/$package_name/wire/src/main/kotlin/com/m2f/server/$package_name/wire/${class_name}Module.kt" << EOF
package com.m2f.server.$package_name.wire

import com.m2f.server.$package_name.contract.${class_name}Service
import com.m2f.server.$package_name.impl.repository.${class_name}Repository
import com.m2f.server.$package_name.impl.service.${class_name}ServiceImpl
import org.jetbrains.exposed.r2dbc.R2dbcDatabase
import org.koin.dsl.module

val ${package_name}Module = module {
    single { ${class_name}Repository(get<R2dbcDatabase>()) }
    single<${class_name}Service> { ${class_name}ServiceImpl(get()) }
}
EOF

cat > "server/$package_name/wire/.gitignore" << EOF
/build
EOF

echo "  Wire module created"

# ============================================================================
# 4. UPDATE settings.gradle.kts
# ============================================================================
echo "Updating settings.gradle.kts..."

last_include_line=$(grep -n 'include("server:' settings.gradle.kts | tail -1 | cut -d: -f1)

if [[ -z "$last_include_line" ]]; then
  echo "include(\"server:$package_name:contract\")" >> settings.gradle.kts
  echo "include(\"server:$package_name:impl\")" >> settings.gradle.kts
  echo "include(\"server:$package_name:wire\")" >> settings.gradle.kts
else
  sed -i '' "${last_include_line}a\\
include(\"server:$package_name:contract\")\\
include(\"server:$package_name:impl\")\\
include(\"server:$package_name:wire\")
" settings.gradle.kts
fi

echo "  settings.gradle.kts updated"

# ============================================================================
# SUMMARY
# ============================================================================
echo ""
echo "========================================="
echo "Server module '$feature_name' created successfully!"
echo "Package: com.m2f.server.$package_name"
echo "Class prefix: $class_name"
echo ""
echo "Created modules:"
echo "  - server:$package_name:contract   (${class_name}Service interface, ${class_name}Errors)"
echo "  - server:$package_name:impl       (ServiceImpl, Repository, Table, Routes, Migrations)"
echo "  - server:$package_name:wire       (Koin ${package_name}Module)"
echo ""
echo "Next steps:"
echo "1. Add wire module to server/build.gradle.kts:"
echo "   implementation(projects.server.$package_name.wire)"
echo ""
echo "2. Include Koin module in ServerModule.kt:"
echo "   includes(${package_name}Module)"
echo ""
echo "3. Register migrations in Application.kt config block:"
echo "   register${class_name}Migrations()"
echo ""
echo "4. Add routes in Application.kt routing block:"
echo "   ${package_name}Routes(get())"
echo "========================================="
