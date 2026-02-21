package com.m2f.core.config.configuration

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
)
