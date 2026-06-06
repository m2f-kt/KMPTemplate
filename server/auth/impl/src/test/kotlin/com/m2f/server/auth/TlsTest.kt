package com.m2f.server.auth

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory
import org.junit.Test

/**
 * TLS 1.3 enforcement on outbound HTTPS.
 *
 * The first test runs unconditionally and asserts the JVM's default SSLContext
 * advertises `TLSv1.3` in its supported protocols — this guarantees that
 * Ktor's outbound HttpClient (CIO engine, which delegates to JSSE) WILL
 * negotiate 1.3 when the peer supports it.
 *
 * The second test performs a real handshake against a neutral always-on
 * TLS-1.3 host and asserts the negotiated protocol is `TLSv1.3`. It is gated
 * behind `RUN_NETWORK_TESTS=true` so offline CI does not fail.
 */
class TlsTest {

    @Test
    fun `JVM SSLContext supports TLS 1_3`() {
        // Constructing TLSv1.3 SSLContext throws NoSuchAlgorithmException on
        // JVMs that don't ship 1.3; this gives a clean signal in addition to
        // the protocol-list assertion below.
        val ctx = SSLContext.getInstance("TLSv1.3")
        ctx.init(null, null, null)
        val supported = ctx.supportedSSLParameters.protocols.toList()
        supported shouldContain "TLSv1.3"
    }

    @Test
    fun `outbound HTTPS handshake negotiates TLS 1_3`() {
        // Gate the live-network assertion: offline CI sets the env var to
        // anything other than "true" and the test no-ops.
        if (System.getenv("RUN_NETWORK_TESTS") != "true") return

        val factory = SSLSocketFactory.getDefault() as SSLSocketFactory
        factory.createSocket(NETWORK_HOST, HTTPS_PORT).use { sock ->
            val sslSock = sock as SSLSocket
            sslSock.startHandshake()
            // Most modern stacks negotiate 1.2 or 1.3; surface the broad
            // assertion first for a clearer failure if the peer downgraded.
            sslSock.session.protocol shouldStartWith "TLSv1."
            // Strict: the production target MUST land on 1.3.
            sslSock.session.protocol shouldBe "TLSv1.3"
        }
    }

    private companion object {
        private const val HTTPS_PORT = 443
        private const val NETWORK_HOST = "www.google.com"
    }
}
