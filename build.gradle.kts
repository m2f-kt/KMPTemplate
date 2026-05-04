plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    id("com.android.application") apply false
    id("com.android.library") apply false
    alias(libs.plugins.composeHotReload) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    // Plugins provided by buildSrc — declared without version to avoid classpath conflict
    id("org.jetbrains.kotlin.jvm") apply false
    id("org.jetbrains.kotlin.plugin.serialization") apply false
    id("org.jetbrains.kotlinx.kover") apply false
    id("io.gitlab.arturbosch.detekt") apply false
    id("org.jetbrains.kotlin.multiplatform") apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
    }

    // Force Kotlin stdlib to match compiler version across all configurations.
    // Arrow 2.2.0 (compiled with Kotlin 2.2.21) pulls in a newer stdlib that
    // causes WASM compilation failures due to compiler/stdlib version mismatch.
    configurations.all {
        resolutionStrategy.eachDependency {
            if (requested.group == "org.jetbrains.kotlin" && requested.name.startsWith("kotlin-stdlib")) {
                useVersion(libs.versions.kotlin.get())
            }
        }
    }
}

// ─────────────────────────────────────────────
// Dev tasks
// ─────────────────────────────────────────────

fun runCommand(vararg args: String): Int {
    val process = ProcessBuilder(*args).redirectErrorStream(true).start()
    process.inputStream.bufferedReader().readLines()
    return process.waitFor()
}

// Read a value from .env (or .env.example as a fallback) so dev tasks stay
// aligned with the runtime configuration. Returns null when the key is absent.
fun envValue(key: String): String? {
    val source = listOf(file(".env"), file(".env.example")).firstOrNull { it.exists() }
        ?: return null
    return source.useLines { lines ->
        lines.map { it.trim() }
            .filterNot { it.isEmpty() || it.startsWith("#") }
            .map { it.split("=", limit = 2) }
            .firstOrNull { it.size == 2 && it[0] == key }
            ?.get(1)
            ?.trim()
    }
}

val serverPort: Int = envValue("PORT")?.toIntOrNull() ?: 8080

tasks.register("checkSetup") {
    group = "dev"
    description = "Check development prerequisites"
    notCompatibleWithConfigurationCache("uses runtime system checks")
    doLast {
        data class Check(val name: String, val passed: Boolean, val fix: String)

        val checks = mutableListOf<Check>()

        // Check Docker running
        val dockerResult = runCommand("docker", "info")
        checks.add(
            Check(
                "Docker running", dockerResult == 0,
                "Install Docker Desktop: https://docs.docker.com/get-docker/"
            )
        )

        // Check Docker Compose available
        val composeResult = runCommand("docker", "compose", "version")
        checks.add(
            Check(
                "Docker Compose available", composeResult == 0,
                "Docker Compose is included with Docker Desktop. Update Docker Desktop if missing."
            )
        )

        // Check JDK version (11+)
        val javaVersion = System.getProperty("java.version").split(".")[0].toIntOrNull() ?: 0
        checks.add(
            Check(
                "JDK 11+ (found: ${System.getProperty("java.version")})", javaVersion >= 11,
                "Install JDK 11+: https://adoptium.net/"
            )
        )

        // Check port availability for each service port
        // Ports used by our own Docker containers or our server are considered OK
        fun isOwnProcess(port: Int): Boolean {
            val process = ProcessBuilder("lsof", "-ti:$port")
                .redirectErrorStream(true).start()
            val pids = process.inputStream.bufferedReader().readLines()
                .mapNotNull { it.trim().toIntOrNull() }
            process.waitFor()
            if (pids.isEmpty()) return false
            return pids.any { pid ->
                val cmdProc = ProcessBuilder("ps", "-p", "$pid", "-o", "command=")
                    .redirectErrorStream(true).start()
                val cmd = cmdProc.inputStream.bufferedReader().readText().trim()
                cmdProc.waitFor()
                cmd.contains("docker") || cmd.contains("com.docker")
                    || cmd.contains("com.m2f.template") || cmd.contains("server:run")
            }
        }

        val ports = mapOf(
            5436 to "PostgreSQL",
            9002 to "MinIO API",
            9003 to "MinIO Console",
            1025 to "MailHog SMTP",
            8025 to "MailHog Web",
            serverPort to "Server"
        )
        for ((port, service) in ports) {
            val available = try {
                java.net.ServerSocket(port).use { true }
            } catch (_: Exception) {
                false
            }
            if (available) {
                checks.add(Check("Port $port ($service) available", true, ""))
            } else if (isOwnProcess(port)) {
                checks.add(Check("Port $port ($service) in use by this project ✓", true, ""))
            } else {
                checks.add(
                    Check(
                        "Port $port ($service) available", false,
                        "Port $port in use by another process. Stop it: lsof -ti:$port | xargs kill"
                    )
                )
            }
        }

        // Print results
        println("\n  Development Environment Check\n")
        checks.forEach { check ->
            val icon = if (check.passed) "✅" else "❌"
            println("  $icon ${check.name}")
            if (!check.passed) println("     Fix: ${check.fix}")
        }
        println()

        val failures = checks.filter { !it.passed }
        if (failures.isNotEmpty()) {
            throw GradleException("${failures.size} prerequisite(s) failed. Fix the issues above and re-run ./gradlew checkSetup")
        }
        println("  All checks passed ✅\n")
    }
}

