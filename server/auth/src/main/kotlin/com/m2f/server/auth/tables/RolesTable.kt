package com.m2f.server.auth.tables

import org.jetbrains.exposed.v1.core.Table

/**
 * Exposed Table definition for the roles table.
 * Stores role definitions with a hierarchy level for authorization comparisons.
 * Seeded with User (1), Admin (2), PowerAdmin (3) during migration.
 */
object RolesTable : Table("roles") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 50).uniqueIndex()
    val level = integer("level")

    override val primaryKey = PrimaryKey(id)
}
