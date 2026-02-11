package com.m2f.server.auth

import com.m2f.core.database.migrations.Migration
import com.m2f.core.database.migrations.MigrationRegistry
import com.m2f.server.auth.tables.RefreshTokensTable
import com.m2f.server.auth.tables.UsersTable
import org.jetbrains.exposed.v1.r2dbc.SchemaUtils

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
 * Register all auth-related database migrations.
 * Must be called before startDatabase() so migrations are available when the database starts.
 */
fun registerAuthMigrations() {
    MigrationRegistry.register(CreateUsersTableMigration())
    MigrationRegistry.register(CreateRefreshTokensTableMigration())
}
