package com.m2f.core.observability

/**
 * Tunables a Langfuse prompt version may carry in its `config` blob. Parsed and surfaced for
 * observability. Whether to APPLY them to the live model is a product decision left to the caller —
 * this module only carries them.
 */
data class PromptConfig(
    val model: String? = null,
    val temperature: Double? = null,
)

/**
 * A resolved prompt: its name, version, the system-prompt text, and any [config]. Produced either
 * from a Langfuse fetch ([com.m2f.core.observability.langfuse.LangfusePromptProvider]) or from a
 * committed constant ([PromptCatalog]).
 */
data class PromptSpec(
    val name: String,
    val version: Int,
    val system: String,
    val config: PromptConfig? = null,
)
