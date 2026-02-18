---
status: resolved
phase: 11-testing-infrastructure
source: 11-01-SUMMARY.md, 11-02-SUMMARY.md, 11-03-SUMMARY.md
started: 2026-02-18T13:57:11Z
updated: 2026-02-18T14:35:08Z
---

## Current Test

[testing complete]

## Tests

### 1. SDK Interfaces Extracted
expected: AuthApi.kt is an interface (not a class) with 6 suspend method signatures. AuthApiImpl.kt exists as the concrete implementation. Same for UserApi (3 methods) and UserApiImpl.
result: pass

### 2. Sdk Facade with Delegation
expected: Sdk.kt combines AuthApi and UserApi via Kotlin `by` delegation (e.g., `class Sdk(...) : AuthApi by authApi, UserApi by userApi`).
result: pass

### 3. Koin Bindings Use Interface Types
expected: SdkModule.kt binds using interface type qualifiers: `single<AuthApi> { AuthApiImpl(...) }` and `single<UserApi> { UserApiImpl(...) }`, plus registers the Sdk facade.
result: pass

### 4. Core Testing Module Compiles
expected: Running `./gradlew :core:testing:assemble` completes successfully. The module re-exports Turbine, Kotest assertions, Arrow assertions, and coroutine-test via api() dependencies.
result: issue
reported: "it fails with compileReleaseKotlinAndroid and compileDebugKotlinAndroid - Unresolved reference 'AfterTest' and 'BeforeTest' in ViewModelTest.kt. kotlin-test-junit was added for JVM but Android target wasn't covered."
severity: blocker

### 5. Fake AuthApi Builder DSL
expected: FakeAuthApiBuilder.kt provides a `fakeAuthApi { login { email, password -> ... } }` top-level DSL function that creates a configurable AuthApi test double with per-method lambda overrides.
result: pass

### 6. LoginViewModel Reference Tests Pass
expected: Running `./gradlew :app:auth:jvmTest` executes 3 LoginViewModelTest cases (login success, validation error, server error) and all pass.
result: pass

### 7. MviViewModel DSL Tests Pass
expected: Running `./gradlew :core:testing:jvmTest` executes 3 MviViewModelTestDslTest cases (model assertion, event assertion, mixed) and all pass.
result: pass

## Summary

total: 7
passed: 6
issues: 1
pending: 0
skipped: 0

## Gaps

- truth: "core:testing module compiles on all KMP targets including Android"
  status: resolved
  reason: "User reported: compileReleaseKotlinAndroid and compileDebugKotlinAndroid fail with Unresolved reference 'AfterTest' and 'BeforeTest' in ViewModelTest.kt. kotlin-test-junit was added for JVM but Android target wasn't covered."
  severity: blocker
  test: 4
  root_cause: "kotlin-test-junit added only to jvmMain.dependencies but androidTarget is a separate compilation unit in KMP that does not inherit jvmMain dependencies. @BeforeTest/@AfterTest are expect declarations needing kotlin-test-junit actual provider on Android classpath too."
  artifacts:
    - path: "core/testing/build.gradle.kts"
      issue: "kotlin-test-junit only in jvmMain.dependencies, missing from androidMain"
  missing:
    - "Add androidMain.dependencies { api(libs.kotlin.testJunit) } to core/testing/build.gradle.kts"
  debug_session: ""
