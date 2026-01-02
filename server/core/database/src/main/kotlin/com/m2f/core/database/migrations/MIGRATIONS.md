# Database Migration System Documentation for AI Agents

This document provides comprehensive guidance on how to create and manage database migrations in the Briiks project. It is specifically designed for AI agents to easily understand and implement database schema changes.

## Table of Contents

1. [Migration System Overview](#migration-system-overview)
2. [Key Components](#key-components)
3. [How to Create a New Migration](#how-to-create-a-new-migration)
4. [Common Migration Operations](#common-migration-operations)
5. [Testing Migrations](#testing-migrations)
6. [Troubleshooting](#troubleshooting)

## Migration System Overview

The migration system allows for versioned, incremental changes to the database schema. Each migration is applied exactly once and in order of version number. The system tracks which migrations have been applied in a `migrations` table.

### How It Works

1. During application startup, all migrations are registered with the `Migrations` object
2. The `Migrations.migrate()` method is called with the database connection
3. The system creates a `migrations` table if it doesn't exist
4. It checks which migrations have already been applied
5. It executes any pending migrations in order of version number
6. Each successful migration is recorded in the `migrations` table

## Key Components

The migration system consists of three main components:

1. **Migration Interface** (`Migration.kt`): Defines the contract for all migrations
2. **Migrations Object** (`Migration.kt`): Manages registration and execution of migrations
3. **MigrationRegistry** (`MigrationRegistry.kt`): Central place to register all migrations

### Migration Interface

```kotlin
interface Migration {
    val version: String
    val description: String
    fun migrate()
}
```

- `version`: A unique identifier for the migration (recommended format: timestamp YYYYMMDDHHMMSS)
- `description`: A brief description of what the migration does
- `migrate()`: The method containing the SQL statements to execute

### Migrations Object

The `Migrations` object handles:
- Registering migrations
- Creating the migrations table
- Checking which migrations have been applied
- Executing pending migrations in order

### MigrationRegistry

The `MigrationRegistry` object is responsible for registering all migrations with the `Migrations` object during application startup.

## How to Create a New Migration

Follow these steps to create a new migration:

1. **Create a new Kotlin file** in the `com.briiks.database.migration` package
2. **Name the file** descriptively (e.g., `AddUserEmailColumnMigration.kt`)
3. **Implement the Migration interface**:

```kotlin
class YourMigrationName : Migration {
    override val version: String = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
    override val description: String = "Description of what your migration does"

    override fun migrate() {
        val connection = TransactionManager.current()

        // Your SQL statements here
        connection.exec("""
            -- SQL statements
        """)
    }
}
```

4. **Register the migration** in `MigrationRegistry.kt`:

```kotlin
object MigrationRegistry {
    fun registerMigrations() {
        // Existing migrations
        Migrations.register(ExistingMigration())

        // Your new migration
        Migrations.register(YourMigrationName())
    }
}
```

## Common Migration Operations

Here are examples of common database migration operations:

### Adding a Column

```kotlin
override fun migrate() {
    val connection = TransactionManager.current()

    connection.exec("""
        DO $$
        BEGIN
            IF NOT EXISTS (
                SELECT 1
                FROM information_schema.columns
                WHERE table_name = 'your_table' AND column_name = 'new_column'
            ) THEN
                ALTER TABLE your_table
                ADD COLUMN new_column TEXT;
            END IF;
        END $$;
    """)
}
```

### Adding a Column with Default Value

```kotlin
connection.exec("""
    DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_name = 'your_table' AND column_name = 'new_column'
        ) THEN
            ALTER TABLE your_table
            ADD COLUMN new_column INTEGER DEFAULT 0;
        END IF;
    END $$;
""")
```

### Adding an Array Column

```kotlin
connection.exec("""
    DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_name = 'your_table' AND column_name = 'new_array_column'
        ) THEN
            ALTER TABLE your_table
            ADD COLUMN new_array_column TEXT[] DEFAULT NULL;
        END IF;
    END $$;
""")
```

### Creating a New Table

```kotlin
connection.exec("""
    CREATE TABLE IF NOT EXISTS new_table (
        id SERIAL PRIMARY KEY,
        name TEXT NOT NULL,
        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );
""")
```

### Adding an Index

```kotlin
connection.exec("""
    DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1
            FROM pg_indexes
            WHERE tablename = 'your_table' AND indexname = 'idx_your_table_column'
        ) THEN
            CREATE INDEX idx_your_table_column ON your_table(column_name);
        END IF;
    END $$;
""")
```

### Renaming a Column

```kotlin
connection.exec("""
    DO $$
    BEGIN
        IF EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_name = 'your_table' AND column_name = 'old_column'
        ) THEN
            ALTER TABLE your_table
            RENAME COLUMN old_column TO new_column;
        END IF;
    END $$;
""")
```

### Dropping a Column

```kotlin
connection.exec("""
    DO $$
    BEGIN
        IF EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_name = 'your_table' AND column_name = 'column_to_drop'
        ) THEN
            ALTER TABLE your_table
            DROP COLUMN column_to_drop;
        END IF;
    END $$;
""")
```

## Testing Migrations

To test a migration:

1. Create a test database
2. Apply the migration
3. Verify that the schema changes were applied correctly
4. Verify that existing data is still accessible and correct
5. Verify that new data can be inserted correctly

## Troubleshooting

### Migration Not Applied

If a migration is not being applied:

1. Check that the migration is registered in `MigrationRegistry.kt`
2. Verify that the version is unique and not already in the migrations table
3. Check for SQL syntax errors in the migration

### SQL Errors

If you encounter SQL errors:

1. Test your SQL statements in a database client first
2. Use the `DO $$ BEGIN ... END $$;` block to make your migrations idempotent
3. Check for table and column name typos
4. Ensure proper quoting of identifiers if they contain special characters

### Data Integrity Issues

If you encounter data integrity issues after a migration:

1. Include data migration steps in your schema migration
2. Consider breaking complex migrations into smaller, sequential migrations
3. Add validation steps to ensure data consistency

## Example: Complete Migration Implementation

Here's a complete example of a migration that adds a new column to a table:

```kotlin
class AddUserEmailVerifiedMigration : Migration {
    override val version: String = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
    override val description: String = "Add email_verified column to users table"

    override fun migrate() {
        val connection = TransactionManager.current()

        // Add email_verified column to users table if it doesn't exist
        connection.exec("""
            DO $$
            BEGIN
                IF NOT EXISTS (
                    SELECT 1
                    FROM information_schema.columns
                    WHERE table_name = 'users' AND column_name = 'email_verified'
                ) THEN
                    ALTER TABLE users
                    ADD COLUMN email_verified BOOLEAN DEFAULT FALSE;
                END IF;
            END $$;
        """)
    }
}
```

Then register it in `MigrationRegistry.kt`:

```kotlin
object MigrationRegistry {
    fun registerMigrations() {
        // Existing migrations
        Migrations.register(AddMediaLinksColumnMigration())

        // New migration
        Migrations.register(AddUserEmailVerifiedMigration())
    }
}
```
