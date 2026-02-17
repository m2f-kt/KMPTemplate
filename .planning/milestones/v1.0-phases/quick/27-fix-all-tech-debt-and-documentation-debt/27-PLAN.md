---
phase: quick-27
plan: 01
type: execute
wave: 1
depends_on: []
files_modified:
  - .planning/REQUIREMENTS.md
  - .planning/phases/03-client-sdk-storage/03-VERIFICATION.md
  - .planning/phases/06.1-add-the-current-chat-agent-exploration-refactor/06.1-VERIFICATION.md
  - .planning/phases/09-wasm-http-engine-fix/09-VERIFICATION.md
  - .planning/v1-MILESTONE-AUDIT.md
autonomous: true
requirements: []
must_haves:
  truths:
    - "All 32 requirements in REQUIREMENTS.md show Satisfied status (not Pending)"
    - "Phase 3 VERIFICATION.md has explicit requirement ID mapping for SDK-01 through SDK-04, STOR-01, STOR-02"
    - "Phase 9 has a VERIFICATION.md file based on UAT evidence and code inspection"
    - "Phase 06.1 VERIFICATION.md reflects WebSocket transport (not SSE) matching current code"
    - "MILESTONE-AUDIT.md documentation_gaps section is updated to reflect resolved items"
  artifacts:
    - path: ".planning/REQUIREMENTS.md"
      provides: "Checked-off traceability table"
      contains: "Satisfied"
    - path: ".planning/phases/09-wasm-http-engine-fix/09-VERIFICATION.md"
      provides: "Phase 9 verification report"
    - path: ".planning/phases/06.1-add-the-current-chat-agent-exploration-refactor/06.1-VERIFICATION.md"
      provides: "Updated verification reflecting WebSocket transport"
      contains: "WebSocket"
    - path: ".planning/phases/03-client-sdk-storage/03-VERIFICATION.md"
      provides: "Explicit requirement ID mapping"
      contains: "SDK-01"
  key_links: []
---

<objective>
Fix all actionable tech debt and documentation debt items identified in the v1.0 milestone audit.

Purpose: Close out remaining documentation gaps so the milestone audit status can move from "tech_debt" to "complete". Resolve the stale 06.1 verification doc, add missing Phase 9 verification, update REQUIREMENTS.md traceability, and add requirement ID mapping to Phase 3 verification.

Output: Updated documentation files, new Phase 9 VERIFICATION.md, updated MILESTONE-AUDIT.md
</objective>

<execution_context>
@./.claude/get-shit-done/workflows/execute-plan.md
@./.claude/get-shit-done/templates/summary.md
</execution_context>

<context>
@.planning/PROJECT.md
@.planning/ROADMAP.md
@.planning/STATE.md
@.planning/v1-MILESTONE-AUDIT.md
@.planning/REQUIREMENTS.md
@.planning/phases/03-client-sdk-storage/03-VERIFICATION.md
@.planning/phases/06.1-add-the-current-chat-agent-exploration-refactor/06.1-VERIFICATION.md
@.planning/phases/09-wasm-http-engine-fix/09-UAT.md
@.planning/phases/09-wasm-http-engine-fix/09-01-SUMMARY.md
</context>

<tasks>

