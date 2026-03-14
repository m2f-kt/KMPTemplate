@file:OptIn(ExperimentalUuidApi::class)

package com.m2f.server.privacy.contract.tables

import com.m2f.server.auth.contract.tables.UsersTable
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import kotlin.uuid.ExperimentalUuidApi

object AccountDeletionRequestsTable : Table("account_deletion_requests") {
    val id = uuid("id").autoGenerate()
    val userId = uuid("user_id").references(UsersTable.id)
    val status = varchar("status", 20)
    val reason = text("reason").nullable()
    val scheduledAt = datetime("scheduled_at")
    val completedAt = datetime("completed_at").nullable()
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)
}
