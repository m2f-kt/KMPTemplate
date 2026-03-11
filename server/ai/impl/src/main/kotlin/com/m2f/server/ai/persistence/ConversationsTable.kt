package com.m2f.server.ai.persistence

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime

/**
 * Stores agent conversation checkpoints for persistence across HTTP requests.
 * Each row represents a checkpoint for a specific agent (keyed by composite agentId).
 *
 * Uses TEXT for checkpoint_data (not JSONB) to avoid R2DBC JSONB driver issues.
 * The agentId follows the composite format: "user:{userId}:conv:{conversationId}"
 * for multi-tenant conversation ownership.
 */
object ConversationsTable : Table("ai_conversations") {
    val id = varchar("id", 255)
    val agentId = varchar("agent_id", 255)
    val checkpointData = text("checkpoint_data")
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)
}
