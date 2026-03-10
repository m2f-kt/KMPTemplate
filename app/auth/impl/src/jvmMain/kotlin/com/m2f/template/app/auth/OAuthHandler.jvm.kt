package com.m2f.template.app.auth

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.awt.Desktop
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.URI

private const val LOCALHOST_PORT = 9876

/**
 * JVM Desktop OAuth handler.
 * Opens the OAuth URL in the system browser and starts a temporary localhost
 * server to receive the callback with JWT tokens.
 *
 * The localhost server listens for exactly one request, extracts the query
 * parameters, publishes them via [oauthResult], then shuts down.
 */
actual class OAuthHandler actual constructor(private val serverBaseUrl: String) {

    private val _oauthResult = MutableStateFlow<Pair<String, String>?>(null)

    /** Observable flow of (accessToken, refreshToken) pairs received from OAuth callback. */
    val oauthResult: StateFlow<Pair<String, String>?> = _oauthResult

    actual fun startOAuth(provider: String) {
        val redirectUri = getRedirectUri()
        // Start localhost callback server BEFORE opening the browser
        startCallbackServer()
        val url = "$serverBaseUrl/api/auth/oauth/$provider?state=$redirectUri"
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(URI(url))
        }
    }

    actual fun getRedirectUri(): String = "http://localhost:$LOCALHOST_PORT/auth/callback"

    /**
     * Starts a temporary server socket that accepts one HTTP request,
     * extracts access_token and refresh_token from query parameters,
     * responds with a simple HTML page, and shuts down.
     */
    private fun startCallbackServer() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                ServerSocket(LOCALHOST_PORT).use { serverSocket ->
                    val socket = serverSocket.accept()
                    val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                    val requestLine = reader.readLine() ?: return@launch
                    // Parse GET /auth/callback?access_token=...&refresh_token=... HTTP/1.1
                    val queryString = requestLine
                        .substringAfter("?", "")
                        .substringBefore(" HTTP")
                    val params = queryString.split("&")
                        .filter { it.contains("=") }
                        .associate { param ->
                            val (key, value) = param.split("=", limit = 2)
                            key to value
                        }

                    val accessToken = params["access_token"]
                    val refreshToken = params["refresh_token"]

                    // Send a simple HTML response
                    val html = """
                        <html><body>
                        <h2>Authentication successful</h2>
                        <p>You can close this window and return to the application.</p>
                        </body></html>
                    """.trimIndent()
                    val response = "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: text/html\r\n" +
                        "Content-Length: ${html.toByteArray().size}\r\n" +
                        "Connection: close\r\n\r\n" +
                        html

                    socket.getOutputStream().write(response.toByteArray())
                    socket.getOutputStream().flush()
                    socket.close()

                    if (accessToken != null && refreshToken != null) {
                        _oauthResult.value = Pair(accessToken, refreshToken)
                    }
                }
            } catch (_: Exception) {
                // Server socket error -- user may need to retry
            }
        }
    }
}
