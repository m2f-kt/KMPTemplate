---
phase: quick-3
plan: 01
type: execute
wave: 1
depends_on: []
files_modified:
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalColors.kt
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/button/TerminalButton.kt
autonomous: true
must_haves:
  truths:
    - "Primary button has dark gray background (#525252 light, #D4D4D4 dark), NOT green accent"
    - "Success variant exists with green success colors"
    - "Disabled state uses specific disabled colors (not just alpha reduction)"
    - "Hover state changes button background on desktop"
    - "Default variant has 16dp horizontal padding; all others have 12dp"
    - "Button label uses fontWeight Medium (500)"
    - "Icon button uses Secondary styling (surface bg, border)"
  artifacts:
    - path: "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalColors.kt"
      provides: "Button-specific color tokens for all 6 variants (light + dark)"
      contains: "btnPrimaryBg"
    - path: "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/button/TerminalButton.kt"
      provides: "TerminalButton with 5 variants + disabled + hover + correct padding/typography"
      contains: "collectIsHoveredAsState"
  key_links:
    - from: "TerminalButton.kt"
      to: "TerminalColors.kt"
      via: "TerminalTheme.colors.btnPrimaryBg etc."
      pattern: "colors\\.btn"
---

<objective>
Align TerminalButton with the Pencil design system by fixing the primary color (dark gray not green),
adding Success variant, adding explicit disabled styling, implementing hover states, and correcting
padding and typography.

Purpose: TerminalButton currently uses the wrong primary color (green accent instead of dark gray)
and is missing Success/Disabled variants and hover states that the Pencil design specifies.

Output: Updated TerminalColors.kt with button tokens, rewritten TerminalButton.kt matching Pencil spec.
</objective>

<execution_context>
@./.claude/get-shit-done/workflows/execute-plan.md
@./.claude/get-shit-done/templates/summary.md
</execution_context>

<context>
@.planning/STATE.md
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalColors.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/button/TerminalButton.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalTheme.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalTypography.kt
</context>

<tasks>

<task type="auto">
  <name>Task 1: Add button color tokens to TerminalColors</name>
  <files>app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalColors.kt</files>
  <action>
Add 16 button-specific color properties to the `TerminalColors` data class, the `LocalTerminalColors`
default instance, `TerminalLightColors`, and `TerminalDarkColors`. These are button-specific tokens
that do NOT map to existing general-purpose tokens.

New properties to add (after the existing `infoBg` property):

```
// Button tokens
val btnPrimaryBg: Color,
val btnPrimaryText: Color,
val btnPrimaryHoverBg: Color,
val btnSecondaryBg: Color,
val btnSecondaryText: Color,
val btnSecondaryBorder: Color,
val btnSecondaryHoverBg: Color,
val btnGhostText: Color,
val btnGhostHoverBg: Color,
val btnDestructiveBg: Color,
val btnDestructiveText: Color,
val btnDestructiveHoverBg: Color,
val btnSuccessBg: Color,
val btnSuccessText: Color,
val btnSuccessHoverBg: Color,
val btnDisabledBg: Color,
val btnDisabledText: Color,
val btnDisabledBorder: Color,
```

Values for LocalTerminalColors default: all `Color.Unspecified`.

LIGHT values (from Pencil design tokens):
- btnPrimaryBg = Color(0xFF525252), btnPrimaryText = Color(0xFFFAFAFA), btnPrimaryHoverBg = Color(0xFF3A3A3A)
- btnSecondaryBg = Color(0xFFFFFFFF), btnSecondaryText = Color(0xFF171717), btnSecondaryBorder = Color(0xFFD4D4D4), btnSecondaryHoverBg = Color(0xFFF5F5F5)
- btnGhostText = Color(0xFF525252), btnGhostHoverBg = Color(0xFFF5F5F5)
- btnDestructiveBg = Color(0xFFF0E8E8), btnDestructiveText = Color(0xFF9A4444), btnDestructiveHoverBg = Color(0xFFE8DADA)
- btnSuccessBg = Color(0xFFE8F0EA), btnSuccessText = Color(0xFF3D7A4A), btnSuccessHoverBg = Color(0xFFD8E8DE)
- btnDisabledBg = Color(0xFFF5F5F5), btnDisabledText = Color(0xFFA3A3A3), btnDisabledBorder = Color(0xFFE5E5E5)

