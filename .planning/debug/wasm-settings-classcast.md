---
status: diagnosed
trigger: "WASM Runtime ClassCastException — StorageSettings cannot be cast to ObservableSettings"
created: 2026-02-21T00:00:00Z
updated: 2026-02-21T00:01:00Z
---

## Current Focus

hypothesis: CONFIRMED — StorageSettings (wasmJs) does NOT implement ObservableSettings; the unchecked cast on line 11 of StorageModule.kt fails at runtime
test: Read library source code for wasmJs platform
expecting: StorageSettings only implements Settings, not ObservableSettings
next_action: Report findings

## Symptoms

expected: App starts normally on WASM target
actual: Crash with ClassCastException: Cannot cast StorageSettings to ObservableSettings
errors: InstanceCreationException for PreferencesStorage -> ObservableSettings, ClassCastException at StorageModule.kt:11:34
reproduction: Run app on WASM target
started: Unknown

## Eliminated

## Evidence

- timestamp: 2026-02-21T00:00:30Z
  checked: StorageModule.kt line 11
  found: `single<ObservableSettings> { Settings() as ObservableSettings }` — unchecked cast
  implication: On any platform where Settings() returns a non-ObservableSettings, this will crash

- timestamp: 2026-02-21T00:00:40Z
  checked: multiplatform-settings-no-arg wasmJs source (NoArg.kt)
  found: `actual fun Settings(): Settings = StorageSettings()` — returns StorageSettings on wasmJs
  implication: The no-arg factory returns StorageSettings for browser targets

- timestamp: 2026-02-21T00:00:45Z
  checked: multiplatform-settings core wasmJs source (StorageSettings.kt)
  found: `class StorageSettings(private val delegate: Storage = localStorage) : Settings` — only implements Settings, NOT ObservableSettings
  implication: The cast `Settings() as ObservableSettings` will always fail on wasmJs

- timestamp: 2026-02-21T00:00:50Z
  checked: Android no-arg source for comparison
  found: `SharedPreferencesSettings(delegate)` which implements `ObservableSettings`
  implication: Cast works on Android/iOS/JVM because their implementations DO implement ObservableSettings, but NOT on wasmJs

- timestamp: 2026-02-21T00:00:55Z
  checked: Searched library for makeObservable or any ObservableSettings adapter for StorageSettings
  found: No such wrapper exists in the library
  implication: There is no built-in way to wrap StorageSettings into ObservableSettings

- timestamp: 2026-02-21T00:01:00Z
  checked: PreferencesStorage usage across codebase
  found: Uses ObservableSettings for getStringFlow() (observe theme/language). Used in App.kt, AppNavHost.kt, SdkModule.kt
  implication: PreferencesStorage fundamentally requires observation capability; the fix must either provide observable settings on wasmJs or redesign PreferencesStorage

## Resolution

root_cause: On wasmJs, `Settings()` (from multiplatform-settings-no-arg v1.3.0) returns `StorageSettings`, which only implements the `Settings` interface — NOT `ObservableSettings`. StorageModule.kt line 11 performs an unchecked cast `Settings() as ObservableSettings` that succeeds at compile time (because it's an unchecked cast in common code) but fails at runtime on wasmJs. On Android/iOS/JVM the cast works because their platform implementations (SharedPreferencesSettings, NSUserDefaultsSettings, PreferencesSettings) all implement ObservableSettings.
fix:
verification:
files_changed: []
