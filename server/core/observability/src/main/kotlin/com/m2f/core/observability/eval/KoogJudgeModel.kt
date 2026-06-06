package com.m2f.core.observability.eval

import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.prompt.executor.model.StructureFixingParser
import ai.koog.prompt.executor.model.executeStructured
import ai.koog.prompt.llm.LLModel

/**
 * Production [JudgeModel]: runs a judge through Koog's native structured-output path so the model
 * returns a schema-valid [JudgeVerdict] with the fixing parser repairing malformed JSON. Live judging
 * requires an LLM, so this path is exercised by the promotion experiment / prod-sampling job (UAT),
 * not by unit tests — the suite logic is tested via a fake [JudgeModel].
 */
class KoogJudgeModel(
    private val executor: PromptExecutor,
    private val model: LLModel,
) : JudgeModel {

    override suspend fun evaluate(judge: Judge, sample: JudgeSample): JudgeVerdict {
        val judgePrompt = prompt("judge-${judge.id}") {
            system(judge.systemPrompt)
            user(judge.userPrompt(sample))
        }
        return executor.executeStructured<JudgeVerdict>(
            prompt = judgePrompt,
            model = model,
            fixingParser = StructureFixingParser(model = model, retries = JUDGE_FIX_RETRIES),
        ).getOrThrow().data
    }

    private companion object {
        const val JUDGE_FIX_RETRIES = 2
    }
}
