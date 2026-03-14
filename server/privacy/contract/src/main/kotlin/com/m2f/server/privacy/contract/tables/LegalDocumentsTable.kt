package com.m2f.server.privacy.contract.tables

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
object LegalDocumentsTable : Table("legal_documents") {
    val id = uuid("id").autoGenerate()
    val type = varchar("type", 50)
    val version = varchar("version", 20)
    val locale = varchar("locale", 5)
    val content = text("content")
    val publishedAt = datetime("published_at").defaultExpression(CurrentDateTime)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)
}
