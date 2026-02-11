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
 *   This is required for the auth interceptor (Plan 03-03) to see 401 responses and attempt token refresh.
 * - ContentNegotiation with kotlinx-serialization handles JSON serialization/deserialization.
 * - The [tokenProvider] parameter is reserved for future use by the AuthInterceptor (Plan 03-03).
 *
 * @param baseUrl The base URL for all API requests (e.g., "http://localhost:8080").
 * @param tokenProvider Optional lambda that returns the current access token. Unused in this plan;
 *        will be wired by the AuthInterceptor in Plan 03-03.
 */
fun createApiClient(
    baseUrl: String = "http://localhost:8080",
    tokenProvider: (() -> String?)? = null,
): HttpClient {
    return HttpClient(platformEngine()) {
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
}
