package com.m2f.core.database.migrations

import com.m2f.core.config.configuration.Configuration
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.slf4j.LoggerFactory
import kotlin.time.ExperimentalTime

/**
 * Base interface for database migrations.
 * All migrations should implement this interface and be registered in the [Migrations] object.
 */
interface Migration {
    /**
     * The version of the migration. This should be a unique identifier for the migration.
     * It's recommended to use a timestamp in the format YYYYMMDDHHMMSS.
     */
    val version: String

    /**
     * The description of the migration. This should be a brief description of what the migration does.
     */
    val description: String

    /**
     * Execute the migration.
     * This method should contain the SQL statements to be executed for the migration.
     */
    suspend fun migrate()
}

/**
 * Object to register and execute migrations.
 */
object Migrations {
    private val logger = LoggerFactory.getLogger("DATABASE")
    private val migs = mutableListOf<Migration>()

    /**
     * Register a migration.
     *
     * Deduplicates by [Migration.version] — the registry is a process-global
     * `mutableListOf` and feature modules call `registerXxxMigrations()`
     * unconditionally on every test-class setup. Without this guard, repeated
     * registrations push duplicate `Migration` instances onto the list and the
     * subsequent `migrate()` call attempts to insert the same row into the
     * `migrations` table, violating its primary key.
     *
     * @param migration The migration to register; ignored if a migration with
     *   the same `version` is already registered.
     */
    fun register(migration: Migration) {
        if (migs.none { it.version == migration.version }) {
            migs.add(migration)
        }
    }

    /**
     * Execute all registered migrations.
     * @param database The database to execute the migrations on.
     */
    @OptIn(ExperimentalTime::class)
    context(_: Configuration)
    suspend fun migrate(database: R2dbcDatabase) {
        // Create the migrations table if it doesn't exist using DBO
        suspendTransaction(db = database) {
            SchemaUtils.create(MigrationsTable)
            // Get the list of applied migrations using DBO
            val appliedMigrations = mutableSetOf<String>()
            MigrationsTable.select(MigrationsTable.version).collect { resultRow ->
                    appliedMigrations.add(resultRow[MigrationsTable.version])
                }

            // Execute migrations that haven't been applied yet
            migs.sortedBy { it.version }.filter { it.version !in appliedMigrations }.forEach { migration ->
                logger.info(
                    "Executing migration | version={}, description={}",
                    migration.version, migration.description
                )
                    migration.migrate()

                    // Record the migration as applied using DBO
                    MigrationsTable.insert {
                        it[version] = migration.version
                        it[description] = migration.description
                    }

                    logger.info("Migration executed successfully | version={}", migration.version)
                }
            }

        }
}
