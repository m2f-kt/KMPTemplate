# Quick Task 13: Fix white corner artifacts on TerminalDropdownMenu

## Result: COMPLETE

## Changes

| File | Change |
|------|--------|
| `TerminalDropdownMenu.kt` | Removed `.shadow()` modifier from composable and preview; removed unused import and variable |

## Root Cause

The `.shadow()` Compose modifier draws a white-filled rectangle as the shadow base. With `RoundedCornerShape`, the white fill peeks through the rounded corners before clipping takes effect, creating visible white triangle artifacts in the corners.

## Fix

Removed `.shadow(shadows.sm.blur, shape)` from both:
1. The `TerminalDropdownMenu` composable (line 63)
2. The `TerminalDropdownMenuPreview` preview Box (line 137)

Also cleaned up the unused `import androidx.compose.ui.draw.shadow` and `val shadows = TerminalTheme.shadows` references.

## Commits

| Hash | Message |
|------|---------|
| `50dde46` | fix(quick-13): remove shadow causing white corner artifacts on TerminalDropdownMenu |
