package com.m2f.server.ai.structured

import ai.koog.prompt.Prompt
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import ai.koog.prompt.executor.model.StructureFixingParser
import ai.koog.prompt.executor.model.executeStructured
import ai.koog.prompt.llm.LLModel
import arrow.core.Either
import com.m2f.template.models.AppError
import kotlin.coroutines.cancellation.CancellationException

/**
 * Generic wrapper around Koog's executeStructured() API.
 * Maps Result<StructuredResponse<T>> to Either<AppError, T> for consistent error handling.
 *
 * Uses GoogleModels.Gemini2_5Flash for structured output calls (has Schema.JSON.Standard capability)
 * and Gemini2_0FlashLite001 for the fixing parser (retries malformed JSON).
 */
class StructuredOutputService(
    @PublishedApi internal val executor: MultiLLMPromptExecutor,
) {

    /**
     * Execute a structured output call returning a typed Kotlin data class.
     * The data class must be @Serializable with @LLMDescription annotations.
     */
    // Broad catch is intentional (see the comment below): Koog 1.0 lets transport throwables escape
    // executeStructured, and this wrapper's contract is to convert ANY failure to Either.Left so the
    // fail-open callers keep working. CancellationException is rethrown, so structured concurrency
    // is preserved.
    @Suppress("TooGenericExceptionCaught")
    suspend inline fun <reified T : Any> execute(
        prompt: Prompt,
        model: LLModel = GoogleModels.Gemini2_5Flash,
    ): Either<AppError, T> {
        // Koog 1.0: executeStructured runs the underlying executor.execute() OUTSIDE its runCatching
        // block, so transport failures (e.g. an invalid API key) now THROW out of executeStructured
        // instead of arriving as Result.failure (they were captured in 0.8.0). Catch the escaping
        // throwable here so this wrapper keeps returning Either.Left on any failure — preserving the
        // fail-open contract callers (RelevanceDetector) depend on. CancellationException is rethrown
        // to respect structured concurrency (project suspend-cancellation rule).
        val result = try {
            executor.executeStructured<T>(
                prompt = prompt,
                model = model,
                fixingParser = StructureFixingParser(
                    // Koog 1.0: GoogleModels.Gemini2_0FlashLite renamed to Gemini2_0FlashLite001 (same model tier).
                    model = GoogleModels.Gemini2_0FlashLite001,
                    retries = 2,
                ),
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            return Either.Left(AppError.AI.AgentFailed(detail = "Structured output failed: ${e.message}"))
        }
        return result.fold(
            onSuccess = { structuredResponse ->
                Either.Right(structuredResponse.data)
            },
            onFailure = { error ->
                Either.Left(AppError.AI.AgentFailed(detail = "Structured output failed: ${error.message}"))
            },
        )
    }
}
