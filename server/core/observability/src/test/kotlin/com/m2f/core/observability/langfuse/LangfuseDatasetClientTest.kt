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
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test

/** Wire-shape contract for the Langfuse dataset client (datasets + dataset-items endpoints). */
class LangfuseDatasetClientTest {

    private class Capture {
        var method: HttpMethod? = null
        var path: String? = null
        var body: String = ""
    }

    private fun clientFor(capture: Capture): LangfuseDatasetClient {
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
        return LangfuseDatasetClient(LangfuseRestClient(http, "https://lf.test", "pk", "sk"))
    }

    @Test
    fun `createDataset POSTs the dataset name`() = runTest {
        val capture = Capture()
        clientFor(capture).createDataset(name = "regressions", description = "from prod failures")

        capture.method shouldBe HttpMethod.Post
        capture.path shouldBe "/api/public/v2/datasets"
        capture.body shouldContain "regressions"
        capture.body shouldContain "from prod failures"
    }

    @Test
    fun `addItem POSTs datasetName + input to the dataset-items endpoint`() = runTest {
        val capture = Capture()
        clientFor(capture).addItem(
            datasetName = "regressions",
            input = buildJsonObject { put("raw", JsonPrimitive("the failing input")) },
        )

        capture.path shouldBe "/api/public/dataset-items"
        capture.body shouldContain "regressions"
        capture.body shouldContain "the failing input"
    }
}
