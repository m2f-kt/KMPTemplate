package com.m2f.core.observability

/**
 * Source of system prompts for an agent graph. The contract enforces a hard latency rule: [specFor]
 * / [systemFor] are INSTANT and NON-BLOCKING — they read an in-memory cache or a committed constant
 * and NEVER touch the network. The only network hop is [refresh], called off the hot path by a
 * background scheduler. This is what lets a prompt change without an app release while keeping the
 * latency-sensitive path free of a Langfuse round-trip.
 */
interface PromptProvider {

    /** Cached spec for [name] if present, else the committed constant. Instant; never fetches. */
    fun specFor(name: String): PromptSpec

    /** Hot-path convenience: the resolved system-prompt text (cache-or-constant). */
    fun systemFor(name: String): String = specFor(name).system

    /** Background refresh from the source of truth. Safe off the hot path; a no-op for constants. */
    suspend fun refresh()
}

/**
 * The zero-dependency fallback provider: always serves the committed [catalog]. Used as the default
 * in any graph (so an offline / Langfuse-less deployment behaves byte-identically) and as the inner
 * fallback of [com.m2f.core.observability.langfuse.LangfusePromptProvider].
 */
class ConstantPromptProvider(
    private val catalog: PromptCatalog = ExamplePromptCatalog,
) : PromptProvider {

    override fun specFor(name: String): PromptSpec =
        requireNotNull(catalog.byName(name)) {
            "No committed prompt constant for '$name' — only ${catalog.all.map { it.name }} exist."
        }

    override suspend fun refresh() = Unit
}