DARK values (from Pencil design tokens):
- btnPrimaryBg = Color(0xFFD4D4D4), btnPrimaryText = Color(0xFF171717), btnPrimaryHoverBg = Color(0xFFE5E5E5)
- btnSecondaryBg = Color(0xFF171717), btnSecondaryText = Color(0xFFE5E5E5), btnSecondaryBorder = Color(0xFF333333), btnSecondaryHoverBg = Color(0xFF262626)
- btnGhostText = Color(0xFFA3A3A3), btnGhostHoverBg = Color(0xFF262626)
- btnDestructiveBg = Color(0xFF251A1A), btnDestructiveText = Color(0xFFC06060), btnDestructiveHoverBg = Color(0xFF301F1F)
- btnSuccessBg = Color(0xFF1A231C), btnSuccessText = Color(0xFF5A9E6A), btnSuccessHoverBg = Color(0xFF1F3028)
- btnDisabledBg = Color(0xFF1C1C1C), btnDisabledText = Color(0xFF525252), btnDisabledBorder = Color(0xFF2A2A2A)

Preserve all existing properties and values exactly as-is. Only append the new button token properties.
  </action>
  <verify>
Project compiles: `cd /Users/marc/IdeaProjects/Template && ./gradlew :app:designsystem:compileKotlinJvm --quiet 2>&1 | tail -5`
Verify the new properties exist: grep for `btnPrimaryBg` in TerminalColors.kt.
  </verify>
  <done>TerminalColors has 18 button-specific color properties with correct Light/Dark hex values from Pencil design tokens.</done>
</task>

<task type="auto">
  <name>Task 2: Rewrite TerminalButton with Pencil-accurate variants, hover, padding, and typography</name>
  <files>app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/button/TerminalButton.kt</files>
  <action>
Rewrite TerminalButton.kt with these changes:

**1. Enum change:**
Change `ButtonVariant` to: `Default, Secondary, Ghost, Destructive, Success`
(Add Success, keep the rest. Disabled is NOT a variant -- handled via `enabled` param.)

**2. Hover support:**
Add these imports:
- `androidx.compose.foundation.interaction.MutableInteractionSource`
- `androidx.compose.foundation.interaction.collectIsHoveredAsState`
- `androidx.compose.foundation.hoverable`
- `androidx.compose.runtime.remember`
- `androidx.compose.runtime.getValue`

In TerminalButton, create:
```kotlin
val interactionSource = remember { MutableInteractionSource() }
val isHovered by interactionSource.collectIsHoveredAsState()
```

**3. Color resolution in TerminalButton:**
Replace the `when (variant)` block. Each variant now resolves `backgroundColor`, `textColor`,
`hasBorder`, `borderColor`, and `hoverBg`:

- **Default**: bg=`colors.btnPrimaryBg`, text=`colors.btnPrimaryText`, hoverBg=`colors.btnPrimaryHoverBg`, NO border
- **Secondary**: bg=`colors.btnSecondaryBg`, text=`colors.btnSecondaryText`, hoverBg=`colors.btnSecondaryHoverBg`, border=`colors.btnSecondaryBorder`
- **Ghost**: bg=`Color.Transparent`, text=`colors.btnGhostText`, hoverBg=`colors.btnGhostHoverBg`, NO border
- **Destructive**: bg=`colors.btnDestructiveBg`, text=`colors.btnDestructiveText`, hoverBg=`colors.btnDestructiveHoverBg`, NO border
- **Success**: bg=`colors.btnSuccessBg`, text=`colors.btnSuccessText`, hoverBg=`colors.btnSuccessHoverBg`, NO border

When `enabled == false` (regardless of variant), override to:
bg=`colors.btnDisabledBg`, text=`colors.btnDisabledText`, border=`colors.btnDisabledBorder` (always show border), hoverBg=`colors.btnDisabledBg` (no hover change).

Final resolved bg: `if (isHovered && enabled) hoverBg else backgroundColor`

**4. Remove alpha-based disabled styling:**
Delete the `alpha(alphaValue)` modifier. Disabled now uses specific disabled colors, not opacity.

**5. Padding fix:**
- Default variant: `horizontal = 16.dp, vertical = 8.dp`
- All other variants (Secondary, Ghost, Destructive, Success): `horizontal = 12.dp, vertical = 8.dp`
- When disabled: use the padding of the original variant (not a fixed value)

Replace `spacing.md` / `spacing.sm` with literal dp values since the design tokens (16/12/8)
don't map cleanly to existing spacing tokens. Use:
```kotlin
val horizontalPadding = if (variant == ButtonVariant.Default) 16.dp else 12.dp
```

