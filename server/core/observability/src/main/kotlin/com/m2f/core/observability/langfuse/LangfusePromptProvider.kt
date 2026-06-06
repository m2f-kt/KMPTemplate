package com.m2f.core.observability.langfuse

import com.m2f.core.observability.ConstantPromptProvider
import com.m2f.core.observability.ExamplePromptCatalog
import com.m2f.core.observability.PromptCatalog
import com.m2f.core.observability.PromptProvider
import com.m2f.core.observability.PromptSpec
import kotlinx.coroutines.CancellationException
import org.slf4j.Logger
import java.util.concurrent.atomic.AtomicReference

/**
 * Langfuse-backed [PromptProvider] with three safety properties:
 *  1. **Hot-path-free:** [specFor] only reads an in-memory [AtomicReference] — it NEVER fetches, so a
 *     latency-sensitive path can call it freely. Fetching happens exclusively in [refresh], which a
 *     background scheduler runs off the hot path.
 *  2. **Stale-on-error:** a failed fetch keeps the last good value in the cache (the map is only
 *     overwritten on success) — a transient Langfuse outage degrades to last-good, then to constant.
 *  3. **Fallback-to-constant:** any name with no cached value resolves through [fallback]
 *     ([ConstantPromptProvider]), so dev / offline / not-yet-seeded all serve the committed prompt
 *     byte-identically — the corpus gate's source of truth is never bypassed.
 *
 * @param catalog the set of managed prompts to refresh (pluggable; a fork ships its own).
 */
class LangfusePromptProvider(
    private val client: PromptFetcher,
    private val label: String,
    private val logger: Logger,
    private val catalog: PromptCatalog = ExamplePromptCatalog,
    private val fallback: PromptProvider = ConstantPromptProvider(catalog),
) : PromptProvider {

    private val cache = AtomicReference<Map<String, PromptSpec>>(emptyMap())

    override fun specFor(name: String): PromptSpec =
        cache.get()[name] ?: fallback.specFor(name)

    override suspend fun refresh() {
        catalog.all.forEach { constant ->
            fetchOrNull(constant.name)?.let { fetched ->
                cache.updateAndGet { current -> current + (constant.name to fetched) }
                // Positive confirmation the managed prompt is live (operators shouldn't have to curl
                // Langfuse to know whether a run used the managed prompt vs the constant floor).
                logger.info(
                    "Langfuse prompt '{}' refreshed from label '{}': now serving v{}",
                    constant.name,
                    label,
                    fetched.version,
                )
            }
        }
    }

    /**
     * Fetch one prompt, returning null on any failure (so the caller keeps last-good/constant).
     * Cancellation-correct: rethrows [CancellationException] rather than swallowing it as a "failure".
     */
    @Suppress("TooGenericExceptionCaught")
    private suspend fun fetchOrNull(name: String): PromptSpec? =
        try {
            client.fetchPrompt(name = name, label = label)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            logger.warn("Langfuse prompt refresh failed for '{}' (keeping last-good/constant): {}", name, e.message)
            null
        }
}
