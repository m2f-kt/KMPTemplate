package com.m2f.core.database.connection

private const val R2DBC_URL = "r2dbc:postgresql://localhost:5436/application"
private const val R2DBC_HOST = "localhost"
private const val R2DBC_PORT = 5436
private const val R2DBC_DATABASE = "application"
private const val R2DBC_USER: String = "postgres"
private const val R2DBC_PW: String = "postgres"
private const val R2DBC_DRIVER: String = "postgresql"

data class DataSource(
    val url: String = getR2dbcUrl(),
    val host: String = System.getenv("PGHOST") ?: R2DBC_HOST,
    val port: Int = System.getenv("PGPORT")?.toIntOrNull() ?: R2DBC_PORT,
    val database: String = System.getenv("PGDATABASE") ?: R2DBC_DATABASE,
    val username: String = System.getenv("PGUSER") ?: R2DBC_USER,
    val password: String = System.getenv("PGPASSWORD") ?: R2DBC_PW,
    val driver: String = R2DBC_DRIVER,
) {

    companion object {

        /**
         * Constructs a R2DBC URL from the environment variable DATABASE_URL or falls back to default R2DBC_URL.
         * Transforms postgres/postgresql URLs to proper R2DBC format by:
         * 1. Removing authentication info from the URL
         * 2. Adding 'r2dbc:' prefix if needed
         *
         * @return formatted R2DBC URL as String
         */
        private fun getR2dbcUrl(): String {
            val databaseUrl = System.getenv("DATABASE_URL") ?: R2DBC_URL
            return databaseUrl
                .replace(Regex("^(?:postgres|postgresql)://.*?:.*?@"), "postgresql://")
                .replace(Regex("^(?:postgres|postgresql)://"), "r2dbc:postgresql://")
                .replace(Regex("^jdbc:"), "r2dbc:")
        }
    }
}
