package com.m2f.template.sdk

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Creates the shared HttpClient used by all SDK API functions.
 *
 * Key design decisions:
 * - [expectSuccess] is set to `false` so that HTTP 4xx/5xx responses are NOT thrown as exceptions.
 *   This allows the [AuthInterceptor] to see 401 responses and attempt token refresh.
 * - ContentNegotiation with kotlinx-serialization handles JSON serialization/deserialization.
 * - The [AuthInterceptor] is installed after client creation to attach bearer tokens and handle
 *   401 refresh+retry with Mutex-based concurrency protection.
 *
 * @param authInterceptor The interceptor that attaches bearer tokens and handles 401 refresh+retry.
 * @param baseUrl The base URL for all API requests (e.g., "http://localhost:8080").
 */
fun createApiClient(
    authInterceptor: AuthInterceptor,
    baseUrl: String = "http://localhost:8080",
): HttpClient {
    val client = HttpClient(platformEngine()) {
        expectSuccess = false

        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }

        install(Logging) {
            level = LogLevel.HEADERS
        }

        defaultRequest {
            url(baseUrl)
            contentType(ContentType.Application.Json)
        }
    }
    authInterceptor.install(client)
    return client
}
