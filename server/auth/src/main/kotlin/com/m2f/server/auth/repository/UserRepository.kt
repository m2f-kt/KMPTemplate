@file:OptIn(ExperimentalUuidApi::class)

package com.m2f.server.auth.repository

import com.m2f.server.auth.tables.UsersTable
import kotlinx.coroutines.flow.singleOrNull
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.r2dbc.update
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Represents a user record from the database.
 */
data class UserRecord(
    val id: Uuid,
    val email: String,
    val passwordHash: String,
    val name: String,
    val role: String,
)

/**
 * Database access for user CRUD operations.
 */
class UserRepository(private val db: R2dbcDatabase) {

    /**
     * Find a user by email address.
     */
    suspend fun findByEmail(email: String): UserRecord? = suspendTransaction(db = db) {
        UsersTable
            .select(UsersTable.columns)
            .where { UsersTable.email eq email }
            .singleOrNull()
            ?.toUserRecord()
    }

    /**
     * Find a user by their UUID.
     */
    suspend fun findById(id: Uuid): UserRecord? = suspendTransaction(db = db) {
        UsersTable
            .select(UsersTable.columns)
            .where { UsersTable.id eq id }
            .singleOrNull()
            ?.toUserRecord()
    }

    /**
     * Insert a new user and return the generated UUID.
     */
    suspend fun insert(
        email: String,
        passwordHash: String,
        name: String,
        role: String,
    ): Uuid = suspendTransaction(db = db) {
        UsersTable.insert {
            it[UsersTable.email] = email
            it[UsersTable.passwordHash] = passwordHash
            it[UsersTable.name] = name
            it[UsersTable.role] = role
        }[UsersTable.id]
    }

    /**
     * Update a user's profile fields. Returns true if a row was updated.
     */
    suspend fun updateProfile(id: Uuid, name: String?, email: String?): Boolean =
        suspendTransaction(db = db) {
            val rowsUpdated = UsersTable.update({ UsersTable.id eq id }) { stmt ->
                name?.let { stmt[UsersTable.name] = it }
                email?.let { stmt[UsersTable.email] = it }
            }
            rowsUpdated > 0
        }

    /**
     * Update a user's password hash. Used by the password reset flow.
     */
    suspend fun updatePasswordHash(id: Uuid, passwordHash: String): Boolean =
        suspendTransaction(db = db) {
            val rowsUpdated = UsersTable.update({ UsersTable.id eq id }) { stmt ->
                stmt[UsersTable.passwordHash] = passwordHash
            }
            rowsUpdated > 0
        }
}

private fun ResultRow.toUserRecord(): UserRecord = UserRecord(
    id = this[UsersTable.id],
    email = this[UsersTable.email],
    passwordHash = this[UsersTable.passwordHash],
    name = this[UsersTable.name],
    role = this[UsersTable.role],
)
