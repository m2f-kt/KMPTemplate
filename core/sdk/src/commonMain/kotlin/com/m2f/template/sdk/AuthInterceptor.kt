package com.m2f.template.sdk

import com.m2f.template.models.dto.AuthResponse
import com.m2f.template.models.dto.RefreshTokenRequest
import com.m2f.template.models.routes.Auth
import com.m2f.template.storage.TokenStorage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.plugin
import io.ktor.client.plugins.resources.post
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.resources.href
import io.ktor.resources.serialization.ResourcesFormat
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Intercepts HTTP requests to attach bearer tokens and handle 401 refresh+retry.
 *
 * Design:
 * - Attaches the current access token as a Bearer header to all requests except refresh.
 * - On 401 responses, attempts a single token refresh (coordinated via [Mutex]).
 * - Concurrent 401 responses share a single refresh call (double-check pattern).
 * - On refresh failure, clears local tokens (session is dead).
 */
class AuthInterceptor(
    private val tokenStorage: TokenStorage,
) {
    private val refreshMutex = Mutex()
    private val _sessionExpired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    /** Emits when the refresh token fails and local tokens are cleared (session is dead). */
    val sessionExpired: SharedFlow<Unit> = _sessionExpired.asSharedFlow()

    /** Path derived from Auth.Refresh resource -- stays in sync with ApiRoutes.kt */
    private val refreshPath = href(ResourcesFormat(), Auth.Refresh())

    fun install(client: HttpClient) {
        client.plugin(HttpSend).intercept { request ->
            // Skip token attachment for refresh endpoint (avoid recursion)
            val isRefreshRequest = request.url.buildString().contains(refreshPath)

            if (!isRefreshRequest) {
                val accessToken = tokenStorage.getAccessToken()
                if (accessToken != null) {
                    request.bearerAuth(accessToken)
                }
            }

            val originalCall = execute(request)

            // If 401 and not a refresh request, try refreshing
            if (originalCall.response.status == HttpStatusCode.Unauthorized && !isRefreshRequest) {
                val refreshToken = tokenStorage.getRefreshToken()
                    ?: return@intercept originalCall

                val originalAccessToken = tokenStorage.getAccessToken()

                val newTokens = refreshMutex.withLock {
                    // Double-check: another coroutine may have already refreshed
                    val currentToken = tokenStorage.getAccessToken()
                    if (currentToken != originalAccessToken && currentToken != null) {
                        // Already refreshed by another coroutine, use new token
                        null
                    } else {
                        // Actually refresh
                        try {
                            val response = client.post(Auth.Refresh()) {
                                contentType(ContentType.Application.Json)
                                setBody(RefreshTokenRequest(refreshToken))
                            }
                            if (response.status.isSuccess()) {
                                val authResponse = response.body<AuthResponse>()
                                tokenStorage.saveTokens(
                                    authResponse.accessToken,
                                    authResponse.refreshToken,
                                )
                                authResponse
                            } else {
                                // Refresh failed -- clear tokens (session expired)
                                tokenStorage.clearTokens()
                                _sessionExpired.tryEmit(Unit)
                                null
                            }
                        } catch (_: Exception) {
                            tokenStorage.clearTokens()
                            _sessionExpired.tryEmit(Unit)
                            null
                        }
                    }
                }

                if (newTokens != null) {
                    // Retry with new token
                    request.bearerAuth(newTokens.accessToken)
                    execute(request)
                } else {
                    // Check if another coroutine refreshed
                    val updatedToken = tokenStorage.getAccessToken()
                    if (updatedToken != null && updatedToken != originalAccessToken) {
                        request.bearerAuth(updatedToken)
                        execute(request)
                    } else {
                        originalCall // Give up, return 401
                    }
                }
            } else {
                originalCall
            }
        }
    }
}
