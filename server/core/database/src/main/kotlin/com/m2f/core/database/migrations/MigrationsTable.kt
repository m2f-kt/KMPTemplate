package com.m2f.core.database.migrations

import org.jetbrains.exposed.v1.core.Table

private const val VERSION_VARCHAR_LENGTH = 255

/**
 * DBO table for tracking database migrations.
 */
internal object MigrationsTable : Table("migrations") {
    val version = varchar("version", VERSION_VARCHAR_LENGTH)
    val description = text("description")
    val appliedAt = long("applied_at")

    override val primaryKey = PrimaryKey(version)
}
