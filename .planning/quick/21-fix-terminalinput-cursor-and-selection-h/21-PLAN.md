# Plan: Fix TerminalInput cursor and selection handle colors

## Goal
Fix the black cursor and blue selection handles in TerminalInput to use theme-appropriate colors.

## Tasks

### Task 1: Add cursorBrush and TextSelectionColors to TerminalInput
**File:** `app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/input/TerminalInput.kt`

1. Add `cursorBrush = SolidColor(colors.text)` to BasicTextField — cursor uses text color
2. Wrap BasicTextField with `CompositionLocalProvider(LocalTextSelectionColors provides ...)` — selection handles use accent color, selection background uses accent at 30% alpha
