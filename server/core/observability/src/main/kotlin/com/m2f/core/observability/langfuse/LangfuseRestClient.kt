package com.m2f.core.observability.langfuse

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import java.util.Base64

/**
 * Thin shared client for the Langfuse Public REST API (`/api/public/...`, HTTP Basic `pk:sk`). There
 * is no official Langfuse Kotlin/JVM SDK, so prompt fetch, label promotion, score push and dataset
 * writes all go through this one authenticated handle. It owns the base URL, the precomputed
 * `Authorization` header, the shared lenient [json], AND the three request primitives ([getText],
 * [postJson], [patchJson]) — so the auth/encode/error-handling plumbing lives in exactly one place
 * and each domain client ([LangfusePromptClient], [LangfuseScoresClient], [LangfuseDatasetClient])
 * only builds its JSON body.
 *
 * Each primitive throws on a non-2xx (with method + path + status) so the caller treats it as a
 * transient failure and falls back to last-good/constant — Langfuse problems never reach the hot
 * path. The injected [client] must have BOUNDED timeouts (set in DI); these are background/eval
 * calls, never the hot path, but unbounded waits are still forbidden.
 */
class LangfuseRestClient(
    private val client: HttpClient,
    host: String,
    publicKey: String,
    secretKey: String,
    /** Lenient JSON shared by every domain client for body building + response parsing. */
    val json: Json = Json { ignoreUnknownKeys = true },
) {
    private val baseUrl: String = host.trimEnd('/')

    private val authHeader: String =
        "Basic " + Base64.getEncoder().encodeToString("$publicKey:$secretKey".encodeToByteArray())

    /** Authenticated GET returning the raw response body; [query] becomes URL query params. */
    suspend fun getText(path: String, query: Map<String, String> = emptyMap()): String {
        val response = client.get("$baseUrl$path") {
            header(HttpHeaders.Authorization, authHeader)
            query.forEach { (key, value) -> parameter(key, value) }
        }
        require(response.status.isSuccess()) { "Langfuse GET $path -> ${response.status}" }
        return response.bodyAsText()
    }

    /** Authenticated POST of a JSON [body]. */
    suspend fun postJson(path: String, body: JsonObject) = send(HttpMethod.Post, path, body)

    /** Authenticated PATCH of a JSON [body]. */
    suspend fun patchJson(path: String, body: JsonObject) = send(HttpMethod.Patch, path, body)

    private suspend fun send(httpMethod: HttpMethod, path: String, body: JsonObject) {
        val response = client.request("$baseUrl$path") {
            method = httpMethod
            header(HttpHeaders.Authorization, authHeader)
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(JsonObject.serializer(), body))
        }
        require(response.status.isSuccess()) { "Langfuse ${httpMethod.value} $path -> ${response.status}" }
    }
}
