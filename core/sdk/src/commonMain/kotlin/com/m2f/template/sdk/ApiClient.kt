package com.m2f.template.sdk

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.resources.Resources
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
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
 * - [HttpTimeout] applies finite connect/request caps so a stalled transport or a half-open
 *   connection fails fast instead of hanging indefinitely.
 * - [WebSockets] keepalive pings hold the connection open across NAT idle timeouts.
 * - [requireSecureBaseUrl] refuses a cleartext `http://`/`ws://` baseUrl to a non-loopback host so
 *   a staging/prod misconfiguration fails fast at construction.
 * - The [AuthInterceptor] is installed after client creation to attach bearer tokens and handle
 *   401 refresh+retry with Mutex-based concurrency protection.
 *
 * @param authInterceptor The interceptor that attaches bearer tokens and handles 401 refresh+retry.
 * @param baseUrl The base URL for all API requests (defaults to [defaultBaseUrl]).
 * @param localeProvider Lambda returning current locale tag (e.g., "en", "es") for Accept-Language header.
 * @param connectTimeoutMillis TCP/TLS connect-phase cap (defaults to [CONNECT_TIMEOUT_MS]).
 * @param requestTimeoutMillis Full request cap (defaults to [REQUEST_TIMEOUT_MS]).
 */
fun createApiClient(
    authInterceptor: AuthInterceptor,
    baseUrl: String = defaultBaseUrl(),
    localeProvider: () -> String = { "en" },
    connectTimeoutMillis: Long = CONNECT_TIMEOUT_MS,
    requestTimeoutMillis: Long = REQUEST_TIMEOUT_MS,
): HttpClient {
    // SDK-TRANSPORT-01: refuse a cleartext baseUrl to a non-loopback host (fail fast on a prod misconfig).
    requireSecureBaseUrl(baseUrl)
    val client = HttpClient(platformEngine()) {
        expectSuccess = false

        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                },
            )
        }

        install(Resources)

        install(HttpTimeout) {
            this.connectTimeoutMillis = connectTimeoutMillis
            this.requestTimeoutMillis = requestTimeoutMillis
        }

        install(WebSockets) {
            // Keepalive ping/pong keeps the connection alive across NAT idle timeouts.
            pingIntervalMillis = WS_PING_INTERVAL_MS
        }

        install(Logging) {
            level = LogLevel.HEADERS
        }

        defaultRequest {
            url(baseUrl)
            contentType(ContentType.Application.Json)
            header(HttpHeaders.AcceptLanguage, localeProvider())
        }
    }
    authInterceptor.install(client)
    return client
}

/** WebSocket keepalive interval — 20 s, mirrored on the server. */
private const val WS_PING_INTERVAL_MS: Long = 20_000L

/** SDK-TRANSPORT-02: TCP/TLS connect-phase cap (10 s). Bounds half-open/SYN-blackhole hangs. */
const val CONNECT_TIMEOUT_MS: Long = 10_000L

/** Full request cap (30 s). Finite by design — a dead/slow server fails fast. */
const val REQUEST_TIMEOUT_MS: Long = 30_000L

/** Loopback / Android-emulator hosts where dev cleartext http/ws is acceptable. */
private val LOOPBACK_HOSTS = setOf("localhost", "127.0.0.1", "::1", "10.0.2.2", "10.0.3.2")

/**
 * SDK-TRANSPORT-01: cleartext `http://`/`ws://` is allowed ONLY to a loopback/emulator host (dev).
 * A cleartext baseUrl to any real host is refused at construction so a staging/prod misconfiguration
 * fails fast instead of silently sending plaintext — mirrors the server's "HTTPS only, refuse http://"
 * rule.
 */
fun requireSecureBaseUrl(baseUrl: String) {
    val lower = baseUrl.lowercase()
    val isCleartext = lower.startsWith("http://") || lower.startsWith("ws://")
    if (!isCleartext) return
    val host = lower.substringAfter("://").substringBefore("/").substringBefore(":")
    require(host in LOOPBACK_HOSTS) {
        "Refusing cleartext baseUrl to non-loopback host '$host' — use https/wss in non-dev builds " +
            "(SDK-TRANSPORT-01)."
    }
}
