---
status: investigating
trigger: "WASM target fails to compile - js() call must be single expression in top-level function body"
created: 2026-02-21T00:00:00Z
updated: 2026-02-21T00:00:00Z
---

## Current Focus

hypothesis: The js() call was extracted to a top-level function, but the function chains .toString().take(2) after the js() call — making it NOT a "single expression" function body containing only js()
test: Read the Kotlin/Wasm docs for js() restrictions
expecting: js() must be the ONLY expression in the function body; no chaining allowed
next_action: Verify hypothesis and propose correct fix

## Symptoms

expected: WASM target compiles successfully after 15-11 fix
actual: Compilation error at line 15: "Calls to 'js(code)' must be a single expression inside a top-level function body or a property initializer"
errors: e: file:///...AppLocale.wasmJs.kt:15:41 Calls to 'js(code)' must be a single expression
reproduction: Build WASM target
started: After 15-11 fix was applied (fix was insufficient)

## Eliminated

## Evidence

- timestamp: 2026-02-21T00:01:00Z
  checked: Current file content at AppLocale.wasmJs.kt
  found: |
    Line 15: `private fun browserLanguage(): String = js("navigator.language").toString().take(2)`
    The js() call is chained with .toString().take(2) in the same expression
  implication: The Kotlin/Wasm compiler requires js() to be the ENTIRE body — no method chaining allowed

- timestamp: 2026-02-21T00:02:00Z
  checked: git show c5649b2 (the 15-11 fix commit)
  found: |
    The fix moved `js("navigator.language")` from inline in getAppLocale() to a new private function browserLanguage().
    But it kept the .toString().take(2) chaining on the same js() call.
  implication: The fix addressed nesting-in-elvis but missed the chaining constraint

## Resolution

root_cause: The js() call on line 15 chains `.toString().take(2)` — Kotlin/Wasm requires js() to be the sole expression in a top-level function body or property initializer, with NO chaining.
fix: Split into two functions: one that only calls js() returning JsString, and another that converts/truncates
verification: pending
files_changed: []
