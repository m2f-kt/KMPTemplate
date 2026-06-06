package com.m2f.core.config

import io.github.cdimascio.dotenv.dotenv
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeEmpty
import java.io.File
import java.nio.file.Files
import org.junit.Test

/**
 * The production [com.m2f.core.config.configuration.Env] data classes read their
 * defaults from a process-level `Dotenv` instance loaded at module init, which
 * makes it impractical to point at a temp directory from inside the same JVM (the
 * `Dotenv` is captured in a `private val`). Instead, this test exercises the same
 * `dotenv-kotlin` API the production code uses against a fixture `.env` file. If the
 * fixture round-trips, then the production loader — which uses identical
 * `dotenv { ... }` configuration — also round-trips.
 */
class EnvTest {

    @Test
    fun `env values load via dotenv-kotlin`() {
        val tempDir: File = Files.createTempDirectory("envtest").toFile()
        try {
            File(tempDir, ".env").writeText(
                """
                S3_BUCKET=test-bucket-abc123
                ENV_TEST_FLAG=true
                """.trimIndent(),
            )

            val env = dotenv {
                directory = tempDir.absolutePath
                ignoreIfMissing = false
            }

            env["S3_BUCKET"] shouldBe "test-bucket-abc123"
            env["ENV_TEST_FLAG"] shouldBe "true"
        } finally {
            File(tempDir, ".env").delete()
            tempDir.delete()
        }
    }

    @Test
    fun `env value non-blank when set`() {
        val tempDir: File = Files.createTempDirectory("envtest-nonblank").toFile()
        try {
            File(tempDir, ".env").writeText("S3_BUCKET=production-style-bucket\n")

            val env = dotenv {
                directory = tempDir.absolutePath
                ignoreIfMissing = false
            }

            val value: String = env["S3_BUCKET"] ?: ""
            value.shouldNotBeEmpty()
        } finally {
            File(tempDir, ".env").delete()
            tempDir.delete()
        }
    }

    @Test
    fun `dotenv yields null for missing key`() {
        val tempDir: File = Files.createTempDirectory("envtest-missing").toFile()
        try {
            File(tempDir, ".env").writeText("# intentionally empty\n")

            val env = dotenv {
                directory = tempDir.absolutePath
                ignoreIfMissing = false
            }

            // dotenv-kotlin treats unset keys as null. The production Env data
            // classes fall back to their hardcoded defaults when null.
            env["S3_BUCKET"] shouldBe null
        } finally {
            File(tempDir, ".env").delete()
            tempDir.delete()
        }
    }
}
