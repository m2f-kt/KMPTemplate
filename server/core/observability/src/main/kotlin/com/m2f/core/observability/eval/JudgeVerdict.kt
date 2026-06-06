package com.m2f.core.observability.eval

import ai.koog.agents.core.tools.annotations.LLMDescription
import kotlinx.serialization.Serializable

/**
 * One LLM-judge's verdict on one quality dimension. The `@Serializable` type IS the schema: judges
 * run through Koog's native structured-output path, so the model returns exactly these three fields.
 * [score] feeds Langfuse as a NUMERIC score; [reason] is attached as the score comment; [pass] gates
 * the promotion experiment.
 */
@Serializable
data class JudgeVerdict(
    @property:LLMDescription("true if the output PASSES this quality dimension, false if it fails it")
    val pass: Boolean,
    @property:LLMDescription("quality score from 0.0 (worst) to 1.0 (perfect) for this dimension")
    val score: Double,
    @property:LLMDescription("one short sentence citing the specific evidence for the verdict")
    val reason: String,
)
