@file:OptIn(ExperimentalUuidApi::class)

package com.m2f.server.groups

import com.m2f.core.database.migrations.Migration
import com.m2f.core.database.migrations.MigrationRegistry
import com.m2f.server.auth.tables.UsersTable
import com.m2f.server.groups.tables.GroupsTable
import com.m2f.server.groups.tables.InvitationsTable
import com.m2f.server.groups.tables.UserGroupMembershipsTable
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.transactions.TransactionManager
import kotlin.uuid.ExperimentalUuidApi

/**
 * Migration to create the groups table.
 */
internal class CreateGroupsTableMigration : Migration {
    override val version: String = "20260219000001"
    override val description: String = "Create groups table"

    override suspend fun migrate() {
        SchemaUtils.create(GroupsTable)
    }
}

/**
 * Migration to create the user_group_memberships join table.
 */
internal class CreateMembershipsTableMigration : Migration {
    override val version: String = "20260219000002"
    override val description: String = "Create user_group_memberships table"

    override suspend fun migrate() {
        SchemaUtils.create(UserGroupMembershipsTable)
    }
}

/**
 * Migration to seed the default group and assign all existing users.
 * If no users exist yet, the default group is still created with a sentinel createdBy
 * that will be updated when the first user registers.
 */
internal class SeedDefaultGroupMigration : Migration {
    override val version: String = "20260219000003"
    override val description: String = "Seed default group and assign all existing users"

    override suspend fun migrate() {
        // Find the first user to use as createdBy (prefer admin role)
        val firstUser = UsersTable
            .select(listOf(UsersTable.id))
            .firstOrNull()

        // Only seed if at least one user exists
        if (firstUser != null) {
            val creatorId = firstUser[UsersTable.id]

            // Insert default group
            val groupId = GroupsTable.insert {
                it[GroupsTable.name] = "Default"
                it[GroupsTable.slug] = "default"
                it[GroupsTable.description] = "Default group for all users"
                it[GroupsTable.createdBy] = creatorId
            }[GroupsTable.id]

            // Assign all existing users as MEMBER
            val allUsers = UsersTable
                .select(listOf(UsersTable.id))
                .toList()

            for (user in allUsers) {
                val userId = user[UsersTable.id]
                val role = if (userId == creatorId) "OWNER" else "MEMBER"
                UserGroupMembershipsTable.insert {
                    it[UserGroupMembershipsTable.userId] = userId
                    it[UserGroupMembershipsTable.groupId] = groupId
                    it[UserGroupMembershipsTable.role] = role
                }
            }
        }
    }
}

/**
 * Migration to seed dev/test groups for development environments.
 * Idempotent: checks slug existence before inserting.
 */
internal class SeedDevTestGroupsMigration : Migration {
    override val version: String = "20260219000004"
    override val description: String = "Seed dev/test groups (Alpha Team, Beta Team)"

    override suspend fun migrate() {
        // Find the first user to use as createdBy
        val firstUser = UsersTable
            .select(listOf(UsersTable.id))
            .firstOrNull() ?: return // Skip if no users exist

        val creatorId = firstUser[UsersTable.id]

        // Create Alpha Team if not exists
        val existingAlpha = GroupsTable
            .select(listOf(GroupsTable.id))
            .where { GroupsTable.slug eq "alpha-team" }
            .firstOrNull()

        if (existingAlpha == null) {
            GroupsTable.insert {
                it[GroupsTable.name] = "Alpha Team"
                it[GroupsTable.slug] = "alpha-team"
                it[GroupsTable.description] = "First dev/test group"
                it[GroupsTable.createdBy] = creatorId
            }
        }

        // Create Beta Team if not exists
        val existingBeta = GroupsTable
            .select(listOf(GroupsTable.id))
            .where { GroupsTable.slug eq "beta-team" }
            .firstOrNull()

        if (existingBeta == null) {
            GroupsTable.insert {
                it[GroupsTable.name] = "Beta Team"
                it[GroupsTable.slug] = "beta-team"
                it[GroupsTable.description] = "Second dev/test group"
                it[GroupsTable.createdBy] = creatorId
            }
        }
    }
}

/**
 * Migration to create the invitations table.
 */
internal class CreateInvitationsTableMigration : Migration {
    override val version: String = "20260222000001"
    override val description: String = "Create invitations table"

    override suspend fun migrate() {
        SchemaUtils.create(InvitationsTable)
    }
}

/**
 * Migration to add revokedAt column to the invitations table.
 * Allows admins to revoke pending invitations.
 */
internal class AddRevokedAtColumnMigration : Migration {
    override val version: String = "20260226000001"
    override val description: String = "Add revoked_at column to invitations table"

    override suspend fun migrate() {
        TransactionManager.current().exec("ALTER TABLE invitations ADD COLUMN IF NOT EXISTS revoked_at TIMESTAMP NULL")
    }
}

/**
 * Register all group-related database migrations.
 * Must be called before startDatabase() so migrations are available when the database starts.
 */
fun registerGroupMigrations() {
    MigrationRegistry.register(CreateGroupsTableMigration())
    MigrationRegistry.register(CreateMembershipsTableMigration())
    MigrationRegistry.register(SeedDefaultGroupMigration())
    MigrationRegistry.register(SeedDevTestGroupsMigration())
    MigrationRegistry.register(CreateInvitationsTableMigration())
    MigrationRegistry.register(AddRevokedAtColumnMigration())
}