<task type="auto">
  <name>Task 1: Update REQUIREMENTS.md traceability and add requirement ID mapping to Phase 3 VERIFICATION.md</name>
  <files>
    .planning/REQUIREMENTS.md
    .planning/phases/03-client-sdk-storage/03-VERIFICATION.md
  </files>
  <action>
  **REQUIREMENTS.md:**
  Update the Traceability table at the bottom of REQUIREMENTS.md. Change all 32 rows from `Pending` to `Satisfied`:

  - FOUND-01 through FOUND-07: Phase 1, Satisfied
  - AUTH-01 through AUTH-07: Phase 2, Satisfied
  - SDK-01 through SDK-04: Phase 3, Satisfied
  - STOR-01: Phase 3, Satisfied
  - STOR-02: Phase 3, Satisfied (note: infrastructure exists, no runtime consumer yet)
  - NAV-01: Phase 4, Satisfied
  - NAV-02: Phase 5, Satisfied
  - NAV-03: Phase 4, Satisfied
  - NAV-04: Phase 4, Satisfied
  - AI-01 through AI-04: Phase 6, Satisfied
  - DX-01: Phase 5, Satisfied
  - DX-02: Phase 5, Satisfied
  - CC-01: Phase 1, Satisfied
  - CC-02: Phase 1, Satisfied

  Also change the v1 requirements checkboxes from `- [ ]` to `- [x]` for all 32 items.

  Update the "Last updated" date at the bottom to 2026-02-17.

  **Phase 3 VERIFICATION.md:**
  Replace the "### Requirements Coverage" section (which currently says "Phase 03 does not have explicit REQUIREMENTS.md mappings. Goal-level verification substitutes.") with an explicit requirement-to-truth mapping table:

  ```
  ### Requirements Coverage

  | Requirement | Truth(s) | Status |
  |-------------|----------|--------|
  | SDK-01 | #2 (apiCall returns Either), #7 (every endpoint has SDK function) | Satisfied |
  | SDK-02 | #6 (bearer token + 401 refresh+retry) | Satisfied |
  | SDK-03 | #3 (HTTP status mapped to AppError subtypes), #4 (network exceptions mapped) | Satisfied |
  | SDK-04 | #1 (platform-specific engines via expect/actual) | Satisfied |
  | STOR-01 | #5 (auth tokens persisted, read, cleared) | Satisfied |
  | STOR-02 | Artifact: PreferencesStorage.kt verified (theme/language with Flow observation) | Satisfied (infrastructure-ready) |
  ```
  </action>
  <verify>
  - grep for "Pending" in REQUIREMENTS.md traceability table -- should find 0 occurrences
  - grep for "Satisfied" in REQUIREMENTS.md traceability table -- should find 32 occurrences
  - grep for "SDK-01" in 03-VERIFICATION.md -- should find a match in the new requirements section
  </verify>
  <done>All 32 requirements show "Satisfied" in traceability table, all 32 v1 requirement checkboxes are checked, Phase 3 VERIFICATION.md has explicit requirement ID mapping for all 6 requirements (SDK-01 through SDK-04, STOR-01, STOR-02)</done>
</task>

