# Testing Patterns

**Analysis Date:** 2026-02-10

## Test Framework

**Runner:**
- Kotlin test framework via `kotlin-test` library
- Version: Kotlin 2.2.10
- Config: Gradle built-in test execution
- Also available: JUnit 4 via `kotlin-test-junit`

**Assertion Library:**
- Kotlin test assertions: `kotlin.test.assertEquals`, `kotlin.test.assertNotNull`, etc.
- Kotest assertions library: `io.kotest:kotest-assertions-core` 6.0.1
- Arrow assertions: `io.kotest:kotest-assertions-arrow` for functional programming patterns
- Arrow FX Coroutines assertions: `io.kotest:kotest-assertions-arrow-fx-coroutines`

**Run Commands:**
```bash
# Run all tests
gradle test

# Run tests for specific module
gradle :server:core:config:test
gradle :server:core:database:test

# Watch mode
gradle test --continuous

# With coverage
gradle koverHtmlReport    # Generate Kover coverage report

# Test specific class
gradle test --tests ClassName
```

## Test File Organization

**Location:**
- **Multiplatform modules**: Co-located in `src/{platform}Test/kotlin/{package}/` directory
  - Example: `shared/src/commonTest/kotlin/com/m2f/template/SharedCommonTest.kt`
  - Example: `composeApp/src/commonTest/kotlin/com/m2f/template/ComposeAppCommonTest.kt`

- **Server modules**: Standard Gradle location
  - Expected: `src/test/kotlin/{package}/` for each module
  - Example structure: `server/core/database/src/test/kotlin/com/m2f/core/database/`
  - Example structure: `server/core/config/src/test/kotlin/com/m2f/core/config/`

**Naming:**
- Suffix `Test` on class names: `SharedCommonTest`, `ComposeAppCommonTest`
- Pattern: `{Class}Test` for unit tests
- Test files parallel package structure to source files

**Structure:**
```
src/
├── commonTest/kotlin/com/m2f/template/
│   ├── SharedCommonTest.kt
│   └── ComposeAppCommonTest.kt
└── test/kotlin/com/m2f/core/{module}/
    ├── {Feature}Test.kt
    └── {Service}Test.kt
```

## Test Structure

**Suite Organization:**
```kotlin
package com.m2f.template

import kotlin.test.Test
import kotlin.test.assertEquals

class SharedCommonTest {

    @Test
    fun example() {
        assertEquals(3, 1 + 2)
    }
}
```

**Patterns:**
- Single test class per source file (observed)
- Test methods annotated with `@Test` from `kotlin.test`
- Simple assertion structure: `assertEquals(expected, actual)`
- No test fixtures or data providers yet (template project stage)

**Expected Advanced Patterns** (based on dependencies):
```kotlin
// Kotest assertions style (available but not yet used)
import io.kotest.assertions.arrow.fx.eventually
import io.kotest.matchers.shouldBe

class FeatureTest {
    @Test
    fun `description should use backticks`() {
        val result = functionUnderTest()
        result shouldBe expectedValue
    }
}
```

## Mocking

**Framework:**
- Not explicitly configured yet
- Testcontainers for integration testing: `org.testcontainers:testcontainers` 1.21.3
- Testcontainers PostgreSQL: `org.testcontainers:postgresql` 1.21.3

**Patterns:**
- Expected: Use Testcontainers for real PostgreSQL instance in integration tests
- Real database strategy: spin up containerized PostgreSQL for IT tests

**Expected Mocking Structure:**
```kotlin
// Integration test with Testcontainers
class DatabaseIntegrationTest {
    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            postgres = PostgreSQLContainer<Nothing>("postgres:latest")
                .withDatabaseName("test")
                .withUsername("test")
                .withPassword("test")
            postgres.start()
        }
    }
}
```

**What to Mock:**
- External HTTP services (when not under test)
- Third-party APIs requiring credentials
- Heavy I/O operations in unit tests

**What NOT to Mock:**
- Database interactions in integration tests (use Testcontainers)
- Framework code (Ktor routing, authentication)
- Local business logic
- Error handling paths (test the actual error types)

## Fixtures and Factories

**Test Data:**
- Not yet implemented in this template
- Expected location: `src/test/kotlin/com/m2f/core/{module}/fixtures/`
- Would follow data class factory pattern:

```kotlin
// Example fixture pattern to follow
object UserFixtures {
    fun validUser() = User(
        id = 1,
        email = "test@example.com",
        name = "Test User"
    )

    fun invalidUser() = User(
        id = -1,
        email = "invalid",
        name = ""
    )
}
```

