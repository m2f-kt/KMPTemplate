# Summary: Fix TerminalInput cursor and selection handle colors

## What changed
- **Cursor color**: Added `cursorBrush = SolidColor(colors.text)` to BasicTextField — cursor now uses the theme text color instead of default black
- **Selection handles**: Wrapped BasicTextField with `CompositionLocalProvider(LocalTextSelectionColors provides selectionColors)` — handles use `colors.accent` instead of default blue
- **Selection highlight**: Background highlight uses `colors.accent.copy(alpha = 0.3f)` for themed text selection

## Files modified
- `app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/input/TerminalInput.kt`

## New imports
- `SolidColor` — for cursor brush
- `TextSelectionColors`, `LocalTextSelectionColors` — for selection handle/highlight colors
- `CompositionLocalProvider` — to provide selection colors
