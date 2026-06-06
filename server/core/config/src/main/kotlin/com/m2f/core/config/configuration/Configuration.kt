package com.m2f.core.config.configuration

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.m2f.core.config.server.BootError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class Configuration(
    /**
     * Dispatcher for R2DBC database operations.
     * Bounded to 16 threads to match a typical R2DBC connection pool size.
     * R2DBC is non-blocking, but limiting parallelism prevents
     * overwhelming the connection pool under load.
     * Uses Dispatchers.IO.limitedParallelism() to reuse the elastic IO thread pool.
     */
    val dbDispatcher: CoroutineDispatcher = Dispatchers.IO.limitedParallelism(16),

    /**
     * Dispatcher for AI/LLM network operations (streaming, embeddings).
     * Bounded to 8 threads. Separate from DB to prevent long-running
     * AI streaming calls from starving database queries.
     * Uses Dispatchers.IO.limitedParallelism() to reuse the elastic IO thread pool.
     */
    val aiDispatcher: CoroutineDispatcher = Dispatchers.IO.limitedParallelism(8),

    /**
     * Dispatcher for CPU-bound work (password hashing, JSON serialization).
     * Uses Dispatchers.Default which is sized to CPU core count.
     */
    val computeDispatcher: CoroutineDispatcher = Dispatchers.Default,

    val maxDatabaseAttempts: Int = 3,
    val env: Env = Env(),
) {
    companion object {
        /** Host substrings that mark a non-production (local/dev) deployment. */
        private val LOCAL_HOST_MARKERS = listOf("localhost", "127.0.0.1", "0.0.0.0")

        /**
         * Boot-time invariant assertions. Call from `Application.module()` (and ideally
         * from `main()`) before the server starts accepting traffic — a `Left` result
         * MUST abort startup so orchestrators (Docker, systemd) restart the process once
         * the operator fixes the environment.
         *
         * Generic invariants only (no product-specific logic):
         * - The JWT secret is not the committed placeholder [DEFAULT_JWT_SECRET] when
         *   running in production (derived from a non-local [Env.Http.baseUrl]).
         * - Configured URLs ([Env.Http.baseUrl], [Env.Http.appUrl]) parse with a scheme
         *   and host.
         */
        fun validate(env: Env): Either<BootError, Env> = when {
            isProduction(env) && env.auth.secret == DEFAULT_JWT_SECRET ->
                BootError.MissingRequiredEnv("JWT_SECRET").left()
            else -> validateUrls(env)
        }

        /**
         * Production is inferred when [Env.Http.baseUrl] does not point at a local host.
         * Keeps [validate] dependency-free of any extra env flag while still letting local
         * dev run with the committed placeholder secret.
         */
        private fun isProduction(env: Env): Boolean {
            val host = runCatching { java.net.URI(env.http.baseUrl).host }.getOrNull()
                ?: return false
            return LOCAL_HOST_MARKERS.none { host.equals(it, ignoreCase = true) }
        }

        private fun validateUrls(env: Env): Either<BootError, Env> = when {
            !isParseableUrl(env.http.baseUrl) ->
                BootError.InvalidConfig("http.baseUrl", "'${env.http.baseUrl}' is not a valid URL").left()
            !isParseableUrl(env.http.appUrl) ->
                BootError.InvalidConfig("http.appUrl", "'${env.http.appUrl}' is not a valid URL").left()
            else -> env.right()
        }

        /** A URL is acceptable when it parses with both a scheme and a host. */
        private fun isParseableUrl(value: String): Boolean {
            val uri = runCatching { java.net.URI(value) }.getOrNull() ?: return false
            return !uri.scheme.isNullOrBlank() && !uri.host.isNullOrBlank()
        }
    }
}
