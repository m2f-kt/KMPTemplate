package com.m2f.server.ai

import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.google.GoogleLLMClient
import ai.koog.prompt.executor.llms.SingleLLMPromptExecutor
import com.m2f.server.ai.rag.RelevanceCheck
import com.m2f.server.ai.rag.RelevanceDetector
import com.m2f.server.ai.structured.StructuredOutputService
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.serializer
import org.junit.Assume
import org.junit.Test

/**
 * Structured output tests covering DEBT-03, AISTR-01, AISTR-02, AISTR-03.
 *
 * Two categories:
 * 1. Unit tests (always run): verify service wiring, data class annotations, fail-open behavior
 * 2. Integration tests (conditional): require AI_GOOGLE_API_KEY to test real Gemini API
 */
class StructuredOutputTest {

    // ---- Unit Tests (always run) ----

    @Test
    fun `RelevanceCheck has correct serialization annotations`() {
        // Verify @Serializable and @SerialName are properly configured (AISTR-02)
        val serializer = serializer<RelevanceCheck>()
        serializer shouldNotBe null
        serializer.descriptor.serialName shouldBe "RelevanceCheck"
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun `RelevanceCheck has expected fields`() {
        // Verify data class structure for structured output (AISTR-01)
        val descriptor = serializer<RelevanceCheck>().descriptor
        descriptor.elementsCount shouldBe 2

        val fieldNames = (0 until descriptor.elementsCount).map { descriptor.getElementName(it) }
        fieldNames shouldBe listOf("needsContext", "searchQuery")
    }

    @Test
    fun `RelevanceDetector defaults to false on error - fail open`() = kotlinx.coroutines.test.runTest {
        // Create a StructuredOutputService with a fake executor that will fail
        // since we have no real API key. The RelevanceDetector should fail open.
        val apiKey = "test-invalid-key"
        val client = GoogleLLMClient(apiKey)
        val executor = SingleLLMPromptExecutor(client)
        val service = StructuredOutputService(executor)
        val detector = RelevanceDetector(service)

        // This should NOT throw; it should fail open with needsContext=false
        val result = detector.check("What is the capital of France?")
        result.needsContext shouldBe false
        result.searchQuery shouldBe null
    }

    // ---- Integration Tests (conditional on API key) ----

    @Test
    fun `executeStructured returns typed RelevanceCheck with real API`() = kotlinx.coroutines.test.runTest {
        val apiKey = System.getenv("AI_GOOGLE_API_KEY")
        Assume.assumeNotNull("Set AI_GOOGLE_API_KEY to run structured output integration tests", apiKey)

        val client = GoogleLLMClient(apiKey!!)
        val executor = SingleLLMPromptExecutor(client)
        val service = StructuredOutputService(executor)

        val checkPrompt = prompt("test-structured") {
            system("Determine if this query needs document context.")
            user("What did I upload yesterday?")
        }

        val result = service.execute<RelevanceCheck>(checkPrompt)

        // Should return Either.Right with a valid RelevanceCheck (AISTR-03)
        result.shouldBeInstanceOf<arrow.core.Either.Right<RelevanceCheck>>()
        val check = result.getOrNull()!!
        check shouldNotBe null
        // "What did I upload yesterday?" clearly references uploaded documents
        check.needsContext shouldBe true
        check.searchQuery shouldNotBe null
    }

    @Test
    fun `RelevanceDetector correctly identifies general knowledge query with real API`() = kotlinx.coroutines.test.runTest {
        val apiKey = System.getenv("AI_GOOGLE_API_KEY")
        Assume.assumeNotNull("Set AI_GOOGLE_API_KEY to run structured output integration tests", apiKey)

        val client = GoogleLLMClient(apiKey!!)
        val executor = SingleLLMPromptExecutor(client)
        val service = StructuredOutputService(executor)
        val detector = RelevanceDetector(service)

        // General knowledge query should NOT need document context
        val result = detector.check("What is 2 + 2?")
        result.needsContext shouldBe false
    }
}
