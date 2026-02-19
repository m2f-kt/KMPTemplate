@file:OptIn(ExperimentalUuidApi::class)

package com.m2f.server.groups.repository

import com.m2f.server.groups.tables.GroupsTable
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.r2dbc.update
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Represents a group record from the database.
 */
data class GroupRecord(
    val id: Uuid,
    val name: String,
    val slug: String,
    val description: String,
    val createdBy: Uuid,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)

/**
 * Database access for group CRUD operations.
 */
class GroupRepository(private val db: R2dbcDatabase) {

    suspend fun findById(id: Uuid): GroupRecord? = suspendTransaction(db = db) {
        GroupsTable
            .select(GroupsTable.columns)
            .where { GroupsTable.id eq id }
            .singleOrNull()
            ?.toGroupRecord()
    }

    suspend fun findBySlug(slug: String): GroupRecord? = suspendTransaction(db = db) {
        GroupsTable
            .select(GroupsTable.columns)
            .where { GroupsTable.slug eq slug }
            .singleOrNull()
            ?.toGroupRecord()
    }

    suspend fun insert(name: String, slug: String, description: String, createdBy: Uuid): Uuid =
        suspendTransaction(db = db) {
            GroupsTable.insert {
                it[GroupsTable.name] = name
                it[GroupsTable.slug] = slug
                it[GroupsTable.description] = description
                it[GroupsTable.createdBy] = createdBy
            }[GroupsTable.id]
        }

    suspend fun update(id: Uuid, name: String?, description: String?): Boolean =
        suspendTransaction(db = db) {
            val rowsUpdated = GroupsTable.update({ GroupsTable.id eq id }) { stmt ->
                name?.let { stmt[GroupsTable.name] = it }
                description?.let { stmt[GroupsTable.description] = it }
            }
            rowsUpdated > 0
        }

    suspend fun delete(id: Uuid): Boolean = suspendTransaction(db = db) {
        val rowsDeleted = GroupsTable.deleteWhere { GroupsTable.id eq id }
        rowsDeleted > 0
    }

    suspend fun listAll(): List<GroupRecord> = suspendTransaction(db = db) {
        GroupsTable
            .select(GroupsTable.columns)
            .toList()
            .map { it.toGroupRecord() }
    }

    suspend fun count(): Long = suspendTransaction(db = db) {
        GroupsTable.select(GroupsTable.columns).count()
    }
}

private fun ResultRow.toGroupRecord(): GroupRecord = GroupRecord(
    id = this[GroupsTable.id],
    name = this[GroupsTable.name],
    slug = this[GroupsTable.slug],
    description = this[GroupsTable.description],
    createdBy = this[GroupsTable.createdBy],
    createdAt = this[GroupsTable.createdAt],
    updatedAt = this[GroupsTable.updatedAt],
)