<task type="auto">
  <name>Task 2: Fix stale 06.1 VERIFICATION.md and create missing Phase 9 VERIFICATION.md</name>
  <files>
    .planning/phases/06.1-add-the-current-chat-agent-exploration-refactor/06.1-VERIFICATION.md
    .planning/phases/09-wasm-http-engine-fix/09-VERIFICATION.md
  </files>
  <action>
  **06.1 VERIFICATION.md -- Update SSE references to WebSocket:**

  This file was written when Phase 06.1 used SSE transport. Quick-24 later migrated to WebSocket. Update the file to reflect reality:

  1. Add a re-verification header section:
     - Change `re_verification: false` to `re_verification: true` in frontmatter
     - Add `re_verified: 2026-02-17T00:00:00Z` to frontmatter
     - Add `re_verification_reason: "quick-24 migrated chat streaming from SSE to WebSocket"` to frontmatter

  2. Update Truth #5: Change from "ktor-server-sse dependency declared and available" to "ktor-server-websockets dependency used for chat streaming (SSE dependency still present but chat streaming migrated to WebSocket by quick-24)"

  3. Update Truth #6: Change from "Clients can connect to SSE endpoint and receive streamed responses" to "Clients connect via WebSocket at /api/ai/chat/ws for streamed responses (migrated from SSE /chat/stream by quick-24)"

  4. Update Truth #7: Change from "SSE endpoint validates JWT from query parameter" to "WebSocket endpoint validates JWT from Authorization header (query param fallback for browser clients)"

  5. Add a "### Post-Verification Changes" section at the end (before the Summary) documenting: "quick-24 (2026-02-15) replaced the SSE streaming endpoint with bidirectional WebSocket. The core streaming strategy, per-request AIAgent pattern, and callbackFlow bridge remain unchanged. Transport layer changed from SSE ServerSentEvents to WebSocket ChatStreamFrame with JSON serialization."

  6. Update the Summary section to mention WebSocket transport instead of SSE.

  **Phase 9 VERIFICATION.md -- Create from UAT evidence:**

  Create a new verification file at `.planning/phases/09-wasm-http-engine-fix/09-VERIFICATION.md` following the standard format. Use the 09-UAT.md (4/4 passed) and 09-01-SUMMARY.md as evidence:

  Frontmatter:
  ```yaml
  ---
  phase: 09-wasm-http-engine-fix
  verified: 2026-02-17T00:00:00Z
  status: passed
  score: 3/3 must-haves verified
  re_verification: false
  ---
  ```

  Phase Goal: "Swap the CIO HTTP engine for the Js engine on the wasmJs target so that browser-based WASM builds can make network requests."

  Observable Truths (derived from ROADMAP success criteria):
  1. "WASM browser build can make HTTP requests to the server" -- VERIFIED via UAT tests 1+2 (login + registration requests pass in browser)
  2. "Non-WASM targets continue using their current engines" -- VERIFIED via UAT test 3 (Desktop/JVM still works) + code inspection (only wasmJsMain PlatformEngine changed)
  3. "Auth interceptor and token refresh work correctly on WASM" -- VERIFIED via UAT test 1 (login succeeds implying full auth flow) + UAT test 4 (CORS preflight passes)

  Required Artifacts (from SUMMARY):
  - `gradle/libs.versions.toml` -- ktor-client-js library entry
  - `core/sdk/build.gradle.kts` -- wasmJsMain uses ktor-client-js
  - `core/sdk/src/wasmJsMain/kotlin/com/m2f/template/sdk/PlatformEngine.wasmJs.kt` -- Returns Js() factory
  - `server/src/main/kotlin/com/m2f/template/Application.kt` -- install(CORS) with dev origins

  Key Links:
  - PlatformEngine.wasmJs.kt -> Js engine (replaces CIO)
  - Application.kt CORS -> browser fetch compatibility

  Requirements Coverage: Gap closure (UAT Phase 8 -- WASM login broken). No specific requirement IDs.

  Evidence source: UAT 4/4 passed (2026-02-16), compilation verified on all 4 KMP targets.
  </action>
  <verify>
  - File exists: .planning/phases/09-wasm-http-engine-fix/09-VERIFICATION.md
  - grep for "WebSocket" in 06.1-VERIFICATION.md -- should find matches
  - grep for "re_verification: true" in 06.1-VERIFICATION.md -- should find match
  - grep for "status: passed" in 09-VERIFICATION.md -- should find match
  </verify>
  <done>06.1 VERIFICATION.md reflects WebSocket transport reality with re-verification note, Phase 9 has a proper VERIFICATION.md documenting all 3 success criteria as verified with UAT evidence</done>
</task>

