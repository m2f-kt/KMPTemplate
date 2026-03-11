@file:OptIn(ExperimentalUuidApi::class)

package com.m2f.server.groups.contract.tables

import com.m2f.server.auth.contract.tables.UsersTable
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import kotlin.uuid.ExperimentalUuidApi

private const val NAME_LENGTH = 100
private const val SLUG_LENGTH = 100

/**
 * Exposed Table definition for the groups table.
 * Each group has a unique slug for URL-friendly identification.
 */
object GroupsTable : Table("groups") {
    val id = uuid("id").autoGenerate()
    val name = varchar("name", NAME_LENGTH)
    val slug = varchar("slug", SLUG_LENGTH).uniqueIndex()
    val description = text("description").default("")
    val createdBy = uuid("created_by").references(UsersTable.id)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)
}
