package com.m2f.template.sdk

import com.m2f.template.models.AppError
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class MapHttpErrorTest {

    private fun clientReturning(status: HttpStatusCode, body: String): HttpClient {
        val engine = MockEngine {
            respond(
                content = body,
                status = status,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        return HttpClient(engine)
    }

    @Test
    fun `400 with a non ErrorResponse body preserves the raw body in Client Unknown`() = runTest {
        val rawBody = """{"error":{"message":"invalid_request","type":"bad"}}"""
        val client = clientReturning(HttpStatusCode.BadRequest, rawBody)
        val response: HttpResponse = client.get("https://api.example.com/x")

        val error = mapHttpError(response)

        val unknown = error.shouldBeInstanceOf<AppError.Client.Unknown>()
        unknown.detail.shouldContainNotNull("invalid_request")
    }

    @Test
    fun `400 with a matching ErrorResponse body still falls back via status mapping`() = runTest {
        val rawBody = """{"code":"SOME_CODE","message":"bad input"}"""
        val client = clientReturning(HttpStatusCode.BadRequest, rawBody)
        val response: HttpResponse = client.get("https://api.example.com/x")

        val error = mapHttpError(response)

        // 400 is not in the explicit status table, so it lands in the Unknown fallback,
        // but the parsed message is preferred over the raw body.
        val unknown = error.shouldBeInstanceOf<AppError.Client.Unknown>()
        unknown.detail shouldBe "bad input"
    }

    @Test
    fun `domainCodeMapper hook is consulted before the status fallback`() = runTest {
        val rawBody = """{"code":"AI_PROVIDER_UNAVAILABLE","message":"down"}"""
        val client = clientReturning(HttpStatusCode.BadRequest, rawBody)
        val response: HttpResponse = client.get("https://api.example.com/x")

        val error = mapHttpError(response) { body ->
            if (body.code == "AI_PROVIDER_UNAVAILABLE") {
                AppError.AI.ProviderUnavailable(message = body.message)
            } else {
                null
            }
        }

        val mapped = error.shouldBeInstanceOf<AppError.AI.ProviderUnavailable>()
        mapped.message shouldBe "down"
    }

    private fun String?.shouldContainNotNull(substring: String) {
        (this ?: "").shouldContain(substring)
    }
}
