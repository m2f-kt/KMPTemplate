---
phase: quick-20
plan: 01
subsystem: ui
tags: [compose, indication, ripple, animation, foundation, multiplatform]

# Dependency graph
requires:
  - phase: 04-02
    provides: TerminalTheme design system foundation with color tokens
provides:
  - TerminalRippleIndication (IndicationNodeFactory) for custom ripple effect
  - rememberTerminalRipple() composable for theme-aware ripple creation
  - Press feedback on all clickable design system components
affects: [design-system, components]

# Tech tracking
tech-stack:
  added: []
  patterns: [IndicationNodeFactory for custom press feedback, bounded vs unbounded ripple for touch target size]

key-files:
  created:
    - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalRipple.kt
  modified:
    - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/button/TerminalButton.kt
    - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalList.kt
    - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalDropdownMenu.kt
    - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/card/TerminalCard.kt
    - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/selection/TerminalCheckbox.kt
    - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/selection/TerminalSwitch.kt
    - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/selection/TerminalRadio.kt
    - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalTable.kt

key-decisions:
  - "Used IndicationNodeFactory (modern API) instead of deprecated Indication interface for CMP 1.10.1 compatibility"
  - "bounded=true for buttons/cards/list items; bounded=false for checkbox/switch/radio (small touch targets)"
  - "Removed .hoverable() from TerminalButton, TerminalIconButton, TerminalDropdownMenuItem -- .clickable() with interactionSource handles hover internally"
  - "Theme text color at 0.12f alpha as default ripple color (subtle terminal aesthetic)"

patterns-established:
  - "IndicationNodeFactory: modern Modifier.Node-based approach for custom press indication in CMP"
  - "Bounded vs unbounded ripple: bounded for components with visible bounds, unbounded for small toggles"
  - ".clickable(interactionSource, indication): replaces .hoverable().clickable() pattern for components needing both hover and press feedback"

# Metrics
duration: 4min
completed: 2026-02-13
---

# Quick Task 20: Add Ripple Click Effect Summary

**Custom TerminalRippleIndication using IndicationNodeFactory with 300ms expand / 200ms fade-out wired into all 9 clickable components**

## Performance

- **Duration:** 4 min
- **Started:** 2026-02-13T00:22:28Z
- **Completed:** 2026-02-13T00:26:27Z
- **Tasks:** 2
- **Files modified:** 9

## Accomplishments
- Created TerminalRippleIndication implementing IndicationNodeFactory (modern Compose Foundation API)
- Ripple animates: circle expands from press point over 300ms at 0.12f alpha, fades out over 200ms on release
- Wired ripple into all 9 clickable components: TerminalButton, TerminalIconButton, TerminalListItem, TerminalDropdownMenuItem, TerminalCompactCard, TerminalCheckbox, TerminalTableCheckbox, TerminalSwitch, TerminalRadio
- No Material dependency -- purely Foundation-based implementation

## Task Commits

Each task was committed atomically:

1. **Task 1: Create TerminalRippleIndication using Foundation IndicationNodeFactory API** - `1fbc7f8` (feat)
2. **Task 2: Wire ripple indication into all clickable design system components** - `16c8989` (feat)

## Files Created/Modified
- `app/designsystem/.../theme/TerminalRipple.kt` - TerminalRippleIndication (IndicationNodeFactory), TerminalRippleNode (DrawModifierNode), rememberTerminalRipple()
- `app/designsystem/.../button/TerminalButton.kt` - TerminalButton + TerminalIconButton: .clickable() with interactionSource + ripple, removed .hoverable()
- `app/designsystem/.../data/TerminalList.kt` - TerminalListItem: bounded ripple on main click, unbounded on ellipsis menu trigger
- `app/designsystem/.../data/TerminalDropdownMenu.kt` - TerminalDropdownMenuItem: .clickable() with interactionSource + ripple, removed .hoverable()
- `app/designsystem/.../card/TerminalCard.kt` - TerminalCompactCard: bounded ripple on conditional click
- `app/designsystem/.../selection/TerminalCheckbox.kt` - Unbounded ripple on triStateToggleable
- `app/designsystem/.../selection/TerminalSwitch.kt` - Unbounded ripple on toggleable
- `app/designsystem/.../selection/TerminalRadio.kt` - Unbounded ripple on selectable
- `app/designsystem/.../data/TerminalTable.kt` - TerminalTableCheckbox: unbounded ripple on triStateToggleable

## Decisions Made
- Used IndicationNodeFactory (modern API) instead of deprecated Indication + rememberUpdatedInstance for CMP 1.10.1
- Removed separate `.hoverable(interactionSource)` from TerminalButton, TerminalIconButton, TerminalDropdownMenuItem because `.clickable()` with interactionSource already handles hoverable internally
- Used bounded=false for checkbox, switch, radio, and ellipsis trigger (standard UX for small touch targets)
- Default ripple color: theme text color at 0.12f alpha (subtle, matches terminal aesthetic)
- TerminalAlert dismiss button and TerminalInput password toggle intentionally left without ripple (not in plan scope; Input already has indication=null)

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

None.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- All clickable design system components now have press feedback
- Ripple effect is cross-platform (Android, iOS, Desktop, WASM)
- No new dependencies introduced

---
*Quick task: 20-add-ripple-click-effect-to-all-clickable*
*Completed: 2026-02-13*
