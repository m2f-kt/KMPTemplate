@file:OptIn(ExperimentalUuidApi::class)

package com.m2f.core.database.tables

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import kotlin.uuid.ExperimentalUuidApi

private const val NAME_LENGTH = 255
private const val CONTENT_TYPE_LENGTH = 100
private const val SCOPE_LENGTH = 20
private const val STATUS_LENGTH = 20

/**
 * Source document metadata for RAG pipeline.
 * Each row represents an uploaded document with its processing status.
 *
 * group_id scopes documents per group for data isolation.
 * scope distinguishes "personal" (user-level) from "group" (shared) documents.
 * status tracks ingestion progress: pending -> processing -> indexed (or failed).
 */
object DocumentsTable : Table("documents") {
    val id = uuid("id").autoGenerate()
    val groupId = uuid("group_id").index()
    val userId = uuid("user_id")
    val assignedToUserId = uuid("assigned_to_user_id").nullable()
    val name = varchar("name", NAME_LENGTH)
    val fileKey = text("file_key")
    val contentType = varchar("content_type", CONTENT_TYPE_LENGTH)
    val scope = varchar("scope", SCOPE_LENGTH)
    val status = varchar("status", STATUS_LENGTH).default("pending")
    val chunkCount = integer("chunk_count").default(0)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)
}
