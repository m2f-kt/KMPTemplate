package com.m2f.core.observability.langfuse

import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Langfuse scores REST surface: attaches a quality score to an existing trace via
 * `POST /api/public/scores`. Judges write NUMERIC scores keyed to a judge id with the verdict reason
 * as the comment; human annotation can be posted as a STRING (CATEGORICAL) score. The `traceId` is
 * the one the span adapter already produces per run. Transport lives in [LangfuseRestClient].
 */
class LangfuseScoresClient(
    private val rest: LangfuseRestClient,
) {

    /** Posts a 0..1 NUMERIC score for [name] onto [traceId], with an optional [comment]. */
    suspend fun postNumericScore(traceId: String, name: String, value: Double, comment: String? = null) {
        val body = buildJsonObject {
            put("traceId", traceId)
            put("name", name)
            put("value", value)
            put("dataType", "NUMERIC")
            comment?.let { put("comment", it) }
        }
        rest.postJson(SCORES_PATH, body)
    }

    /** Posts a CATEGORICAL score (e.g. a human reviewer's failure-type label) onto [traceId]. */
    suspend fun postCategoricalScore(traceId: String, name: String, value: String, comment: String? = null) {
        val body = buildJsonObject {
            put("traceId", traceId)
            put("name", name)
            put("value", value)
            put("dataType", "CATEGORICAL")
            comment?.let { put("comment", it) }
        }
        rest.postJson(SCORES_PATH, body)
    }

    private companion object {
        const val SCORES_PATH = "/api/public/scores"
    }
}
