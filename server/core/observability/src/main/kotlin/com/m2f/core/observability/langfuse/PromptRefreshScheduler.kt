package com.m2f.core.observability.langfuse

import com.m2f.core.observability.PromptProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.slf4j.Logger
import kotlin.time.Duration

/**
 * Process-scoped background refresher for the centralized prompts. A [SupervisorJob] on an INJECTED
 * dispatcher (never `Dispatchers.IO` directly — Detekt `InjectDispatcher`) kicks an immediate
 * prefetch at boot and then re-fetches every [interval]. [start] launches and returns instantly, so
 * server boot is NEVER blocked on a Langfuse round-trip; a failed iteration is logged and retried
 * next tick (the provider keeps serving last-good/constant meanwhile).
 */
class PromptRefreshScheduler(
    private val provider: PromptProvider,
    private val interval: Duration,
    private val dispatcher: CoroutineDispatcher,
    private val logger: Logger,
) {
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    /** Launch the boot prefetch + periodic refresh loop. Non-blocking. */
    @Suppress("TooGenericExceptionCaught")
    fun start() {
        // Cancel the loop at process shutdown so the SupervisorJob scope never outlives the app.
        Runtime.getRuntime().addShutdownHook(Thread { stop() })
        scope.launch {
            while (isActive) {
                try {
                    provider.refresh()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Throwable) {
                    logger.warn("prompt refresh loop iteration failed (serving last-good/constant): {}", e.message)
                }
                delay(interval)
            }
        }
    }

    /** Cancel the refresh loop (graceful shutdown). */
    fun stop() {
        scope.cancel()
    }
}
