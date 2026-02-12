---
phase: quick-4
plan: 01
subsystem: ui
tags: [compose-preview, annotation, design-system, multiplatform]

# Dependency graph
requires:
  - phase: 04-navigation-ui
    provides: "All 17 designsystem component files with @Preview annotations"
provides:
  - "@TerminalPreview multi-mode annotation (Light, Dark, Desktop) in theme package"
  - "All 17 component files migrated to @TerminalPreview"
affects: [designsystem, any future component files]

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "Custom multi-mode @TerminalPreview annotation instead of bare @Preview"
    - "All preview functions generate 3 variants: Light Mode, Dark Mode, Desktop (1024dp)"

key-files:
  created:
    - "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalPreview.kt"
  modified:
    - "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/button/TerminalButton.kt"
    - "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/TerminalText.kt"
    - "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/card/TerminalCard.kt"
    - "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/input/TerminalInput.kt"
    - "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/input/TerminalTextarea.kt"
    - "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/feedback/TerminalAlert.kt"
    - "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/feedback/TerminalBadge.kt"
    - "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/feedback/TerminalProgress.kt"
    - "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/feedback/TerminalTooltip.kt"
    - "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/display/TerminalAvatar.kt"
    - "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/display/TerminalDivider.kt"
    - "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/display/TerminalKbd.kt"
    - "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/selection/TerminalCheckbox.kt"
    - "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/selection/TerminalRadio.kt"
    - "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/selection/TerminalSwitch.kt"
    - "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalList.kt"
    - "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalTable.kt"

key-decisions:
  - "Used AndroidUiModes for uiMode constants (consistent with existing commonMain usage in TerminalButton)"
  - "Annotation lives in theme package alongside TerminalTheme (design-system-level concern)"
  - "widthDp=1024 for Desktop preview to simulate wide-screen/landscape layout"

patterns-established:
  - "@TerminalPreview annotation: all new preview functions MUST use @TerminalPreview, not bare @Preview"

# Metrics
duration: 4min
completed: 2026-02-12
---

# Quick Task 4: Create Custom Multi-mode Preview Annotation Summary

**@TerminalPreview annotation stacking Light, Dark, and Desktop (1024dp) previews -- migrated all 17 component files, zero bare @Preview remaining**

## Performance

- **Duration:** 4 min
- **Started:** 2026-02-12T15:56:37Z
- **Completed:** 2026-02-12T16:00:50Z
- **Tasks:** 2
- **Files modified:** 18 (1 created + 17 modified)

## Accomplishments
- Created `@TerminalPreview` annotation combining Light Mode, Dark Mode, and Desktop (widthDp=1024) previews
- Migrated all 17 designsystem component files from `@Preview` to `@TerminalPreview`
- Eliminated all bare `@Preview` annotations and old `Preview`/`AndroidUiModes` imports from components directory
- Verified compilation passes on JVM target

## Task Commits

Each task was committed atomically:

1. **Task 1: Create @TerminalPreview multi-mode annotation** - `3c47582` (feat)
2. **Task 2: Replace all @Preview with @TerminalPreview across 17 files** - `045f538` (refactor)

## Files Created/Modified
- `theme/TerminalPreview.kt` - New multi-mode preview annotation (3 stacked @Preview: Light, Dark, Desktop)
- `components/button/TerminalButton.kt` - Replaced 2 stacked @Preview + 1 bare @Preview with 2x @TerminalPreview
- `components/TerminalText.kt` - Replaced @Preview with @TerminalPreview
- `components/card/TerminalCard.kt` - Replaced @Preview with @TerminalPreview
- `components/input/TerminalInput.kt` - Replaced @Preview with @TerminalPreview
- `components/input/TerminalTextarea.kt` - Replaced @Preview with @TerminalPreview
- `components/feedback/TerminalAlert.kt` - Replaced @Preview with @TerminalPreview
- `components/feedback/TerminalBadge.kt` - Replaced @Preview with @TerminalPreview
- `components/feedback/TerminalProgress.kt` - Replaced @Preview with @TerminalPreview
- `components/feedback/TerminalTooltip.kt` - Replaced @Preview with @TerminalPreview
- `components/display/TerminalAvatar.kt` - Replaced @Preview with @TerminalPreview
- `components/display/TerminalDivider.kt` - Replaced @Preview with @TerminalPreview
- `components/display/TerminalKbd.kt` - Replaced @Preview with @TerminalPreview
- `components/selection/TerminalCheckbox.kt` - Replaced @Preview with @TerminalPreview
- `components/selection/TerminalRadio.kt` - Replaced @Preview with @TerminalPreview
- `components/selection/TerminalSwitch.kt` - Replaced @Preview with @TerminalPreview
- `components/data/TerminalList.kt` - Replaced @Preview with @TerminalPreview
- `components/data/TerminalTable.kt` - Replaced @Preview with @TerminalPreview

## Decisions Made
- Used `AndroidUiModes` import for `UI_MODE_NIGHT_NO`/`UI_MODE_NIGHT_YES` constants (consistent with existing commonMain usage in TerminalButton.kt, avoids direct android.content.res.Configuration reference)
- Placed `TerminalPreview.kt` in `theme` package alongside `TerminalTheme.kt` -- it is a design-system-level concern
- Used `widthDp = 1024` for the Desktop preview to simulate a wide-screen/landscape layout

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered
None.

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- All future designsystem component previews should use `@TerminalPreview` instead of bare `@Preview`
- The annotation is available at `com.m2f.template.designsystem.theme.TerminalPreview`

## Self-Check: PASSED

- FOUND: TerminalPreview.kt
- FOUND: 4-SUMMARY.md
- FOUND: 3c47582 (Task 1 commit)
- FOUND: 045f538 (Task 2 commit)

---
*Quick task: quick-4*
*Completed: 2026-02-12*