**Location:**
- Shared fixtures per module in `fixtures/` subdirectory
- Test utilities in `utils/` subdirectory
- Domain object builders/factories in same package as tests

## Coverage

**Requirements:**
- Kover plugin configured for coverage analysis: `org.jetbrains.kotlinx.kover` 0.9.1
- Coverage enforcement: Not enforced in current config
- Target: Not specified

**View Coverage:**
```bash
# Generate HTML coverage report
gradle koverHtmlReport

# View coverage report
open build/reports/kover/html/index.html

# Get coverage summary
gradle koverReport
```

**Coverage Configuration:**
- Kover applied to all subprojects in root `build.gradle.kts`
- Per-module coverage reports generated independently

## Test Types

**Unit Tests:**
- Scope: Single class/function in isolation
- Approach: Kotlin test + Kotest assertions (when needed)
- Location: `src/test/kotlin/` or `src/commonTest/kotlin/`
- Example: Testing validation logic in `InvalidField.kt`
- Would test functions like `String.notBlank()`, `String.minSize()`, `String.maxSize()`

**Integration Tests:**
- Scope: Multiple components interacting with real database
- Approach: Testcontainers PostgreSQL + Ktor test host
- Framework: `ktor-server-test-host` for testing routes/handlers
- Expected: Database migrations + API endpoints
- Example location: `server/core/database/src/test/kotlin/com/m2f/core/database/`

**E2E Tests:**
- Framework: Not configured
- Status: Not applicable for server-only module at this stage

## Server-Specific Testing

**Ktor Test Host:**
- Dependency: `ktor-server-test-host` 3.3.0
- Pattern for route testing:

```kotlin
@Test
fun `GET /amazing returns greeting`() = testApplication {
    val response = client.get("/amazing")

    assertEquals(HttpStatusCode.OK, response.status)
    assertEquals("Ktor: Hello World!", response.bodyAsText())
}
```

**Database Testing with Testcontainers:**
- Dependency: `testcontainers` 1.21.3, `testcontainers-postgresql` 1.21.3
- Pattern for migrations testing:

```kotlin
@Test
fun `migrations execute successfully`() {
    postgres.start()

    val database = R2dbcDatabase.connect { /* config */ }
    val migrations = Migrations()

    migrations.migrate(database)

    // Assert migrations table has entries
}
```

## Async Testing with Coroutines

**Pattern:**
```kotlin
@Test
fun `suspend function completes`() {
    runBlocking {
        val result = suspendFunctionUnderTest()
        assertEquals(expected, result)
    }
}
```

- Use `runBlocking` wrapper for suspend functions in tests
- Or declare test function as `suspend fun` with test framework that supports it

## Testing Dependencies Summary

**Core Testing:**
- `kotlin-test:1.9.22` - Kotlin test API
- `kotlin-testJunit:2.2.10` - JUnit 4 integration
- `junit:4.13.2` - JUnit 4 runner

**Assertion Libraries:**
- `kotest-assertionsCore:6.0.1` - BDD-style assertions
- `kotest-arrow:6.0.1` - Arrow-kt assertions
- `kotest-arrow-fx-coroutines:6.0.1` - Async/concurrent assertions

**Test Infrastructure:**
- `ktor-server-test-host:3.3.0` - In-process Ktor server testing
- `testcontainers:1.21.3` - Docker container management
- `testcontainers-postgresql:1.21.3` - PostgreSQL container preset

**Dependency Injection:**
- `koin-test:4.1.1` - Koin DI for tests
- `koin-test-junit4:4.1.1` - JUnit 4 integration for Koin

**Bundle:** Testing dependencies configured in `gradle/libs.versions.toml`:
```
testing-server = [
    "ktor-server-test-host",
    "kotlin-testJunit",
    "testcontainers",
    "testcontainers-postgresql",
    "koin-test",
    "koin-test-junit4",
    "kotest-assertionsCore",
    "kotest-arrow",
    "kotest-arrow-fx",
]
```

## Configuration Files

**Build Configuration:**
- Server modules include `testImplementation(libs.bundles.testing.server)` in `build.gradle.kts`
- Example: `server/core/config/build.gradle.kts` line 12

**No test-specific Gradle configuration files found** - uses defaults

## Future Testing Patterns

Based on dependencies available but not yet used:

1. **Kotest Spec Classes** - BDD-style test structures
2. **Property-based Testing** - Arrow + Kotest for generative tests
3. **Arrow FX Concurrent Testing** - For resource/concurrent code paths
4. **Koin Test Injection** - For service/integration tests requiring DI

---

*Testing analysis: 2026-02-10*
