package com.m2f.core.observability.langfuse

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Langfuse datasets REST surface (the improvement flywheel): a golden/regression set built from real
 * failures. [createDataset] makes the set; [addItem] adds one input (e.g. the input of a flagged
 * trace) with an optional expected output. Experiments then run prompt candidates against the set and
 * score them (see the eval package) before promotion. Transport lives in [LangfuseRestClient].
 */
class LangfuseDatasetClient(
    private val rest: LangfuseRestClient,
) {

    /** Creates (idempotently, by name) a dataset. */
    suspend fun createDataset(name: String, description: String? = null) {
        val body = buildJsonObject {
            put("name", name)
            description?.let { put("description", it) }
        }
        rest.postJson(DATASETS_PATH, body)
    }

    /** Adds one item (a flagged input) to [datasetName] as a permanent regression case. */
    suspend fun addItem(
        datasetName: String,
        input: JsonElement,
        expectedOutput: JsonElement? = null,
        metadata: JsonObject? = null,
    ) {
        val body = buildJsonObject {
            put("datasetName", datasetName)
            put("input", input)
            expectedOutput?.let { put("expectedOutput", it) }
            metadata?.let { put("metadata", it) }
        }
        rest.postJson(DATASET_ITEMS_PATH, body)
    }

    private companion object {
        const val DATASETS_PATH = "/api/public/v2/datasets"
        const val DATASET_ITEMS_PATH = "/api/public/dataset-items"
    }
}
