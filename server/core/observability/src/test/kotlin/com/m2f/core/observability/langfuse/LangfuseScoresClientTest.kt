package com.m2f.core.observability.langfuse

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

/** Wire-shape contract for the Langfuse scores client (`POST /api/public/scores`). */
class LangfuseScoresClientTest {

    private class Capture {
        var method: HttpMethod? = null
        var path: String? = null
        var body: String = ""
    }

    private fun clientFor(capture: Capture): LangfuseScoresClient {
        val engine = MockEngine { request ->
            capture.method = request.method
            capture.path = request.url.encodedPath
            capture.body = (request.body as? TextContent)?.text.orEmpty()
            respond(
                content = "{}",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val http = HttpClient(engine) { install(ContentNegotiation) { json() } }
        return LangfuseScoresClient(LangfuseRestClient(http, "https://lf.test", "pk", "sk"))
    }

    @Test
    fun `postNumericScore posts traceId, name, numeric value and comment`() = runTest {
        val capture = Capture()
        clientFor(capture).postNumericScore(
            traceId = "trace-1",
            name = "faithfulness",
            value = 0.92,
            comment = "no added facts",
        )

        capture.method shouldBe HttpMethod.Post
        capture.path shouldBe "/api/public/scores"
        capture.body shouldContain "\"traceId\":\"trace-1\""
        capture.body shouldContain "\"name\":\"faithfulness\""
        capture.body shouldContain "0.92"
        capture.body shouldContain "NUMERIC"
        capture.body shouldContain "no added facts"
    }

    @Test
    fun `postCategoricalScore posts a categorical value`() = runTest {
        val capture = Capture()
        clientFor(capture).postCategoricalScore(traceId = "t", name = "human_label", value = "hallucination")

        capture.body shouldContain "CATEGORICAL"
        capture.body shouldContain "hallucination"
    }
}
