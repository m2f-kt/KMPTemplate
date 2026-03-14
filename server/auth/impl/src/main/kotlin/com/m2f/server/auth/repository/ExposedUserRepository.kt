@file:OptIn(ExperimentalUuidApi::class)

package com.m2f.server.auth.repository

import com.m2f.server.auth.contract.repository.UserRecord
import com.m2f.server.auth.contract.repository.UserRepository
import com.m2f.server.auth.contract.tables.UsersTable
import com.m2f.template.models.UserRole
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
 * Exposed R2DBC implementation of [UserRepository].
 */
class ExposedUserRepository(private val db: R2dbcDatabase) : UserRepository {

    override suspend fun findByEmail(email: String): UserRecord? = suspendTransaction(db = db) {
        UsersTable
            .select(UsersTable.columns)
            .where { UsersTable.email eq email }
            .singleOrNull()
            ?.toUserRecord()
    }

    override suspend fun findById(id: Uuid): UserRecord? = suspendTransaction(db = db) {
        UsersTable
            .select(UsersTable.columns)
            .where { UsersTable.id eq id }
            .singleOrNull()
            ?.toUserRecord()
    }

    override suspend fun insert(
        email: String,
        passwordHash: String,
        name: String,
        role: UserRole,
    ): Uuid = suspendTransaction(db = db) {
        UsersTable.insert {
            it[UsersTable.email] = email
            it[UsersTable.passwordHash] = passwordHash
            it[UsersTable.name] = name
            it[UsersTable.roleId] = roleIdForRole(role)
        }[UsersTable.id]
    }

    override suspend fun updateProfile(id: Uuid, name: String?, email: String?): Boolean =
        suspendTransaction(db = db) {
            val rowsUpdated = UsersTable.update({ UsersTable.id eq id }) { stmt ->
                name?.let { stmt[UsersTable.name] = it }
                email?.let { stmt[UsersTable.email] = it }
            }
            rowsUpdated > 0
        }

    override suspend fun updatePasswordHash(id: Uuid, passwordHash: String): Boolean =
        suspendTransaction(db = db) {
            val rowsUpdated = UsersTable.update({ UsersTable.id eq id }) { stmt ->
                stmt[UsersTable.passwordHash] = passwordHash
            }
            rowsUpdated > 0
        }

    override suspend fun count(): Long = suspendTransaction(db = db) {
        UsersTable.select(UsersTable.columns).count()
    }

    override suspend fun updateAvatarUrl(id: Uuid, avatarUrl: String): Boolean =
        suspendTransaction(db = db) {
            val rowsUpdated = UsersTable.update({ UsersTable.id eq id }) { stmt ->
                stmt[UsersTable.avatarUrl] = avatarUrl
            }
            rowsUpdated > 0
        }

    override suspend fun updateProcessingRestricted(id: Uuid, restricted: Boolean): Boolean =
        suspendTransaction(db = db) {
            val rowsUpdated = UsersTable.update({ UsersTable.id eq id }) { stmt ->
                stmt[processingRestricted] = restricted
            }
            rowsUpdated > 0
        }
}

private fun roleIdForRole(role: UserRole): Int = when (role) {
    UserRole.User -> 1
    UserRole.Admin -> 2
    UserRole.PowerAdmin -> 3
}

private fun ResultRow.toUserRecord(): UserRecord = UserRecord(
    id = this[UsersTable.id],
    email = this[UsersTable.email],
    passwordHash = this[UsersTable.passwordHash],
    name = this[UsersTable.name],
    role = when (this[UsersTable.roleId]) {
        2 -> UserRole.Admin
        3 -> UserRole.PowerAdmin
        else -> UserRole.User
    },
    avatarUrl = this[UsersTable.avatarUrl],
    processingRestricted = this[UsersTable.processingRestricted],
)
