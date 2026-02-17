---
phase: quick-01
plan: 01
subsystem: ui
tags: [compose, card, design-system, foundation, pencil]

# Dependency graph
requires:
  - phase: 04-02
    provides: TerminalTheme tokens (colors, spacing, gap, radius, borders, typography)
provides:
  - TerminalCard with header/content/footer structure (4 vertical variants)
  - TerminalCompactCard with horizontal Row layout
  - CardVariant enum (Default, Accent, Info, Highlighted, Compact)
  - 5-variant preview with realistic content
affects: [phase-05, design-system, card-consumers]

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "drawBehind for section divider lines (header bottom border, footer top border)"
    - "Separate composable for fundamentally different layout (TerminalCompactCard vs TerminalCard)"
    - "Private CardHeader/CardContent/CardFooter internal composables for card structure"

key-files:
  modified:
    - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/card/TerminalCard.kt

key-decisions:
  - "TerminalCompactCard is a separate composable (not a variant flag on TerminalCard) because it has a fundamentally different horizontal Row layout"
  - "Accent variant uses colors.textMuted for header bg (maps to #5A5A5A, closest to Pencil #525252 dark header)"
  - "borders.default (2dp) used for Highlighted variant border width, borders.thin (1dp) for all others"
  - "CardVariant.Compact kept in enum for backward compatibility but TerminalCard renders defaults if passed Compact"

patterns-established:
  - "Card header/content/footer structure with per-variant styling derived from TerminalTheme tokens"
  - "drawBehind modifier for thin section borders without Material3 Divider"

# Metrics
duration: 2min
completed: 2026-02-12
---

# Quick Task 1: Align TerminalCard with Pencil Design Summary

**Redesigned TerminalCard with proper header/content/footer structure: Default (with footer), Accent (dark header), Info (colored header), Highlighted (accent border + SemiBold), and separate TerminalCompactCard (horizontal Row)**

## Performance

- **Duration:** 2 min
- **Started:** 2026-02-12T13:12:41Z
- **Completed:** 2026-02-12T13:14:49Z
- **Tasks:** 2
- **Files modified:** 1

## Accomplishments
- Complete rewrite of TerminalCard with header (title + description + icon), content slot, and optional footer
- Default variant renders header with bottom border + content + footer with top border (end-aligned buttons)
- Accent variant renders dark-background header (textMuted bg, surface text) + content -- NO left edge, NO footer
- Info variant renders infoBg-colored header + content -- NO footer
- Highlighted variant renders accentMuted bg, 2dp accent border, SemiBold title + content -- NO footer
- TerminalCompactCard renders as horizontal Row: leading icon, title+details column, trailing icon
- Comprehensive 5-variant preview with realistic terminal-themed content

## Task Commits

Both tasks shipped in a single commit (same file, plan required complete rewrite):

1. **Task 1: Rewrite TerminalCard with header/content/footer structure** - `4516ed7` (feat)
2. **Task 2: Add TerminalCompactCard and 5-variant preview** - `4516ed7` (feat, same commit)

## Files Created/Modified
- `app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/card/TerminalCard.kt` - Complete rewrite: TerminalCard (4 variants), TerminalCompactCard, CardHeader/CardContent/CardFooter private composables, 5-variant preview

## Decisions Made
- TerminalCompactCard is a separate composable because its horizontal Row layout is fundamentally different from the vertical header/content/footer Column
- Accent variant uses `colors.textMuted` (#5A5A5A) for header background as the closest token to the Pencil dark header (#525252)
- `borders.default` (2dp) used specifically for Highlighted variant; all others use `borders.thin` (1dp)
- Footer param is accepted by all variants but only rendered for Default (matches Pencil design intent)

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

- Gradle task `compileKotlinDesktop` does not exist; used `compileKotlinJvm` instead (designsystem module has JVM target, not Desktop)

## User Setup Required

None - no external service configuration required.

## Next Steps
- Card component is now aligned with Pencil design; future components can follow the same header/content/footer pattern
- Preview is ready for visual verification in Android Studio / Fleet

## Self-Check: PASSED

- [x] TerminalCard.kt exists at expected path
- [x] 1-SUMMARY.md created
- [x] Commit 4516ed7 verified in git log

---
*Quick Task: 1-align-design-system-components-with-penc*
*Completed: 2026-02-12*
