package com.m2f.core.observability

/**
 * The set of centrally-managed prompts a deployment owns — the SAFETY FLOOR of the Langfuse
 * prompt-management design. When Langfuse returns nothing (offline / outage / dev / not-yet-seeded),
 * the resolver falls back to exactly these committed constants.
 *
 * This is a PLUGGABLE seam: a product module ships its own catalog (its real prompt constants) and
 * injects it. The observability module ships only [EmptyPromptCatalog] and [ExamplePromptCatalog]
 * so it compiles and tests stand alone without any product-specific prompt CONTENT.
 */
interface PromptCatalog {

    /** Every centrally-managed prompt — what a seed script pushes and the refresher fetches. */
    val all: Set<PromptSpec>

    /** The committed spec for [name], or null for an unknown name. */
    fun byName(name: String): PromptSpec? = all.firstOrNull { it.name == name }
}

/**
 * The zero-prompt catalog: a deployment that manages no prompts (pure tracing, no prompt management).
 * [byName] always returns null, so [ConstantPromptProvider.specFor] throws for any name — wire this
 * only when the prompt-management half is unused.
 */
object EmptyPromptCatalog : PromptCatalog {
    override val all: Set<PromptSpec> = emptySet()
}

/**
 * A one-entry example catalog so the module's tests + a fresh fork have a working reference. Replace
 * it with a product catalog carrying the real prompt constants the corpus gate guards.
 */
object ExamplePromptCatalog : PromptCatalog {

    /** The placeholder managed prompt. A fork swaps the [PromptSpec.system] for its real constant. */
    val example: PromptSpec = PromptSpec(
        name = "example",
        version = 1,
        system = "You are a helpful assistant. Replace this with your real committed system prompt.",
    )

    override val all: Set<PromptSpec> = setOf(example)
}