tasks.register<Exec>("devUp") {
    group = "dev"
    description = "Start all Docker services and wait for healthy"
    commandLine("docker", "compose", "up", "-d", "--wait", "postgres", "minio", "mailhog")
}

tasks.register<Exec>("seedData") {
    group = "dev"
    description = "Seed demo user (dev@example.com / password)"
    commandLine(
        "bash", "-c",
        "docker exec -i template-postgres psql -U postgres -d application < dev-scripts/seed-dev-data.sql"
    )
}

tasks.register("devSetup") {
    group = "dev"
    description = "Full first-time setup: check prerequisites + start Docker services"
    dependsOn("checkSetup")
    finalizedBy("devUp")
}

tasks.register("testAll") {
    group = "dev"
    description = "Run all tests across all modules"
    dependsOn(":server:test")
    dependsOn(":shared:allTests")
}

tasks.register("verifySetup") {
    group = "dev"
    description = "Verify all services are running correctly"
    notCompatibleWithConfigurationCache("uses runtime HTTP checks")
    doLast {
        data class Check(val name: String, val passed: Boolean, val fix: String)

        val checks = mutableListOf<Check>()

        // Check Docker containers
        val containers = mapOf(
            "template-postgres" to "PostgreSQL",
            "template-minio" to "MinIO",
            "template-mailhog" to "MailHog",
        )
        for ((container, service) in containers) {
            val process = ProcessBuilder(
                "docker", "inspect", "--format", "{{.State.Health.Status}}", container
            ).redirectErrorStream(true).start()
            val output = process.inputStream.bufferedReader().readText().trim()
            val exitCode = process.waitFor()
            val healthy = exitCode == 0 && output == "healthy"
            checks.add(
                Check(
                    "$service container ($container)", healthy,
                    "Run: ./gradlew devUp"
                )
            )
        }

        // Check server /health endpoint
        try {
            val url = java.net.URI("http://localhost:$serverPort/health").toURL()
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.connectTimeout = 3000
            connection.readTimeout = 3000
            connection.requestMethod = "GET"
            val responseCode = connection.responseCode
            val body = connection.inputStream.bufferedReader().readText()
            connection.disconnect()

            checks.add(
                Check(
                    "Server /health endpoint", responseCode == 200,
                    "Start the server: ./gradlew :server:run"
                )
            )

            if (responseCode == 200 || responseCode == 503) {
                println("\n  Server Health Response:\n  $body\n")
            }
        } catch (_: Exception) {
            checks.add(
                Check(
                    "Server /health endpoint", false,
                    "Start the server: ./gradlew :server:run"
                )
            )
        }

        // Print results
        println("\n  Setup Verification\n")
        checks.forEach { check ->
            val icon = if (check.passed) "✅" else "❌"
            println("  $icon ${check.name}")
            if (!check.passed) println("     Fix: ${check.fix}")
        }
        println()

        val failures = checks.filter { !it.passed }
        if (failures.isNotEmpty()) {
            throw GradleException("${failures.size} service(s) not healthy. Fix the issues above.")
        }
        println("  All services verified ✅\n")
    }
}

tasks.register("installGitHooks") {
    group = "dev"
    description = "Install git hooks for code quality checks (opt-in)"
    doLast {
        val hooksDir = file(".git/hooks")
        if (!hooksDir.exists()) {
            throw GradleException(".git/hooks directory not found. Is this a git repository?")
        }

        // Pre-commit: run detekt
        val preCommit = file(".git/hooks/pre-commit")
        preCommit.writeText(
            """
            |#!/usr/bin/env bash
            |set -euo pipefail
            |echo "Running detekt..."
            |./gradlew detekt --no-daemon
            |echo "Detekt passed ✅"
            """.trimMargin()
        )
        preCommit.setExecutable(true)

        // Pre-push: run all tests
        val prePush = file(".git/hooks/pre-push")
        prePush.writeText(
            """
            |#!/usr/bin/env bash
            |set -euo pipefail
            |echo "Running tests..."
            |./gradlew testAll --no-daemon
            |echo "All tests passed ✅"
            """.trimMargin()
        )
        prePush.setExecutable(true)

        println("\n  Git hooks installed ✅")
        println("  - pre-commit: runs detekt")
        println("  - pre-push: runs testAll\n")
    }
}