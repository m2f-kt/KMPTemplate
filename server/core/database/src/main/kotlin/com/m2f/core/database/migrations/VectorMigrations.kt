package com.m2f.core.database.migrations

import com.m2f.core.database.tables.DocumentEmbeddingsTable
import org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import org.jetbrains.exposed.v1.r2dbc.transactions.TransactionManager

/**
 * Migration to enable pgvector extension and create the document_embeddings table.
 * Requires pgvector Docker image (pgvector/pgvector:pg15) or manual extension installation.
 */
internal class EnablePgvectorAndCreateEmbeddingsTableMigration : Migration {
    override val version: String = "20260221100001"
    override val description: String = "Enable pgvector extension and create document_embeddings table"

    override suspend fun migrate() {
        // Enable pgvector extension (idempotent) via current transaction's exec
        TransactionManager.current().exec("CREATE EXTENSION IF NOT EXISTS vector")
        // Create the document_embeddings table
        SchemaUtils.create(DocumentEmbeddingsTable)
    }
}

/**
 * Register all vector/RAG-related database migrations.
 * Must be called before startDatabase() so migrations are available when the database starts.
 * Follows the Auth.kt registerAuthMigrations() pattern.
 */
fun registerVectorMigrations() {
    MigrationRegistry.register(EnablePgvectorAndCreateEmbeddingsTableMigration())
}