<task type="auto">
  <name>Task 3: Update MILESTONE-AUDIT.md to reflect resolved documentation debt</name>
  <files>
    .planning/v1-MILESTONE-AUDIT.md
  </files>
  <action>
  Update the MILESTONE-AUDIT.md to reflect the documentation fixes made by Tasks 1 and 2:

  1. In frontmatter, change `status: tech_debt` to `status: complete` (all actionable items resolved)

  2. In the `documentation_gaps` frontmatter array:
     - Strike through or remove the `missing_verification` for Phase 9 (now exists)
     - Strike through or remove `unchecked_traceability` (now all 32 checked)
     - Strike through or remove `missing_req_mapping` for Phase 3 (now has explicit mapping)
     - Keep `missing_summary_frontmatter` as acknowledged/deferred (39 files, low impact)

  3. Update "Documentation Debt" section (near bottom):
     - Item 1 (REQUIREMENTS.md checkboxes): Mark as FIXED with note "Updated to Satisfied"
     - Item 2 (Phase 3 VERIFICATION.md): Mark as FIXED with note "Added explicit requirement ID mapping"
     - Item 3 (Phase 9 VERIFICATION.md): Mark as FIXED with note "Created from UAT evidence"
     - Item 4 (SUMMARY frontmatter): Keep as acknowledged/deferred

  4. Update the "Phase Verification Summary" table:
     - Phase 9 row: Change from `**no verification**` / `--` to `passed` / `3/3 truths` with note "Created from UAT evidence"
     - Phase 06.1 row: Add note "(re-verified: SSE -> WebSocket)"

  5. In the "Source Availability" table:
     - VERIFICATION.md: Change from "9/10 phases" to "10/10 phases"

  6. Update the "Changes Since Previous Audit" section to add a new subsection for this documentation fix pass.

  7. Update the Conclusion paragraph to remove "remaining tech debt" qualifiers about documentation -- state that documentation debt is resolved (except SUMMARY frontmatter which is deferred).

  8. Update `*Updated:` date at the bottom to 2026-02-17.

  **Keep the 2 human_needed tech debt items (runtime DI verification, WASM prod stability) as-is -- they genuinely need human testing. Keep STOR-02 as infrastructure-ready tech debt.**
  </action>
  <verify>
  - grep for "status: complete" in v1-MILESTONE-AUDIT.md frontmatter -- should find match
  - grep for "10/10 phases" in v1-MILESTONE-AUDIT.md -- should find match
  - grep for "FIXED" in Documentation Debt section -- should find 3 occurrences
  </verify>
  <done>MILESTONE-AUDIT.md status is "complete", documentation debt items 1-3 marked FIXED, Phase 9 verification row shows passed, source availability shows 10/10 phases, remaining items (human_needed tech debt + STOR-02 + SUMMARY frontmatter) acknowledged as deferred</done>
</task>

</tasks>

<verification>
After all 3 tasks:
1. REQUIREMENTS.md has 32 "Satisfied" entries and 0 "Pending" entries in the traceability table
2. Phase 3 VERIFICATION.md has explicit SDK-01, SDK-02, SDK-03, SDK-04, STOR-01, STOR-02 mappings
3. Phase 06.1 VERIFICATION.md mentions WebSocket (not just SSE) and has re_verification: true
4. Phase 9 VERIFICATION.md exists with status: passed
5. MILESTONE-AUDIT.md has status: complete and 3 FIXED documentation items
</verification>

<success_criteria>
- All 32 v1 requirement checkboxes checked and traceability shows Satisfied
- Phase 3 verification has explicit requirement ID mapping table
- Phase 06.1 verification reflects WebSocket reality
- Phase 9 verification exists based on UAT evidence
- Milestone audit reflects resolved documentation debt
- No changes to actual application code (pure documentation fixes)
</success_criteria>

<notes>
**Acknowledged but NOT addressed in this plan:**

1. **Phase 01 runtime DI verification (human_needed):** Koin DI resolution on all 4 KMP targets requires human runtime testing. Cannot be fixed by documentation changes.

2. **Phase 01 WASM production stability (human_needed):** Production build stability requires human deployment testing. Cannot be fixed by documentation changes.

3. **STOR-02 PreferencesStorage unused:** The module exists and is wired in DI but no screen/ViewModel consumes it. This is infrastructure-ready for developers using the template. Wiring it into a specific consumer (e.g., theme toggle in dashboard) would be a separate quick task if desired.

4. **SUMMARY frontmatter (39 files):** Adding requirements-completed field to 39 SUMMARY.md files is high effort, low value. The 2-source cross-reference (VERIFICATION + REQUIREMENTS.md traceability) is sufficient. Deferred.
</notes>

<output>
After completion, create `.planning/quick/27-fix-all-tech-debt-and-documentation-debt/27-SUMMARY.md`
</output>
