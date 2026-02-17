---
phase: quick-02
plan: 01
subsystem: ui
tags: [compose, input, password, canvas, foundation, pencil-design]

# Dependency graph
requires:
  - phase: 04-navigation-ui
    provides: TerminalTheme tokens, Foundation-only composable patterns
provides:
  - TerminalInput aligned to Pencil design spec (prefix, conditional border, correct tokens)
  - TerminalPasswordInput with eye toggle icon
  - Canvas-based EyeIcon (open/closed states)
affects: [04-navigation-ui, auth-screens, form-components]

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "Canvas-based icon drawing for custom icons (EyeIcon follows TerminalCheckbox/TerminalRadio pattern)"
    - "Conditional border modifier with .then() for state-dependent styling"

key-files:
  modified:
    - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/input/TerminalInput.kt

key-decisions:
  - "Removed leadingIcon parameter -- Pencil design specifies only the '>' prefix, no generic leading icon slot"
  - "Used literal 6.dp for label gap (not a standard token) since Pencil specifies 6dp which falls between gap.xs (4) and gap.sm (8)"
  - "Combined Task 1 and Task 2 into single commit since both modify the same file and are tightly coupled"

patterns-established:
  - "Terminal prefix pattern: '>' character with state-dependent color (success when filled, textMuted when empty)"
  - "Conditional border pattern: border hidden when field has content, visible when empty or error"

# Metrics
duration: 2min
completed: 2026-02-12
---

# Quick Task 2: Align TerminalInput with Pencil Design Summary

**TerminalInput rewritten with ">" prefix, conditional border, correct spacing/typography tokens, plus TerminalPasswordInput with Canvas-based eye toggle**

## Performance

- **Duration:** 2 min
- **Started:** 2026-02-12T14:28:02Z
- **Completed:** 2026-02-12T14:29:41Z
- **Tasks:** 2
- **Files modified:** 1

## Accomplishments
- Fixed all 7 Pencil design divergences in TerminalInput (prefix, border, padding, typography, label weight, gaps, radius)
- Added `visualTransformation` parameter to TerminalInput for password masking support
- Created TerminalPasswordInput composable wrapping TerminalInput with password visibility toggle
- Created Canvas-based EyeIcon (open=pupil circle, closed=diagonal slash) consistent with checkbox/radio icon patterns
- Updated preview to show 5 states: empty, filled, error, password-masked, password-empty

## Task Commits

Both tasks committed atomically (same file, tightly coupled changes):

1. **Task 1+2: Fix TerminalInput + Create TerminalPasswordInput** - `8a93f6d` (feat)

## Files Created/Modified
- `app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/input/TerminalInput.kt` - TerminalInput (Pencil-aligned), TerminalPasswordInput, EyeIcon, comprehensive preview

## Decisions Made
- Removed `leadingIcon` parameter: Pencil design has no arbitrary leading icon, only the ">" terminal prefix
- Used literal `6.dp` for label-to-input gap since Pencil specifies 6dp which is not a standard token (gap tokens are 4, 8, 12, 16, 24)
- Combined Task 1 and Task 2 into a single commit since both modify the same file and Task 2 depends directly on Task 1 changes

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

None.

## User Setup Required

None - no external service configuration required.

## Next Steps
- TerminalInput and TerminalPasswordInput are ready for use in auth screens (login/register forms)
- Password input can be verified visually via desktop preview

## Self-Check: PASSED

- FOUND: `app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/input/TerminalInput.kt`
- FOUND: commit `8a93f6d`
- BUILD: `:app:designsystem:compileKotlinJvm` SUCCESS

---
*Quick Task: quick-02*
*Completed: 2026-02-12*
