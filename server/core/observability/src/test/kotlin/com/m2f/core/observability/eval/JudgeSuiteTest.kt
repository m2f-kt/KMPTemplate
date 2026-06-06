package com.m2f.core.observability.eval

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

/**
 * Suite aggregation, encoding WHY all-pass is the bar: promotion clears only when EVERY judge passes,
 * so a single failing dimension drops [allPass] to false. Also pins the default-judge contract and
 * the DATA-wrapped user prompt, with a fake [JudgeModel] (no live LLM).
 */
class JudgeSuiteTest {

    private class FakeModel(val pass: (Judge) -> Boolean) : JudgeModel {
        override suspend fun evaluate(judge: Judge, sample: JudgeSample): JudgeVerdict =
            JudgeVerdict(pass = pass(judge), score = if (pass(judge)) 1.0 else 0.0, reason = "fake")
    }

    private val sample = JudgeSample(input = "r", output = "c")

    @Test
    fun `suite runs the default judges in order`() = runTest {
        val results = JudgeSuite(FakeModel { true }).evaluate(sample)
        results.map { it.judge.id } shouldContainExactly defaultJudges.map { it.id }
        results.allPass().shouldBeTrue()
    }

    @Test
    fun `allPass is false when any judge fails`() = runTest {
        val results = JudgeSuite(FakeModel { it.id != FaithfulnessJudge.id }).evaluate(sample)
        results.allPass().shouldBeFalse()
    }

    @Test
    fun `an empty result set never passes`() {
        emptyList<JudgeResult>().allPass().shouldBeFalse()
    }

    @Test
    fun `default judges have stable ids and non-blank rubrics`() {
        defaultJudges.map { it.id } shouldContainExactly listOf("faithfulness", "injection_resistance")
        defaultJudges.forEach { it.systemPrompt.isNotBlank().shouldBeTrue() }
        defaultJudges.map { it.id }.toSet().size shouldBe defaultJudges.size
    }

    @Test
    fun `default user prompt wraps input + output as DATA`() {
        val prompt = FaithfulnessJudge.userPrompt(JudgeSample(input = "R", output = "C"))
        prompt shouldContain "<input>R</input>"
        prompt shouldContain "<output>C</output>"
        prompt shouldNotContain "delivered"
    }
}
