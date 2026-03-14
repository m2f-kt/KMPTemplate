@file:OptIn(ExperimentalUuidApi::class)

package com.m2f.server.privacy.contract.tables

import com.m2f.server.auth.contract.tables.UsersTable
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import kotlin.uuid.ExperimentalUuidApi

object ConsentRecordsTable : Table("consent_records") {
    val id = uuid("id").autoGenerate()
    val userId = uuid("user_id").references(UsersTable.id)
    val consentType = varchar("consent_type", 50)
    val granted = bool("granted")
    val legalDocumentVersion = varchar("legal_document_version", 20)
    val ipAddress = varchar("ip_address", 45).nullable()
    val userAgent = text("user_agent").nullable()
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)
}
