package com.m2f.template.desktop

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import java.nio.file.Files
import kotlin.test.Test

/**
 * Unit coverage for [SingleInstanceGuard]. The guard is the structural defense against a second
 * instance starting; if a refactor inverted the lock check (e.g. returned `true` when the lock is
 * already held), the second instance would start silently — this test fails first.
 */
class SingleInstanceGuardTest {

    @Test
    fun `first acquire wins, a second on the same lock file is refused`() {
        val lockFile = Files.createTempFile("template-single-instance", ".lock").toFile()
        lockFile.deleteOnExit()

        // First caller holds the lock → it is the sole instance.
        SingleInstanceGuard.acquire(lockFile).shouldBeTrue()
        // A second attempt on the same lock cannot acquire it → refused.
        SingleInstanceGuard.acquire(lockFile).shouldBeFalse()
    }
}
