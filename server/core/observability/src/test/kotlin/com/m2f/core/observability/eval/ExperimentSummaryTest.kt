package com.m2f.core.observability.eval

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import kotlin.test.Test

/** Pure experiment aggregation — the value used to compare prompt versions and gate promotion. */
class ExperimentSummaryTest {

    private fun result(judge: Judge, pass: Boolean, score: Double) =
        JudgeResult(judge = judge, verdict = JudgeVerdict(pass = pass, score = score, reason = "r"))

    @Test
    fun `mean score, pass rate, and allPass are computed per judge`() {
        val perSample = listOf(
            listOf(result(FaithfulnessJudge, pass = true, score = 1.0)),
            listOf(result(FaithfulnessJudge, pass = false, score = 0.0)),
        )
        val summary = summarizeExperiment(perSample)

        summary.sampleCount shouldBe 2
        val faithfulness = summary.perJudge.single { it.judge.id == FaithfulnessJudge.id }
        faithfulness.meanScore shouldBe 0.5
        faithfulness.passRate shouldBe 0.5
        faithfulness.allPass.shouldBeFalse()
        summary.overallPass.shouldBeFalse()
        summary.minJudgeMeanScore shouldBe 0.5
    }

    @Test
    fun `all judges passing yields overallPass and a high min mean`() {
        val perSample = listOf(
            defaultJudges.map { result(it, pass = true, score = 0.9) },
            defaultJudges.map { result(it, pass = true, score = 1.0) },
        )
        val summary = summarizeExperiment(perSample)

        summary.overallPass.shouldBeTrue()
        summary.minJudgeMeanScore shouldBe 0.95
    }

    @Test
    fun `an empty experiment never passes`() {
        val summary = summarizeExperiment(emptyList())
        summary.overallPass.shouldBeFalse()
        summary.minJudgeMeanScore shouldBe 0.0
    }
}
