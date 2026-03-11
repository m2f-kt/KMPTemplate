package com.m2f.server.auth

import com.m2f.core.database.migrations.Migration
import com.m2f.core.database.migrations.MigrationRegistry
import com.m2f.server.auth.contract.tables.PasswordResetTokensTable
import com.m2f.server.auth.contract.tables.RefreshTokensTable
import com.m2f.server.auth.contract.tables.RolesTable
import com.m2f.server.auth.contract.tables.UsersTable
import org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import org.jetbrains.exposed.v1.r2dbc.insert

/**
 * Migration to create the users table.
 */
internal class CreateUsersTableMigration : Migration {
    override val version: String = "20260211000001"
    override val description: String = "Create users table"

    override suspend fun migrate() {
        SchemaUtils.create(UsersTable)
    }
}

/**
 * Migration to create the refresh_tokens table.
 */
internal class CreateRefreshTokensTableMigration : Migration {
    override val version: String = "20260211000002"
    override val description: String = "Create refresh_tokens table"

    override suspend fun migrate() {
        SchemaUtils.create(RefreshTokensTable)
    }
}

/**
 * Migration to create the password_reset_tokens table.
 */
internal class CreatePasswordResetTokensTableMigration : Migration {
    override val version: String = "20260213000001"
    override val description: String = "Create password_reset_tokens table"

    override suspend fun migrate() {
        SchemaUtils.create(PasswordResetTokensTable)
    }
}

/**
 * Migration to create the roles table, seed role rows, and add role_id FK to users.
 */
internal class CreateRolesTableAndMigrateUsersMigration : Migration {
    override val version: String = "20260215000001"
    override val description: String = "Create roles table, seed roles, migrate users.role to role_id FK"

    override suspend fun migrate() {
        SchemaUtils.create(RolesTable)
        RolesTable.insert { it[name] = "USER"; it[level] = 0 }
        RolesTable.insert { it[name] = "ADMIN"; it[level] = 1 }
        RolesTable.insert { it[name] = "POWER_ADMIN"; it[level] = 2 }
        @Suppress("DEPRECATION")
        SchemaUtils.createMissingTablesAndColumns(UsersTable)
    }
}

/**
 * Register all auth-related database migrations.
 * Must be called before startDatabase() so migrations are available when the database starts.
 */
fun registerAuthMigrations() {
    MigrationRegistry.register(CreateUsersTableMigration())
    MigrationRegistry.register(CreateRefreshTokensTableMigration())
    MigrationRegistry.register(CreatePasswordResetTokensTableMigration())
    MigrationRegistry.register(CreateRolesTableAndMigrateUsersMigration())
}
