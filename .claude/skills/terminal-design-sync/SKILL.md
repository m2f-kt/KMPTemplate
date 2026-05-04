---
name: terminal-design-sync
description: Audits and self-updates the design ↔ code mapping (manifest.json, REFERENCES.md, CODE-MAP.md) by diffing three sources of truth — Pencil reusable components + variables in `terminal_design_system.pen`, the Kotlin theme + components in `app:designsystem`, and the current manifest. Use whenever a Pencil component is added/renamed/removed, a `$--` token changes, a Composable is added/renamed/removed in `app:designsystem`, or after a pull that touched either side. Defaults to read-only audit; pass `--apply` to write the patches.
---

# Terminal Design Sync

The third skill in the design-system trio. Where:
- `terminal-design-generator` enforces design → ref discipline,
- `terminal-design-implementer` enforces design → code lookup,
- **`terminal-design-sync` keeps the mapping itself honest as both sides evolve.**

## When this skill applies

Trigger this skill any time one of these happens:
- A new reusable component is added to `terminal_design_system.pen` (Pencil designer's work)
- An existing Pencil component is renamed, retired, or has its variant set changed
- A `$--terminal-*` / `$--btn-*` / `$--card-*` etc. variable is added, renamed, or its value changes
- A new `Terminal*` Composable lands in `app/designsystem/src/commonMain/kotlin/.../components/`
- A Composable is renamed, deleted, or its public signature changes
- A new `TerminalColors` field, `TerminalTypography` style, `TerminalRadius` step, etc. is added or removed
- After a `git pull` that touches `terminal_design_system.pen` OR anything under `app/designsystem/`
- Quarterly drift check (recommended cadence)

If none of those happened, you don't need this skill.

## Modes

The skill has three explicit modes selected by user intent:

| Mode               | Reads | Writes | Output                                                |
|--------------------|-------|--------|-------------------------------------------------------|
| `--audit` *(default)* | yes  | no    | `last-sync-report.md` next to this file              |
| `--apply`           | yes  | **yes** | report + patched `manifest.json`/`REFERENCES.md`/`CODE-MAP.md` |
| `--verify`          | yes  | no    | report + spawns the 3 verification agents (system / style / mapping) for confirmation |

If the user just says "sync the design system mapping" without qualifier → run **`--audit`**, present the diff, and ask before applying. Never apply silently.

## The five sources of truth

The skill compares three sources against the central truth:

1. **Pencil components** — the 41+ reusable refs in `terminal_design_system.pen`
2. **Pencil variables** — the `$--*` tokens in the same file
3. **Kotlin Composables** — every `@Composable fun Terminal*` under `app/designsystem/src/commonMain/.../components/`
4. **Kotlin theme tokens** — every field in `TerminalColors`, `TerminalTypography`, `TerminalSpacing`, `TerminalGap`, `TerminalRadius`, `TerminalBorders`, `TerminalOpacity`, `TerminalShadows`
5. **Current manifest** — `.claude/skills/terminal-design-generator/manifest.json`

## Workflow (run in this exact order)

### Step 1 — Snapshot the five sources

Run these commands in parallel:

**Pencil components**:
```
mcp__pencil__batch_get(
  filePath: "terminal_design_system.pen",
  patterns: [{ reusable: true }],
  readDepth: 1,
  searchDepth: 3
)
```
Capture: `id`, `name`, child structure (only enough to detect customizable slots).

**Pencil variables**:
```
mcp__pencil__get_variables(filePath: "terminal_design_system.pen")
```
Capture: every variable name + type (color/number/string).

**Kotlin Composables** (use Grep, not full file reads):
```
Grep pattern="@Composable\nfun Terminal\\w+" 
     glob="app/designsystem/src/commonMain/**/*.kt"
     output_mode="content" -n
```
Capture: file path + function name for every match.

**Kotlin theme tokens** (read each theme file fully):
```
Read app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalColors.kt
Read .../TerminalTypography.kt
Read .../TerminalSpacing.kt  Read .../TerminalGap.kt  Read .../TerminalRadius.kt
Read .../TerminalBorders.kt  Read .../TerminalOpacity.kt  Read .../TerminalShadows.kt
```
Capture: every `data class` field name + value.

**Current manifest**:
```
Read .claude/skills/terminal-design-generator/manifest.json
```

### Step 2 — Build the diff

Cross-reference the five sources into these buckets. **Be exhaustive — do not skim.**

#### A. Components (refs)

For each Pencil component ID (e.g. `SpHta`):
- Is it in `manifest.components.*[].id`? → if no → **NEW IN PENCIL**
- Is the `kotlin` field's named function found in the Composable grep results? → if no → **MISSING IN CODE**
- Is the `name` field equal to the actual Pencil component `name`? → if no → **RENAMED**

For each Kotlin `Terminal*` Composable:
- Is it in `manifest.components.*[].kotlin` OR `manifest.code_only_components.*[].kotlin`? → if no → **NEW IN CODE (unmapped)**

For each manifest entry under `components.*`:
- Is the `id` still in the Pencil reusable list? → if no → **REMOVED FROM PENCIL**
- Is the `file` path still present in the repo? → if no → **MOVED OR DELETED IN CODE**

For each manifest entry under `code_only_components.*`:
- Is the `kotlin` symbol still found in the grep results? → if no → **REMOVED FROM CODE**

#### B. Tokens

For each Pencil variable (`$--terminal-bg`, `$--btn-primary-bg`, …):
- Is it in `manifest.tokens.*` (any branch)? → if no → **NEW IN PENCIL**
- Does the matching `kotlin` accessor exist in the actual theme files? → if no → **MISSING IN CODE**

For each Kotlin theme field (`TerminalColors.bg`, `TerminalRadius.xs`, …):
- Is it in `manifest.tokens.*[].kotlin`? → if no → **NEW IN CODE (unmapped)**

For each manifest token entry:
- Is the Pencil variable still defined? → if no → **REMOVED FROM PENCIL**
- Is the Kotlin field still defined? → if no → **REMOVED FROM CODE**

#### C. Scales

For each scale (`tokens.typography.scale`, `tokens.spacing.scale`, `tokens.gap.scale`, `tokens.radius.scale`):
- Count entries in manifest vs entries in the Kotlin data class. → if differ → **SCALE DRIFT**
- For each manifest entry's claimed `dp`/`sp`, compare to the actual literal in the data class constructor. → if differ → **VALUE DRIFT**

### Step 3 — Categorize and report

Write the report to `.claude/skills/terminal-design-sync/last-sync-report.md` with this exact structure:

```markdown
# Sync Report — <YYYY-MM-DD HH:MM>

## Summary
- mode: <audit|apply|verify>
- pencil components: <n>
- code components: <n>
- manifest entries: <n>
- in sync: <n>
- diffs: <n>

## ➕ NEW IN PENCIL (component or token added designer-side, no code yet)
- ref `XXXX` "Name"
  - suggested kotlin: `Terminal<X>(...)` *(pattern guess)*
  - suggested file: `components/<category>/Terminal<X>.kt`
  - **action:** add Composable in code; then re-run sync with `--apply`

## ➕ NEW IN CODE (Composable or token shipped, no Pencil ref yet)
- kotlin `TerminalSomething(...)` at `components/<category>/TerminalSomething.kt`
  - **action (apply mode):** add to `manifest.code_only_components.<category>`; flag for designer to add a Pencil ref

## ➖ REMOVED FROM PENCIL
- ref `YYYY` was in manifest but no longer in `terminal_design_system.pen`
  - matching kotlin: `Terminal<Y>(...)` — still present in code
  - **action (apply mode):** remove `manifest.components.*[id=YYYY]`; if code still uses, optionally move to `code_only_components`

## ➖ REMOVED FROM CODE
- kotlin `Terminal<Z>(...)` was in manifest but no longer found in `app:designsystem`
  - matching ref: `ZZZZ` — still in Pencil
  - **action (apply mode):** remove from manifest; flag designer that the ref is orphaned

## 🔄 RENAMED
- ref `AAAA`: manifest name "Old" → actual Pencil name "New"
- kotlin: manifest claims `OldName(...)` → actual `NewName(...)` at <file>
  - **action (apply mode):** patch manifest fields

## ⚠️ SCALE DRIFT
- typography: manifest scale lists [...] but code ships [...]
- spacing: ...
  - **action (apply mode):** rewrite the scale block in manifest

## ⚠️ VALUE DRIFT
- token `--terminal-radius`: manifest claims dp=4, code has `TerminalRadius.sm = 4.dp` ✓ (OK)
- token `--btn-primary-bg`: Light theme value Pencil=#525252, code=#525252 ✓
- token `--checkbox-bg`: Pencil-side missing (still TODO)

## ✅ IN SYNC
<n> components and <n> tokens are fully aligned. (No action needed.)

## Recommended apply order
1. <list of patches in dependency order>
```

### Step 4 — Apply (only if `--apply`)

Apply patches in this order to keep the system valid at every intermediate step:

1. **Update `manifest.json`** — patch the categorized entries.
2. **Mirror to `REFERENCES.md`** — re-emit the affected sections (components table, token tables, scales). Edit in place; do not regenerate the whole file.
3. **Mirror to `CODE-MAP.md`** — re-emit the matching rows in the reverse-lookup tables.
4. **Bump `manifest.version`** following semver:
   - patch: typo / clarification only
   - minor: components or tokens added (additive)
   - major: components or tokens removed, or scale shape changed

After applying, **re-read** the three files and verify they parse / render cleanly. Don't trust your own write — confirm.

### Step 5 — Verify (only if `--verify` or after `--apply`)

Spawn three read-only verification agents in parallel (same prompts as the original audit pipeline):
1. **System audit** — confirm every `manifest.components.*` entry resolves in code; every `code_only_components` entry exists.
2. **Style audit** — confirm no new hex/font/spacing leaks were introduced in any Composable that was touched as part of the sync.
3. **Mapping audit** — confirm every manifest token entry has a Kotlin counterpart; every Kotlin field is in the manifest.

If any agent reports drift → STOP, write the new findings to the report, and ask the user before applying further.

## Apply-mode safety rules

These are **non-negotiable** when patching the manifest files:

- ✋ Never delete an entry without listing it in the report's `➖ REMOVED` section first.
- ✋ Never rename an entry without listing it in the report's `🔄 RENAMED` section first.
- ✋ Never collapse a scale (e.g. drop a typography step) — only widen them.
- ✋ Never modify the `escape_hatches`, `rules.required`, or `rules.forbidden` blocks via sync — those are policy, not data.
- ✋ Never touch `atlas_palette` — those colors are decorative metaphor and outside this skill's scope.
- ✅ Always preserve handwritten `_*_note` comments and `todo` annotations in the manifest.
- ✅ Always update `manifest.version` and a top-level `_last_sync` ISO timestamp.

If applying a patch would violate any of these, stop and ask.

## Output paths

```
.claude/skills/terminal-design-sync/
├── SKILL.md                      ← this file
└── last-sync-report.md           ← generated each run (overwrites previous)
```

The report is the audit trail. Don't delete it between runs — overwriting is fine, but never `rm -f` it.

## Quick start

User says: *"sync the design system mapping"*
→ run `--audit`, write report, summarize the diff in chat, ask for confirmation before applying.

User says: *"apply the design system sync"*
→ run `--audit` first (to confirm the diff is what they expect), then `--apply`, then `--verify`.

User says: *"check for design drift"*
→ run `--audit`, summarize counts only, point at the report file.

## Why this exists

Without this skill, the manifest decays the moment a designer adds a new ref or an engineer adds a new Composable. The sister skills (`generator` / `implementer`) trust the manifest blindly — they have no way to know it's stale. This skill is the only thing that keeps the manifest *true*.

Treat it like a code formatter: cheap to run, safe by default, easy to apply once you've reviewed the diff.
