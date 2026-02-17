---
phase: quick-6
plan: 01
subsystem: ui
tags: [compose, design-tokens, color-system, pencil-design]

# Dependency graph
requires:
  - phase: quick-1
    provides: "TerminalCard with CardVariant.Accent using generic color tokens"
  - phase: 04-02
    provides: "TerminalColors data class with theme token infrastructure"
provides:
  - "4 card-accent color tokens (cardAccentBg, cardAccentHeaderBg, cardAccentHeaderText, cardAccentBodyText)"
  - "CardVariant.Accent using dedicated Pencil design tokens instead of generic fallbacks"
affects: [designsystem, card-components]

# Tech tracking
tech-stack:
  added: []
  patterns: ["Dedicated per-component color tokens for variant-specific styling"]

key-files:
  created: []
  modified:
    - "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalColors.kt"
    - "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/card/TerminalCard.kt"

key-decisions:
  - "cardAccentBodyText token added for future body text usage even though current Accent branch only uses header text tokens"

patterns-established:
  - "Per-variant color tokens: component variants get dedicated color tokens instead of reusing generic theme colors"

# Metrics
duration: 3min
completed: 2026-02-12
---

# Quick Task 6: Add Card-Accent Color Tokens Summary

**4 dedicated card-accent color tokens added to TerminalColors and wired into CardVariant.Accent with correct Pencil light/dark hex values**

## Performance

- **Duration:** ~3 min
- **Started:** 2026-02-12T17:21:04Z
- **Completed:** 2026-02-12T17:23:41Z
- **Tasks:** 2
- **Files modified:** 2

## Accomplishments
- Added cardAccentBg, cardAccentHeaderBg, cardAccentHeaderText, cardAccentBodyText to TerminalColors data class with Color.Unspecified defaults
- Set correct Pencil light theme values (#E5E5E5, #525252, #E8EBE4, #525252) and dark theme values (#262626, #D4D4D4, #171717, #A3A3A3)
- Replaced all generic token usage (colors.bg, colors.textMuted, colors.surface) in CardVariant.Accent with dedicated cardAccent* tokens
- Updated preview composable icon tint from colors.surface to colors.cardAccentHeaderText

## Task Commits

Each task was committed atomically:

1. **Task 1: Add 4 card-accent color tokens to TerminalColors** - `ab159db` (feat)
2. **Task 2: Wire CardVariant.Accent to use new card-accent tokens** - `0601e7a` (feat)

## Files Created/Modified
- `app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalColors.kt` - 4 new card-accent color properties in data class, LocalTerminalColors defaults, TerminalLightColors, TerminalDarkColors
- `app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/card/TerminalCard.kt` - CardVariant.Accent branch and preview icon tint updated to use cardAccent* tokens

## Decisions Made
- cardAccentBodyText token included in TerminalColors for completeness (matches Pencil design spec) even though current Accent branch only uses cardAccentHeaderText for title/description/icon -- body text color will be available when card content styling needs it

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered
- Pre-existing Gradle build failure (ui-tooling dependency not resolving for iOS/wasmJs targets) prevented full compilation check. This issue predates quick-6 changes and is unrelated to the color token additions. Verified changes are correct via grep verification and diff inspection.

## User Setup Required

None - no external service configuration required.

## Next Steps
- Consider using cardAccentBodyText in the TerminalCard content slot for Accent variant body text coloring
- The pre-existing ui-tooling dependency issue should be addressed separately

---
*Phase: quick-6*
*Completed: 2026-02-12*
