@file:OptIn(ExperimentalUuidApi::class)

package com.m2f.core.database.tables

import com.m2f.core.database.vector.vector
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import kotlin.uuid.ExperimentalUuidApi

/**
 * Stores document chunk embeddings for RAG (Retrieval-Augmented Generation).
 * Each row represents one chunk of a document with its vector embedding.
 *
 * group_id scopes embeddings per group for data isolation in multi-tenant RAG queries.
 * Vector dimension 768 matches Google's text-embedding-004 model output (Koog/Gemini stack).
 */
object DocumentEmbeddingsTable : Table("document_embeddings") {
    val id = uuid("id").autoGenerate()
    val groupId = uuid("group_id").index()
    val content = text("content")
    val embedding = vector("embedding", 768)
    val metadata = text("metadata").default("{}")
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)
}
