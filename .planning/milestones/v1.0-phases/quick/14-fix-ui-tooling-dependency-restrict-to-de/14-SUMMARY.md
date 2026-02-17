# Quick Task 14: Fix ui-tooling dependency — restrict to debug Android only

## Result: COMPLETE

## Changes

| File | Change |
|------|--------|
| `app/designsystem/build.gradle.kts` | Moved `libs.androidx.ui.tooling` from `commonMain.dependencies` to top-level `debugImplementation` |

## Root Cause

`androidx.compose.ui:ui-tooling:1.10.3` was declared in `commonMain.dependencies` but this library is Android/JVM-only. When Gradle resolves dependencies for iOS native targets (`ios_simulator_arm64`), it cannot find a matching variant, causing the build to fail.

## Fix

- Removed `implementation(libs.androidx.ui.tooling)` from `commonMain.dependencies { }`
- Added a top-level `dependencies { debugImplementation(libs.androidx.ui.tooling) }` block

This ensures the tooling library is only available for Android debug builds (where it's needed for interactive previews in Android Studio) while keeping iOS, WasmJs, and JVM targets clean.

## Verification

- `compileKotlinIosSimulatorArm64` — BUILD SUCCESSFUL
- `compileKotlinJvm` — BUILD SUCCESSFUL
- `compileKotlinWasmJs` — BUILD SUCCESSFUL

## Commits

| Hash | Message |
|------|---------|
| `0f92825` | fix(quick-14): move androidx.ui.tooling to Android debugImplementation |
