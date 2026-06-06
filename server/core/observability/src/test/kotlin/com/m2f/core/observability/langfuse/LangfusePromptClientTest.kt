package com.m2f.core.observability.langfuse

import io.kotest.assertions.throwables.shouldThrowAny
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
import java.util.Base64
import kotlin.test.Test

/**
 * Wire-shape + parsing contract for the Langfuse prompt REST client, against a [MockEngine] (no real
 * network). Asserts the exact method/path/auth/label/body Langfuse expects so a future refactor that
 * breaks the contract fails here, and that a chat-array prompt (not a text prompt) is rejected.
 */
class LangfusePromptClientTest {

    private class Capture {
        var method: HttpMethod? = null
        var path: String? = null
        var auth: String? = null
        var label: String? = null
        var body: String = ""
    }

    private val expectedAuth = "Basic " + Base64.getEncoder().encodeToString("pk:sk".encodeToByteArray())

    private fun clientFor(capture: Capture, status: HttpStatusCode, responseBody: String): LangfusePromptClient {
        val engine = MockEngine { request ->
            capture.method = request.method
            capture.path = request.url.encodedPath
            capture.auth = request.headers[HttpHeaders.Authorization]
            capture.label = request.url.parameters["label"]
            capture.body = (request.body as? TextContent)?.text.orEmpty()
            respond(
                content = responseBody,
                status = status,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val http = HttpClient(engine) { install(ContentNegotiation) { json() } }
        return LangfusePromptClient(
            LangfuseRestClient(client = http, host = "https://lf.test", publicKey = "pk", secretKey = "sk"),
        )
    }

    @Test
    fun `fetchPrompt sends auth + label and parses a text prompt with config`() = runTest {
        val capture = Capture()
        val responseBody = """
            {"name":"example","version":3,"type":"text","prompt":"SYS",
             "config":{"model":"gpt-4o-mini","temperature":0.0}}
        """.trimIndent()
        val client = clientFor(capture = capture, status = HttpStatusCode.OK, responseBody = responseBody)

        val spec = client.fetchPrompt(name = "example", label = "production")

        spec.version shouldBe 3
        spec.system shouldBe "SYS"
        spec.config?.model shouldBe "gpt-4o-mini"
        spec.config?.temperature shouldBe 0.0
        capture.method shouldBe HttpMethod.Get
        capture.path shouldBe "/api/public/v2/prompts/example"
        capture.label shouldBe "production"
        capture.auth shouldBe expectedAuth
    }

    @Test
    fun `fetchPrompt throws on a non-2xx so the caller can fall back`() = runTest {
        val client = clientFor(Capture(), HttpStatusCode.NotFound, "not found")
        shouldThrowAny { client.fetchPrompt(name = "example", label = "production") }
    }

    @Test
    fun `createPrompt POSTs name + prompt + labels`() = runTest {
        val capture = Capture()
        val client = clientFor(capture, HttpStatusCode.OK, "{}")

        client.createPrompt(name = "example", system = "SYS BODY", labels = listOf("production"))

        capture.method shouldBe HttpMethod.Post
        capture.path shouldBe "/api/public/v2/prompts"
        capture.body shouldContain "\"name\":\"example\""
        capture.body shouldContain "SYS BODY"
        capture.body shouldContain "production"
    }

    @Test
    fun `setLabel PATCHes the version label endpoint`() = runTest {
        val capture = Capture()
        val client = clientFor(capture, HttpStatusCode.OK, "{}")

        client.setLabel(name = "example", version = 3, labels = listOf("production"))

        capture.method shouldBe HttpMethod.Patch
        capture.path shouldBe "/api/public/v2/prompts/example/versions/3"
        capture.body shouldContain "newLabels"
        capture.body shouldContain "production"
    }

    @Test
    fun `parsePrompt rejects a chat-array prompt`() {
        val client = clientFor(Capture(), HttpStatusCode.OK, "{}")
        shouldThrowAny {
            client.parsePrompt("""{"name":"x","version":1,"prompt":[{"role":"system","content":"a"}]}""")
        }
    }
}
