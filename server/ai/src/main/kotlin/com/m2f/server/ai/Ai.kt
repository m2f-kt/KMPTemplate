package com.m2f.server.ai

import com.m2f.core.database.migrations.Migration
import com.m2f.core.database.migrations.MigrationRegistry
import com.m2f.server.ai.persistence.ConversationsTable
import org.jetbrains.exposed.v1.r2dbc.SchemaUtils

/**
 * Migration to create the ai_conversations table for agent checkpoint persistence.
 */
internal class CreateConversationsTableMigration : Migration {
    override val version: String = "20260213100001"
    override val description: String = "Create ai_conversations table"

    override suspend fun migrate() {
        SchemaUtils.create(ConversationsTable)
    }
}

/**
 * Register all AI-related database migrations.
 * Must be called before startDatabase() so migrations are available when the database starts.
 * Follows the Auth.kt registerAuthMigrations() pattern.
 */
fun registerAiMigrations() {
    MigrationRegistry.register(CreateConversationsTableMigration())
}
