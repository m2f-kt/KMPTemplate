package com.m2f.template.designsystem.accessibility

import io.kotest.matchers.shouldBe
import kotlin.test.Test

/**
 * Guards the JVM [prefersReducedMotion] bridge: on a non-macOS host (CI is Linux) it must short-
 * circuit to `false` without ever loading the AppKit JNA library or throwing. On a real Mac the
 * value is whatever the OS reports, so we only assert the non-macOS contract.
 */
class ReducedMotionTest {

    @Test
    fun `prefersReducedMotion is false on non-macOS hosts and never throws`() {
        val isMac = System.getProperty("os.name") == "Mac OS X"
        if (isMac) return
        prefersReducedMotion() shouldBe false
    }
}
