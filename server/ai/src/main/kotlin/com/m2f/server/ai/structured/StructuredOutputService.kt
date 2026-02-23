package com.m2f.server.ai.structured

import ai.koog.prompt.dsl.Prompt
import ai.koog.prompt.executor.llms.SingleLLMPromptExecutor
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.structure.StructureFixingParser
import ai.koog.prompt.structure.executeStructured
import arrow.core.Either
import com.m2f.template.models.AppError

/**
 * Generic wrapper around Koog's executeStructured() API.
 * Maps Result<StructuredResponse<T>> to Either<AppError, T> for consistent error handling.
 *
 * Uses GoogleModels.Gemini2_5Flash for structured output calls (has Schema.JSON.Standard capability)
 * and Gemini2_0FlashLite for the fixing parser (retries malformed JSON).
 */
class StructuredOutputService(
    @PublishedApi internal val executor: SingleLLMPromptExecutor,
) {

    /**
     * Execute a structured output call returning a typed Kotlin data class.
     * The data class must be @Serializable with @LLMDescription annotations.
     */
    suspend inline fun <reified T : Any> execute(
        prompt: Prompt,
        model: LLModel = GoogleModels.Gemini2_5Flash,
    ): Either<AppError, T> {
        val result = executor.executeStructured<T>(
            prompt = prompt,
            model = model,
            fixingParser = StructureFixingParser(
                model = GoogleModels.Gemini2_0FlashLite,
                retries = 2,
            ),
        )
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
