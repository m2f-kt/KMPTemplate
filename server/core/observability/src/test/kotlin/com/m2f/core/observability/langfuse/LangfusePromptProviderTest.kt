package com.m2f.core.observability.langfuse

import com.m2f.core.observability.ExamplePromptCatalog
import com.m2f.core.observability.PromptSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.slf4j.LoggerFactory
import kotlin.test.Test

/**
 * The three safety properties of the Langfuse-backed prompt provider, each encoding WHY it matters:
 *  - **never fetch on the hot path** — a network hop on `specFor` would blow a latency budget.
 *  - **stale-on-error** — a transient Langfuse outage must NOT drop a good prompt back to the constant.
 *  - **fallback-to-constant** — before any successful fetch, resolution still works (corpus floor).
 */
class LangfusePromptProviderTest {

    private val logger = LoggerFactory.getLogger("LangfusePromptProviderTest")
    private val name = ExamplePromptCatalog.example.name

    private class FakeFetcher(var fail: Boolean = false) : PromptFetcher {
        var calls = 0
        override suspend fun fetchPrompt(name: String, label: String): PromptSpec {
            calls++
            if (fail) error("langfuse down")
            return PromptSpec(name = name, version = 7, system = "REMOTE:$name")
        }
    }

    @Test
    fun `specFor never fetches on the hot path`() = runTest {
        val fetcher = FakeFetcher()
        val provider = LangfusePromptProvider(client = fetcher, label = "production", logger = logger)
        repeat(5) { provider.specFor(name) }
        fetcher.calls shouldBe 0
    }

    @Test
    fun `specFor falls back to the committed constant before any refresh`() = runTest {
        val provider = LangfusePromptProvider(client = FakeFetcher(), label = "production", logger = logger)
        provider.systemFor(name) shouldBe ExamplePromptCatalog.example.system
    }

    @Test
    fun `after a successful refresh specFor returns the fetched prompt`() = runTest {
        val provider = LangfusePromptProvider(client = FakeFetcher(), label = "production", logger = logger)
        provider.refresh()
        provider.specFor(name).system shouldBe "REMOTE:$name"
        provider.specFor(name).version shouldBe 7
    }

    @Test
    fun `a failed refresh keeps the last-good value (stale-on-error)`() = runTest {
        val fetcher = FakeFetcher()
        val provider = LangfusePromptProvider(client = fetcher, label = "production", logger = logger)
        provider.refresh() // caches REMOTE
        fetcher.fail = true
        provider.refresh() // fails — must keep last-good, not revert
        provider.specFor(name).system shouldBe "REMOTE:$name"
    }

    @Test
    fun `a failed refresh with an empty cache falls back to the constant`() = runTest {
        val provider = LangfusePromptProvider(client = FakeFetcher(fail = true), label = "production", logger = logger)
        provider.refresh()
        provider.systemFor(name) shouldBe ExamplePromptCatalog.example.system
    }
}