**6. Typography fix:**
Replace `typography.sm` with `typography.sm.copy(fontWeight = FontWeight.Medium)` for the button
label text. Import `FontWeight` from `androidx.compose.ui.text.font.FontWeight`.
The sm style is fontSize=12 which matches the design's --btn-font-size: 12.
FontWeight.Medium = 500 which matches the design. The font family doesn't include a Medium weight
file, but Compose will interpolate between Regular (400) and SemiBold (600).

**7. Hoverable modifier:**
Add `.hoverable(interactionSource)` to the Box modifier chain, before `.clickable(...)`.

**8. Clickable when disabled:**
When `enabled = false`, the `.clickable` should have `enabled = false` (already does this).
Also remove the `onClick` from disabled state or keep `enabled = false` which prevents clicks.

**9. Icon button color fix:**
In `TerminalIconButton`, use button-specific tokens instead of generic tokens:
- bg = if disabled: `colors.btnDisabledBg` else `colors.btnSecondaryBg`
- border = if disabled: `colors.btnDisabledBorder` else `colors.btnSecondaryBorder`
- Add hover support (same pattern: MutableInteractionSource + collectIsHoveredAsState + hoverable)
- Hover bg = `colors.btnSecondaryHoverBg`
- Icon size stays controlled by the caller
- Padding stays `spacing.sm` (8dp, matches design's icon button padding=8 all sides)
- Remove alpha-based disabled styling for icon button too

**10. Update @Preview functions:**
Update `TerminalButtonPreview` to show all 5 variants + disabled state:
```kotlin
TerminalButton(text = "Default", onClick = {}, variant = ButtonVariant.Default)
TerminalButton(text = "Secondary", onClick = {}, variant = ButtonVariant.Secondary)
TerminalButton(text = "Ghost", onClick = {}, variant = ButtonVariant.Ghost)
TerminalButton(text = "Destructive", onClick = {}, variant = ButtonVariant.Destructive)
TerminalButton(text = "Success", onClick = {}, variant = ButtonVariant.Success)
TerminalButton(text = "Disabled", onClick = {}, enabled = false)
```

Keep the TerminalIconButtonPreview as-is (it already shows enabled + disabled states).

Follow existing preview pattern: `@Preview` annotation, `private` visibility, `TerminalTheme` wrapper,
`Column` with `colors.bg` background.
  </action>
  <verify>
Project compiles: `cd /Users/marc/IdeaProjects/Template && ./gradlew :app:designsystem:compileKotlinJvm --quiet 2>&1 | tail -5`
Verify Success variant exists: grep for `ButtonVariant.Success` in TerminalButton.kt.
Verify hover support exists: grep for `collectIsHoveredAsState` in TerminalButton.kt.
Verify padding fix: grep for `16.dp` in TerminalButton.kt.
Verify font weight fix: grep for `FontWeight.Medium` in TerminalButton.kt.
Verify no alpha disabled: grep should NOT find `alpha(alphaValue)` in TerminalButton.kt.
  </verify>
  <done>
TerminalButton uses Pencil-accurate colors from TerminalColors button tokens. Primary is dark gray (not green).
5 variants exist: Default, Secondary, Ghost, Destructive, Success. Disabled uses specific disabled colors
(not alpha). Hover states change background via InteractionSource. Default has 16dp horizontal padding,
others have 12dp. Label text uses FontWeight.Medium (500). Icon button uses Secondary styling with hover.
Preview shows all variants + disabled state.
  </done>
</task>

</tasks>

<verification>
- `./gradlew :app:designsystem:compileKotlinJvm` passes with no errors
- TerminalColors.kt has 18 new `btn*` properties with correct hex values for light and dark
- TerminalButton.kt ButtonVariant enum has 5 entries: Default, Secondary, Ghost, Destructive, Success
- No alpha-based disabled styling remains (disabled uses btnDisabled* colors)
- `collectIsHoveredAsState` + `hoverable` present in both TerminalButton and TerminalIconButton
- Default variant padding is 16dp horizontal, all others 12dp horizontal, all 8dp vertical
- Button label uses `FontWeight.Medium`
- Preview function renders all 5 variants + disabled
</verification>

<success_criteria>
TerminalButton matches the Pencil design specification: correct primary color (dark gray #525252/#D4D4D4),
5 variants with accurate colors, disabled state with specific disabled tokens, hover states,
correct padding per variant, Medium font weight. Compiles successfully.
</success_criteria>

<output>
After completion, create `.planning/quick/3-align-terminalbutton-with-pencil-design-/3-SUMMARY.md`
</output>
