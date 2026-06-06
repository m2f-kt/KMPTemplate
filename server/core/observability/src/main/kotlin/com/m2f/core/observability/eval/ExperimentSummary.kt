package com.m2f.core.observability.eval

/** Aggregated judge result over a whole experiment (one prompt candidate × many samples). */
data class JudgeAggregate(
    val judge: Judge,
    val meanScore: Double,
    val passRate: Double,
    val allPass: Boolean,
)

/**
 * Roll-up of a dataset experiment: per-judge mean score + pass rate over every sample. Drives both
 * the version comparison ("did v8 beat v7?") and the promotion decision ([PromotionGate]).
 */
data class ExperimentSummary(
    val perJudge: List<JudgeAggregate>,
    val sampleCount: Int,
) {
    /** Every judge passed on every sample — the strict bar an experiment must clear. */
    val overallPass: Boolean get() = perJudge.isNotEmpty() && perJudge.all { it.allPass }

    /** The weakest judge's mean score — the value compared against a promotion threshold. */
    val minJudgeMeanScore: Double get() = perJudge.minOfOrNull { it.meanScore } ?: 0.0
}

/**
 * Aggregates per-sample judge results (one `List<JudgeResult>` per sample) into an [ExperimentSummary].
 * Pure — the testable core of comparing prompt versions and gating promotion. Judges are grouped by
 * [Judge.id] (the interface has no fixed enumeration), preserving first-seen order.
 */
fun summarizeExperiment(perSample: List<List<JudgeResult>>): ExperimentSummary {
    val byJudgeId = perSample.flatten().groupBy { it.judge.id }
    val orderedIds = perSample.flatten().map { it.judge.id }.distinct()
    val perJudge = orderedIds.mapNotNull { id ->
        byJudgeId[id]?.takeIf { it.isNotEmpty() }?.let { results ->
            JudgeAggregate(
                judge = results.first().judge,
                meanScore = results.map { it.verdict.score }.average(),
                passRate = results.count { it.verdict.pass }.toDouble() / results.size,
                allPass = results.all { it.verdict.pass },
            )
        }
    }
    return ExperimentSummary(perJudge = perJudge, sampleCount = perSample.size)
}
