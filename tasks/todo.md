# Port NoType generics → Template + Aura re-skin

Branch: `feat/port-notype-generics-aura`
Plan + findings: `tasks/port-plan.json`, `tasks/port-findings.json`

## Decisions (user-confirmed)
1. **Design** = FULL Aura re-skin: rename `Terminal*`→`Aura*`, neon palette (cyan/violet/magenta), dark-canonical surfaces, ship Space Grotesk + Manrope + JetBrains Mono fonts, aurora-border + waveform signatures, re-author Pencil design skills.
2. **Scope** = EVERYTHING (all phases), on this branch, dependency-ordered, verified + committed per phase.
3. **Observability** = FULL stack, bump Koog baseline 0.6.2 → 1.0.0-preview, wire OTel + Langfuse by default.
4. **Native** = ALL FOUR new modules: `server:core:observability`, `core:permissions`, `core:platform-macos`, `core:securestorage` (full Keychain/DPAPI/Keystore/cinterop).

## Cross-cutting invariants
- **KEEP wasmJs** as first-class target. Never copy NoType `build.gradle.kts` wholesale. Every ported `expect` needs a `wasmJs` actual.
- Package rename `com.m2f.notype.*` → `com.m2f.template.*` in every ported file.
- Do NOT port NoType-local regressions: workflow_dispatch-only CI, wasmJs/ktor-client-js removal, macos-native-gate.yml.

## Phases

### Wave 1 — Foundation (disjoint modules)
- [ ] P0 Build/env foundation: EnvLoader, detekt hook rewrite, settings.json node→bash fix, version catalog, gitignore
- [ ] P1 `:server:run` .env injection + Env.kt robust discovery
- [ ] P2 core:sdk transport: HttpTimeout + WebSockets + requireSecureBaseUrl
- [ ] P3 core:sdk ErrorMapper raw-body capture + body-code dispatch hook
- [ ] P4 core:navigation primitives (NavSelectionSignal + navigateAdd + navigationModule)
- [ ] S1 server-core: migration dedup + named auth-jwt provider
- [ ] A1 core:models AppError generalization (Permission/Native + Permission enum + PermissionStatus)

### Wave 2 — Shared/server hardening
- [ ] P5 InMemorySettings→core:testing, PreferencesStorage tests, WS/streaming test deps
- [ ] P6 generic reactive consent pref + WS→Flow pattern docs
- [ ] S2 boot-time invariant framework + boot validation + safe key-length logging
- [ ] S3 TLS 1.3 enforcement test

### Wave 3 — Design system (Aura) — SEQUENTIAL
- [ ] D1 tokens: Motion + Glows groups
- [ ] D2 tokens: Color slots (neon) + Radius xl
- [ ] D3 tokens: 3-family typography + fonts (Space Grotesk + Manrope) + back-compat aliasing
- [ ] D4 effect modifiers (auroraBorder etc.) + new generic components
- [ ] D5 rename Terminal*→Aura* across 21 screens + Pencil skill/.pen reconciliation

### Wave 4 — Observability
- [ ] O0 Koog 0.6.2 → 1.0.0-preview bump (prerequisite)
- [ ] O1 server:core:observability module: OTel SDK + Langfuse SpanAdapter
- [ ] O2 trace-privacy redaction gate + Langfuse Env config
- [ ] O3 Langfuse REST clients + prompt-provider + eval/judge harness
- [ ] O4 self-hosted Langfuse docker profile + .env.example + seed task + docs

### Wave 5 — Native / Apple / desktop
- [ ] A2 core:platform-macos (JNA bridge) + reduced-motion accessibility
- [ ] A3 core:permissions (gate + status-probe + AX deep-link)
- [ ] A4 core:securestorage (Keychain/DPAPI/Keystore backends)
- [ ] A5 desktop shell (title-bar inset, dark-mode detect, single-instance, file logging, decision helpers)
- [ ] A6 desktop packaging recipe + plist-lint buildSrc helper
- [ ] A7 floating-overlay/tray/Compose-in-Swing scaffolds + pure HUD math

### Wave 6 — Tooling / docs / skills
- [ ] G1 setup.sh whitelabel wizard upgrade
- [ ] G2 DI seam (expect/actual allAppModules) — only if per-platform DI omission needed
- [ ] G3 dev/CI scripts + launch.json + .mcp.json + compose hot-reload MCP
- [ ] G4 CLAUDE.md generic sections + skill verification steps + skills-lock convention
- [ ] G5 reusable skill additions (launch-*, asc-*, langfuse, permissions)

## Review
(filled at end)
