@file:OptIn(ExperimentalTime::class)

package com.m2f.server.ai.persistence

import ai.koog.agents.snapshot.feature.AgentCheckpointData
import ai.koog.agents.snapshot.providers.PersistenceStorageProvider
import ai.koog.agents.snapshot.providers.PersistenceUtils
import ai.koog.agents.snapshot.providers.filters.AgentCheckpointPredicateFilter
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.r2dbc.update
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Custom PersistenceStorageProvider backed by R2DBC Exposed.
 * Stores agent checkpoint data as serialized JSON in the ai_conversations table.
 *
 * Uses [PersistenceUtils.defaultCheckpointJson] for serialization of [AgentCheckpointData],
 * which is marked @Serializable by Koog.
 *
 * Constructor takes [R2dbcDatabase] (Koin-injected in plan 06-03).
 */
class ExposedPersistenceStorage(
    private val db: R2dbcDatabase,
) : PersistenceStorageProvider<AgentCheckpointPredicateFilter> {

    private val json = PersistenceUtils.defaultCheckpointJson

    override suspend fun saveCheckpoint(
        agentId: String,
        agentCheckpointData: AgentCheckpointData,
    ) {
        val serialized = json.encodeToString(AgentCheckpointData.serializer(), agentCheckpointData)
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)

        suspendTransaction(db = db) {
            // Check if checkpoint already exists (upsert pattern)
            val existing = ConversationsTable
                .select(ConversationsTable.id)
                .where { ConversationsTable.id eq agentCheckpointData.checkpointId }
                .toList()

            if (existing.isNotEmpty()) {
                ConversationsTable.update({ ConversationsTable.id eq agentCheckpointData.checkpointId }) {
                    it[checkpointData] = serialized
                    it[updatedAt] = now
                }
            } else {
                ConversationsTable.insert {
                    it[id] = agentCheckpointData.checkpointId
                    it[ConversationsTable.agentId] = agentId
                    it[checkpointData] = serialized
                    it[createdAt] = now
                    it[updatedAt] = now
                }
            }
        }
    }

    override suspend fun getCheckpoints(
        agentId: String,
        filter: AgentCheckpointPredicateFilter?,
    ): List<AgentCheckpointData> = suspendTransaction(db = db) {
        ConversationsTable
            .select(ConversationsTable.columns)
            .where { ConversationsTable.agentId eq agentId }
            .toList()
            .map { row ->
                json.decodeFromString(
                    AgentCheckpointData.serializer(),
                    row[ConversationsTable.checkpointData],
                )
            }
            .let { checkpoints ->
                if (filter != null) checkpoints.filter { filter.check(it) } else checkpoints
            }
    }

    override suspend fun getLatestCheckpoint(
        agentId: String,
        filter: AgentCheckpointPredicateFilter?,
    ): AgentCheckpointData? = suspendTransaction(db = db) {
        ConversationsTable
            .select(ConversationsTable.columns)
            .where { ConversationsTable.agentId eq agentId }
            .toList()
            .map { row ->
                json.decodeFromString(
                    AgentCheckpointData.serializer(),
                    row[ConversationsTable.checkpointData],
                )
            }
            .let { checkpoints ->
                if (filter != null) checkpoints.filter { filter.check(it) } else checkpoints
            }
            .maxByOrNull { it.createdAt }
    }
}
