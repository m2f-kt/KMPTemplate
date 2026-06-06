package com.m2f.template.platform

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.shouldBe
import kotlin.test.Test

/**
 * Guards the pure JVM platform helpers. [isMacOs] must return a [Boolean] without throwing on any
 * host (CI is Linux); off macOS it must agree with [isMacOsHost] and be `false`. We never load the
 * JNA bridge here — [isMacOs] reads `os.name` only.
 */
class PlatformTest {

    @Test
    fun `isMacOs returns a Boolean and matches isMacOsHost without throwing`() {
        isMacOs() shouldBe isMacOsHost
    }

    @Test
    fun `isMacOs is false on non-macOS hosts`() {
        if (isMacOsHost) return
        isMacOs().shouldBeFalse()
    }
}
