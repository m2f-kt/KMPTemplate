---
phase: 04-navigation-ui-components
plan: 02
subsystem: ui
tags: [compose, compositionlocal, theme, jetbrains-mono, design-system, kmp]

# Dependency graph
requires:
  - phase: 01-foundation
    provides: "kmp-library-convention plugin, module structure patterns"
provides:
  - "app:designsystem KMP module with TerminalTheme system"
  - "8 @Immutable theme data classes with staticCompositionLocalOf"
  - "Light/dark color palettes matching Pencil design tokens"
  - "JetBrains Mono font loading via Compose Resources"
  - "TerminalText helper composable wrapping BasicText"
  - "TerminalTheme composable and accessor object"
affects: [04-03, 04-04, 04-05, 05-navigation-ui-components]

# Tech tracking
tech-stack:
  added: [compose.components.resources, JetBrains Mono font]
  patterns: [CompositionLocal theme system, @Immutable data class tokens, @Composable Font() loading]

key-files:
  created:
    - "app/designsystem/build.gradle.kts"
    - "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalColors.kt"
    - "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalTypography.kt"
    - "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalSpacing.kt"
    - "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalGap.kt"
    - "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalShadows.kt"
    - "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalOpacity.kt"
    - "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalRadius.kt"
    - "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalBorders.kt"
    - "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalTheme.kt"
    - "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/TerminalText.kt"
  modified:
    - "settings.gradle.kts"
    - "composeApp/build.gradle.kts"
    - "app/auth/build.gradle.kts"
    - "app/dashboard/build.gradle.kts"

key-decisions:
  - "Custom TerminalShadow data class instead of CMP DropShadow (API availability uncertain in CMP 1.10.1)"
  - "isSystemInDarkTheme() imported from compose.foundation (no Material3 needed in designsystem module)"
  - "Removed compose.material3 from app:auth and app:dashboard (terminal design system replaces it)"
  - "Font files use underscore naming (JetBrainsMono_Regular.ttf) for Compose Resources accessor generation"

patterns-established:
  - "CompositionLocal theme pattern: @Immutable data class + staticCompositionLocalOf + TerminalTheme accessor object"
  - "@Composable terminalTypography() for font loading (Font() is @Composable in CMP)"
  - "TerminalText helper wraps BasicText with theme color/typography defaults"

# Metrics
duration: 8min
completed: 2026-02-12
---

# Phase 4 Plan 2: Design System Theme Summary

**Custom CompositionLocal theme system with 8 subsystems (colors, typography, spacing, gap, shadows, opacity, radius, borders), JetBrains Mono font loading, and TerminalText helper composable in app:designsystem module**

## Performance

- **Duration:** 8 min
- **Started:** 2026-02-12T00:26:52Z
- **Completed:** 2026-02-12T00:35:06Z
- **Tasks:** 3
- **Files modified:** 15

## Accomplishments
- Created standalone `app:designsystem` KMP module with Foundation + Compose Resources (no Material3)
- Implemented 8 @Immutable theme data classes with staticCompositionLocalOf, each providing design tokens
- Light and dark color palettes match Pencil design system tokens exactly (17 color properties each)
- JetBrains Mono font loaded via @Composable Font() API with Regular/SemiBold/Bold weights
- TerminalTheme composable provides all 8 CompositionLocals with automatic dark mode detection
- TerminalText helper wraps BasicText with theme-aware color and typography defaults
- Feature modules (app:auth, app:dashboard) updated to depend on designsystem instead of material3

## Task Commits

Each task was committed atomically:

1. **Task 1: Create app:designsystem module with font resources** - `818717b` (feat)
2. **Task 2: Implement all 8 theme data classes with CompositionLocals** - `c4c84e7` (feat, absorbed by parallel 04-01 commit)
3. **Task 3: Create TerminalTheme composable, accessor object, and TerminalText helper** - `0c45ede` (feat)

## Files Created/Modified
- `app/designsystem/build.gradle.kts` - KMP library module with compose.foundation + compose.components.resources
- `app/designsystem/src/commonMain/composeResources/font/JetBrainsMono_*.ttf` - 3 font weight files
- `app/designsystem/src/commonMain/kotlin/.../theme/TerminalColors.kt` - 17-property color system with light/dark instances
- `app/designsystem/src/commonMain/kotlin/.../theme/TerminalTypography.kt` - @Composable font loading with 5 text styles
- `app/designsystem/src/commonMain/kotlin/.../theme/TerminalSpacing.kt` - Spacing tokens (4/8/12/16/20 dp)
- `app/designsystem/src/commonMain/kotlin/.../theme/TerminalGap.kt` - Gap tokens (4/8/12/16/24 dp)
- `app/designsystem/src/commonMain/kotlin/.../theme/TerminalShadows.kt` - Custom TerminalShadow data class with sm/md/lg presets
- `app/designsystem/src/commonMain/kotlin/.../theme/TerminalOpacity.kt` - Opacity tokens (1.0/0.75/0.50/0.25)
- `app/designsystem/src/commonMain/kotlin/.../theme/TerminalRadius.kt` - Corner radius tokens (0/4/6/12/24/9999 dp)
- `app/designsystem/src/commonMain/kotlin/.../theme/TerminalBorders.kt` - Border width tokens (1/2/3 dp)
- `app/designsystem/src/commonMain/kotlin/.../theme/TerminalTheme.kt` - Theme composable + accessor object
- `app/designsystem/src/commonMain/kotlin/.../components/TerminalText.kt` - BasicText wrapper with theme defaults
- `settings.gradle.kts` - Added include("app:designsystem")
- `composeApp/build.gradle.kts` - Added designsystem dependency
- `app/auth/build.gradle.kts` - Replaced material3 with designsystem
- `app/dashboard/build.gradle.kts` - Replaced material3 with designsystem

## Decisions Made
- Used custom `TerminalShadow` data class instead of CMP `DropShadow` -- the DropShadow API availability in CMP 1.10.1 is uncertain, and a custom data class provides full control over shadow properties (blur, offsetX, offsetY, spread, color)
- `isSystemInDarkTheme()` imported from `compose.foundation` -- confirmed available without Material3 dependency
- Removed `compose.material3` from app:auth and app:dashboard since neither module uses Material3 composables; the terminal design system replaces Material3 entirely
- Font files renamed with underscores (`JetBrainsMono_Regular.ttf`) because Compose Resources accessor generation requires underscores, not hyphens

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered
- Task 2 theme files were absorbed into a parallel 04-01 plan commit (c4c84e7) due to concurrent execution -- files were correctly created and committed, just under a different commit message

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- TerminalTheme system is complete and ready for component implementation (04-03, 04-04, 04-05 plans)
- All 8 CompositionLocals available via `TerminalTheme.colors`, `.typography`, `.spacing`, `.gap`, `.shadows`, `.opacity`, `.radius`, `.borders`
- Feature modules can now import and use the design system

## Self-Check: PASSED

All 14 created files verified on disk. Commits 818717b and 0c45ede verified in git log. JVM and WasmJs compilation verified. 8 staticCompositionLocalOf declarations confirmed.

---
*Phase: 04-navigation-ui-components*
*Completed: 2026-02-12*
