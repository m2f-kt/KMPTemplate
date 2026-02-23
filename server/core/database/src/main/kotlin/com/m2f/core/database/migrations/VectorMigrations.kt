package com.m2f.core.database.migrations

import com.m2f.core.database.tables.DocumentEmbeddingsTable
import com.m2f.core.database.tables.DocumentsTable
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
 * Migration to create the documents table and add userId, documentId, chunkIndex columns
 * to the existing document_embeddings table for document scoping and tracking.
 */
internal class AddDocumentsTableAndEmbeddingColumnsMigration : Migration {
    override val version: String = "20260224100001"
    override val description: String = "Create documents table and add scoping columns to document_embeddings"

    override suspend fun migrate() {
        // Create the documents table for source document metadata
        SchemaUtils.create(DocumentsTable)

        // Add new columns to document_embeddings for document scoping
        val exec = TransactionManager.current()
        exec.exec("ALTER TABLE document_embeddings ADD COLUMN IF NOT EXISTS user_id UUID")
        exec.exec("ALTER TABLE document_embeddings ADD COLUMN IF NOT EXISTS document_id UUID")
        exec.exec("ALTER TABLE document_embeddings ADD COLUMN IF NOT EXISTS chunk_index INTEGER DEFAULT 0")
        exec.exec("CREATE INDEX IF NOT EXISTS idx_document_embeddings_user_id ON document_embeddings(user_id)")
        exec.exec("CREATE INDEX IF NOT EXISTS idx_document_embeddings_document_id ON document_embeddings(document_id)")
    }
}

/**
 * Register all vector/RAG-related database migrations.
 * Must be called before startDatabase() so migrations are available when the database starts.
 * Follows the Auth.kt registerAuthMigrations() pattern.
 */
fun registerVectorMigrations() {
    MigrationRegistry.register(EnablePgvectorAndCreateEmbeddingsTableMigration())
    MigrationRegistry.register(AddDocumentsTableAndEmbeddingColumnsMigration())
}
