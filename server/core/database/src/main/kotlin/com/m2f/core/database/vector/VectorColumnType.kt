package com.m2f.core.database.vector

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.ColumnType
import org.jetbrains.exposed.v1.core.Table

/**
 * Custom Exposed column type for PostgreSQL pgvector `vector(dimensions)`.
 *
 * Stores float arrays as pgvector strings: "[0.1,0.2,0.3]"
 * R2DBC postgresql driver handles vector as String representation.
 *
 * @param dimensions The fixed dimensionality of the vector (e.g., 768 for text-embedding-004)
 */
class VectorColumnType(private val dimensions: Int) : ColumnType<List<Float>>() {

    override fun sqlType(): String = "vector($dimensions)"

    override fun valueFromDB(value: Any): List<Float> {
        return when (value) {
            is String -> parseVectorString(value)
            else -> error("Unexpected value type for vector column: ${value::class}")
        }
    }

    override fun notNullValueToDB(value: List<Float>): String {
        require(value.size == dimensions) {
            "Vector dimension mismatch: expected $dimensions, got ${value.size}"
        }
        return "[${value.joinToString(",")}]"
    }

    override fun nonNullValueToString(value: List<Float>): String {
        return "'${notNullValueToDB(value)}'"
    }

    private fun parseVectorString(raw: String): List<Float> {
        return raw.trim('[', ']')
            .split(',')
            .map { it.trim().toFloat() }
    }
}

/**
 * Register a vector column on a Table.
 *
 * Usage:
 * ```kotlin
 * object MyTable : Table("my_table") {
 *     val embedding = vector("embedding", 768)
 * }
 * ```
 */
fun Table.vector(name: String, dimensions: Int): Column<List<Float>> =
    registerColumn(name, VectorColumnType(dimensions))
