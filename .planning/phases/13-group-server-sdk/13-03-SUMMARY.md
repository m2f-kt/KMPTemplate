# Plan 13-03 Summary: SDK GroupApi Interface, Implementation, and Testing Fakes

**Status:** Complete
**Duration:** ~3 min
**Commits:** 1

## What was built
- GroupApi interface with 9 methods matching server endpoint contract
- GroupApiImpl using Ktor Resources (@Resource route classes) for type-safe HTTP calls
- Sdk facade updated to delegate to GroupApi alongside AuthApi and UserApi
- SdkModule registers GroupApi and updates Sdk constructor in Koin DI
- FakeGroupApiBuilder following existing FakeAuthApiBuilder/FakeUserApiBuilder DSL pattern
- FakeSdkBuilder updated with `group { }` configuration block

## Key files
- `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/GroupApi.kt` -- Interface
- `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/GroupApiImpl.kt` -- HTTP implementation
- `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/Sdk.kt` -- Updated facade
- `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/di/SdkModule.kt` -- Updated DI
- `core/testing/src/commonMain/kotlin/com/m2f/template/core/testing/fakes/FakeGroupApiBuilder.kt` -- Test fake
- `core/testing/src/commonMain/kotlin/com/m2f/template/core/testing/fakes/FakeSdkBuilder.kt` -- Updated builder

## Decisions
- GroupApiImpl constructor takes only HttpClient (no TokenStorage needed unlike AuthApiImpl)
- Default fake responses return Either.Left(AppError.Client.Unknown()) for fail-fast behavior
- All 9 GroupApi methods match the server route contract exactly

## Self-Check: PASSED
- core:sdk compiles on JVM target
- core:testing compiles on JVM target
- GroupApi has 9 methods matching server endpoints
- Sdk delegates to GroupApi via Kotlin delegation
- FakeSdkBuilder supports group {} configuration
