---
name: run-migration
description: Create and validate Exposed database migrations for server feature modules. Use when adding new tables, altering columns, or registering migrations.
disable-model-invocation: true
---

# Run Migration Skill

Creates and validates Exposed database migrations following the project's server module patterns.

## Workflow

### Step 1: Gather Context

Ask: Which server module? (auth, groups, files, ai, or NEW module)
Ask: What schema change? (new table, add column, alter column, add index, etc.)

### Step 2: Check Current State

Read the module's existing tables and migrations:
- `server/<module>/src/main/kotlin/com/m2f/server/<module>/tables/` — Exposed table definitions
- `server/<module>/src/main/kotlin/com/m2f/server/<module>/` — Look for `registerXxxMigrations()` function

### Step 3: Create/Update Table Definition

If adding a new table, create in `tables/`:

```kotlin
package com.m2f.server.<module>.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object XxxTable : Table("xxx") {
    val id = uuid("id").autoGenerate()
    val name = varchar("name", 255)
    val createdAt = timestamp("created_at")

    override val primaryKey = PrimaryKey(id)
}
```

If altering an existing table, update the table definition to reflect the final desired state.

### Step 4: Create Migration

Create the migration SQL file or Exposed migration in the module. Follow the existing migration pattern in the module.

### Step 5: Register Migration

Ensure the migration is registered in the module's entry file via `registerXxxMigrations()`.

### Step 6: Validate

Run the following commands to validate:

```bash
# Ensure Docker is running
./gradlew devUp

# Run server to apply migrations
./gradlew :server:run &
SERVER_PID=$!
sleep 10
kill $SERVER_PID

# Run server tests to verify schema
./gradlew :server:test
```

If tests fail, diagnose and fix the migration.

### Step 7: Verify in Database

Connect to the local Postgres (port 5436) and verify the schema change was applied correctly:

```bash
docker exec -it template-postgres psql -U postgres -d template -c "\d xxx"
```
