package com.m2f.core.database.migrations

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.datetime
import org.jetbrains.exposed.v1.datetime.CurrentDateTime

private const val VERSION_VARCHAR_LENGTH = 255

/**
 * DBO table for tracking database migrations.
 */
internal object MigrationsTable : Table("migrations") {
    val version = varchar("version", VERSION_VARCHAR_LENGTH)
    val description = text("description")
    val appliedAt = datetime("applied_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(version)
}
