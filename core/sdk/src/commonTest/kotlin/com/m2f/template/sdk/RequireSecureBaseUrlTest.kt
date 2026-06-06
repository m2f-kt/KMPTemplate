package com.m2f.template.sdk

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import kotlin.test.Test

class RequireSecureBaseUrlTest {

    @Test
    fun `cleartext http to a non-loopback host is refused`() {
        val error = shouldThrow<IllegalArgumentException> {
            requireSecureBaseUrl("http://example.com")
        }
        error.message shouldContain "example.com"
    }

    @Test
    fun `cleartext http to localhost is allowed`() {
        // Does not throw.
        requireSecureBaseUrl("http://localhost:8080")
    }

    @Test
    fun `cleartext http to the android emulator loopback is allowed`() {
        // Does not throw.
        requireSecureBaseUrl("http://10.0.2.2")
    }

    @Test
    fun `https to any host is allowed`() {
        // Does not throw — non-cleartext schemes are never refused.
        requireSecureBaseUrl("https://example.com")
    }

    @Test
    fun `finite transport timeouts are exposed with sane defaults`() {
        CONNECT_TIMEOUT_MS shouldNotBe 0L
        REQUEST_TIMEOUT_MS shouldNotBe 0L
    }
}
