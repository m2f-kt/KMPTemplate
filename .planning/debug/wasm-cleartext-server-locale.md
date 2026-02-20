---
status: diagnosed
trigger: "UAT Test 10: WASM compile error + Android cleartext + server returns English errors"
created: 2026-02-20T10:00:00Z
updated: 2026-02-20T10:15:00Z
---

## Current Focus

hypothesis: Three independent issues with clear root causes identified
test: Code reading and structural analysis
expecting: N/A — diagnosis complete
next_action: Return structured diagnosis

## Symptoms

expected: WASM compiles, Android connects to localhost, server returns localized error messages
actual: WASM fails to compile at AppLocale.wasmJs.kt:16; Android blocked by cleartext policy; server always returns English
errors: "Calls to 'js(code)' must be a single expression inside a top-level function body or a property initializer in Kotlin/Wasm"
reproduction: Build WASM target; run Android against localhost; send request without Accept-Language header
started: Phase 15 localization — introduced in plan 05

## Eliminated

(none — all three root causes confirmed on first investigation)

## Evidence

- timestamp: 2026-02-20T10:02:00Z
  checked: composeApp/src/wasmJsMain/kotlin/com/m2f/template/localization/AppLocale.wasmJs.kt:16
  found: `js("navigator.language")` used inside expression body of `getAppLocale()` — in Kotlin/Wasm, js() calls MUST be either (a) a top-level function body or (b) a property initializer; they cannot appear inside another expression like `overrideLocale ?:`
  implication: Direct root cause of WASM compile failure

- timestamp: 2026-02-20T10:03:00Z
  checked: composeApp/src/androidMain/AndroidManifest.xml
  found: No `android:usesCleartextTraffic="true"` attribute on <application>. No network_security_config.xml exists anywhere in the project.
  implication: Android 9+ blocks all cleartext (HTTP) traffic by default. Localhost connections fail.

- timestamp: 2026-02-20T10:05:00Z
  checked: core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/ApiClient.kt (defaultRequest block, lines 47-50)
  found: defaultRequest only sets base URL and Content-Type. No Accept-Language header is ever set. AuthInterceptor only adds Authorization header.
  implication: Client NEVER sends Accept-Language — server's preferredLanguage() always falls back to "en"

- timestamp: 2026-02-20T10:07:00Z
  checked: server/core/config/src/main/kotlin/com/m2f/core/config/server/Error.kt (preferredLanguage function)
  found: Server correctly reads Accept-Language header and passes locale to ServerStrings.resolve(). Fully wired: DomainError.respond() -> preferredLanguage() -> ServerStrings.resolve(code, locale)
  implication: Server-side localization IS implemented. Problem is purely client-side — header not sent.

- timestamp: 2026-02-20T10:08:00Z
  checked: server/core/config/src/main/kotlin/com/m2f/core/config/server/localization/ServerStrings.kt
  found: Has full en + es translation maps for all error codes. resolve() function correctly falls back en -> code.
  implication: Server localization fully functional — just needs the header

- timestamp: 2026-02-20T10:10:00Z
  checked: server Application.kt CORS config (lines 78-94)
  found: allowHeader only includes ContentType and Authorization. Accept-Language is NOT in the CORS allowed headers list.
  implication: For WASM/browser clients, even if client DOES send Accept-Language, CORS preflight will reject it as a non-simple header

- timestamp: 2026-02-20T10:12:00Z
  checked: composeApp/src/commonMain/kotlin/com/m2f/template/localization/AppLocale.kt
  found: getAppLocale() exists in commonMain as an expect function. Available on all platforms.
  implication: ApiClient can use getAppLocale() to set Accept-Language header — but getAppLocale is in composeApp module while ApiClient is in core/sdk module. Architectural boundary issue.

## Resolution

root_cause: |
  Issue 1 (WASM): js() call at line 16 of AppLocale.wasmJs.kt is nested inside an elvis expression. Kotlin/Wasm requires js() to be a standalone top-level function body or property initializer.
  Issue 2 (Android): AndroidManifest.xml lacks android:usesCleartextTraffic="true" and there is no network_security_config.xml.
  Issue 3 (Server English): ApiClient.kt defaultRequest block does not set Accept-Language header. Server is fully wired to read it. Additionally, CORS config does not allow Accept-Language header for browser clients.
fix: (not applied — diagnosis only)
verification: (not applied — diagnosis only)
files_changed: []
