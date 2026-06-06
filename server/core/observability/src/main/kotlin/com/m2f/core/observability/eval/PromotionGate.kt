package com.m2f.core.observability.eval

import com.m2f.core.observability.langfuse.LangfusePromptClient

/** Label only a gate-cleared version ever carries; the runtime serves exactly this label. */
const val PRODUCTION_LABEL: String = "production"

/**
 * The promotion decision (pure). A staging prompt may move to `production` ONLY when BOTH gates clear:
 * the offline corpus gate AND the LLM-judge experiment (every judge passing with a weakest-mean-score
 * above [minMeanScore]). This is what reconciles "centralize + iterate fast" with "no regression ever
 * ships" — a casual UI edit can't reach users.
 */
object PromotionGate {

    fun shouldPromote(corpusPassed: Boolean, experiment: ExperimentSummary, minMeanScore: Double): Boolean =
        corpusPassed && experiment.overallPass && experiment.minJudgeMeanScore >= minMeanScore
}

/**
 * Applies a promotion: moves the `production` label onto [version] of [name] via the Langfuse REST
 * client, but ONLY if [PromotionGate.shouldPromote] returns true. Returns whether it promoted, so a
 * CI task / runbook can fail loudly when the gate blocks. RBAC (who may apply `production`) is
 * enforced separately in the Langfuse project settings.
 */
class PromptPromoter(private val client: LangfusePromptClient) {

    suspend fun promoteIfPassed(
        name: String,
        version: Int,
        corpusPassed: Boolean,
        experiment: ExperimentSummary,
        minMeanScore: Double,
    ): Boolean {
        val promote = PromotionGate.shouldPromote(
            corpusPassed = corpusPassed,
            experiment = experiment,
            minMeanScore = minMeanScore,
        )
        if (promote) {
            client.setLabel(name = name, version = version, labels = listOf(PRODUCTION_LABEL))
        }
        return promote
    }
}
