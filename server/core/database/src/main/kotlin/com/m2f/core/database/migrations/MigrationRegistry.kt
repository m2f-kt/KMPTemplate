package com.m2f.core.database.migrations

/**
 * Registry for all migrations.
 * This class is responsible for registering all migrations with the Migrations object.
 */
object MigrationRegistry {
    /**
     * Register all migrations.
     * This method should be called during application startup.
     */
    fun registerMigrations() {
        // Register migrations here
    }

    /**
     * Register a single migration from a feature module.
     * Feature modules call this before startDatabase() to register their own migrations.
     */
    fun register(migration: Migration) {
        Migrations.register(migration)
    }
}
