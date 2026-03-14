@file:OptIn(ExperimentalUuidApi::class)

package com.m2f.server.privacy.contract.tables

import com.m2f.server.auth.contract.tables.UsersTable
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import kotlin.uuid.ExperimentalUuidApi

object DataExportRequestsTable : Table("data_export_requests") {
    val id = uuid("id").autoGenerate()
    val userId = uuid("user_id").references(UsersTable.id)
    val status = varchar("status", 20)
    val fileKey = varchar("file_key", 512).nullable()
    val completedAt = datetime("completed_at").nullable()
    val expiresAt = datetime("expires_at").nullable()
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)
}
