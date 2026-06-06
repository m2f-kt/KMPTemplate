package com.m2f.core.observability.langfuse

import com.m2f.core.observability.PromptConfig
import com.m2f.core.observability.PromptSpec
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/**
 * Read seam the [LangfusePromptProvider] depends on, so its cache / fallback / stale-on-error logic
 * can be unit-tested against a counting fake without an HTTP layer.
 */
interface PromptFetcher {
    suspend fun fetchPrompt(name: String, label: String): PromptSpec
}

/**
 * Langfuse Prompt-Management REST surface (text prompts):
 *  - [fetchPrompt] `GET /api/public/v2/prompts/{name}?label=…` — centralized fetch.
 *  - [createPrompt] `POST /api/public/v2/prompts` — seed (push the committed constant as v1).
 *  - [setLabel] `PATCH /api/public/v2/prompts/{name}/versions/{version}` — promotion (move the
 *    `production` label onto a version that passed the gate).
 *
 * All transport (auth, encode, non-2xx → throw) lives in [LangfuseRestClient]; this client only
 * builds bodies and parses responses. A thrown error → the caller falls back to last-good/constant.
 */
class LangfusePromptClient(
    private val rest: LangfuseRestClient,
) : PromptFetcher {

    /** Fetches the version of [name] currently carrying [label] (e.g. `production`). */
    override suspend fun fetchPrompt(name: String, label: String): PromptSpec =
        parsePrompt(rest.getText("$PROMPTS_PATH/$name", mapOf("label" to label)))

    /** Seeds/creates a new text-prompt version for [name] with the given [labels] and [config]. */
    suspend fun createPrompt(
        name: String,
        system: String,
        labels: List<String>,
        config: PromptConfig? = null,
    ) {
        val body = buildJsonObject {
            put("name", name)
            put("type", "text")
            put("prompt", system)
            put("labels", JsonArray(labels.map { JsonPrimitive(it) }))
            config?.let { put("config", it.toJsonObject()) }
        }
        rest.postJson(PROMPTS_PATH, body)
    }

    /** Moves [labels] onto an existing [version] of [name] (promotion gate). */
    suspend fun setLabel(name: String, version: Int, labels: List<String>) {
        val body = buildJsonObject {
            put("newLabels", JsonArray(labels.map { JsonPrimitive(it) }))
        }
        rest.patchJson("$PROMPTS_PATH/$name/versions/$version", body)
    }

    /** Parses a Langfuse text-prompt response body into a [PromptSpec]. Visible for testing. */
    internal fun parsePrompt(rawJson: String): PromptSpec {
        val obj = rest.json.parseToJsonElement(rawJson).jsonObject
        val name = obj["name"]?.jsonPrimitive?.content
            ?: error("Langfuse prompt response missing 'name'")
        val version = obj["version"]?.jsonPrimitive?.content?.toIntOrNull()
            ?: error("Langfuse prompt response missing integer 'version'")
        // A text prompt's `prompt` is a string; reject chat-array shapes.
        val system = (obj["prompt"] as? JsonPrimitive)?.takeIf { it.isString }?.content
            ?: error("Langfuse prompt '$name' is not a text prompt (expected string 'prompt')")
        val config = (obj["config"] as? JsonObject)?.let { cfg ->
            PromptConfig(
                model = (cfg["model"] as? JsonPrimitive)?.takeIf { it.isString }?.content,
                temperature = (cfg["temperature"] as? JsonPrimitive)?.doubleOrNull,
            )
        }
        return PromptSpec(name = name, version = version, system = system, config = config)
    }

    private companion object {
        const val PROMPTS_PATH = "/api/public/v2/prompts"
    }
}

private fun PromptConfig.toJsonObject(): JsonObject = buildJsonObject {
    model?.let { put("model", it) }
    temperature?.let { put("temperature", it) }
}
