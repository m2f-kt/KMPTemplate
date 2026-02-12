---
phase: quick-6
plan: 01
type: execute
wave: 1
depends_on: []
files_modified:
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalColors.kt
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/card/TerminalCard.kt
autonomous: true

must_haves:
  truths:
    - "TerminalColors has all 4 card-accent color tokens (cardAccentBg, cardAccentHeaderBg, cardAccentHeaderText, cardAccentBodyText)"
    - "CardVariant.Accent uses the new dedicated tokens instead of generic colors"
    - "Light and dark theme both define correct Pencil hex values for all 4 tokens"
  artifacts:
    - path: "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalColors.kt"
      provides: "4 new card-accent color properties in data class, LocalTerminalColors, TerminalLightColors, TerminalDarkColors"
      contains: "cardAccentBg"
    - path: "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/card/TerminalCard.kt"
      provides: "CardVariant.Accent using dedicated card-accent tokens"
      contains: "colors.cardAccentBg"
  key_links:
    - from: "TerminalCard.kt CardVariant.Accent"
      to: "TerminalColors.cardAccentBg, cardAccentHeaderBg, cardAccentHeaderText, cardAccentBodyText"
      via: "TerminalTheme.colors property access"
      pattern: "colors\\.cardAccent"
---

<objective>
Add 4 card-accent color tokens from the Pencil design system to TerminalColors and wire them into CardVariant.Accent.

Purpose: CardVariant.Accent currently uses wrong generic color tokens (bg, textMuted, surface) instead of the dedicated card-accent tokens defined in the Pencil design system. This causes incorrect visual rendering in both light and dark themes.

Output: TerminalColors with 4 new properties, correct light/dark values, and CardVariant.Accent using them.
</objective>

<execution_context>
@./.claude/get-shit-done/workflows/execute-plan.md
@./.claude/get-shit-done/templates/summary.md
</execution_context>

<context>
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalColors.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/card/TerminalCard.kt
</context>

<tasks>

<task type="auto">
  <name>Task 1: Add 4 card-accent color tokens to TerminalColors</name>
  <files>app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalColors.kt</files>
  <action>
Add 4 new color properties to the TerminalColors data class, after the existing button token block (after `btnDisabledBorder`). Add a comment line `// Card accent tokens` before them.

Properties to add to the data class:
```kotlin
// Card accent tokens
val cardAccentBg: Color,
val cardAccentHeaderBg: Color,
val cardAccentHeaderText: Color,
val cardAccentBodyText: Color,
```

Add corresponding `Color.Unspecified` defaults to the `LocalTerminalColors` `staticCompositionLocalOf` block, in the same position (after `btnDisabledBorder`):
```kotlin
cardAccentBg = Color.Unspecified,
cardAccentHeaderBg = Color.Unspecified,
cardAccentHeaderText = Color.Unspecified,
cardAccentBodyText = Color.Unspecified,
```

Add light theme values to `TerminalLightColors` (after `btnDisabledBorder`):
```kotlin
cardAccentBg = Color(0xFFE5E5E5),
cardAccentHeaderBg = Color(0xFF525252),
cardAccentHeaderText = Color(0xFFE8EBE4),
cardAccentBodyText = Color(0xFF525252),
```

Add dark theme values to `TerminalDarkColors` (after `btnDisabledBorder`):
```kotlin
cardAccentBg = Color(0xFF262626),
cardAccentHeaderBg = Color(0xFFD4D4D4),
cardAccentHeaderText = Color(0xFF171717),
cardAccentBodyText = Color(0xFFA3A3A3),
```
  </action>
  <verify>Build compiles: run the Gradle compileKotlin task or equivalent for the designsystem module. All 4 new properties exist in the data class and both theme objects.</verify>
  <done>TerminalColors data class has cardAccentBg, cardAccentHeaderBg, cardAccentHeaderText, cardAccentBodyText. LocalTerminalColors defaults, TerminalLightColors, and TerminalDarkColors all include the 4 new tokens with correct hex values matching the Pencil design system.</done>
</task>

<task type="auto">
  <name>Task 2: Wire CardVariant.Accent to use new card-accent tokens</name>
  <files>app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/card/TerminalCard.kt</files>
  <action>
In the `TerminalCard` composable, update the `CardVariant.Accent` branch of the `when` block (lines 96-106) to use the new dedicated tokens instead of the current wrong generic ones.

Replace the entire Accent branch with:
```kotlin
CardVariant.Accent -> {
    containerBg = colors.cardAccentBg
    borderColor = Color.Transparent
    hasBorder = false
    headerBg = colors.cardAccentHeaderBg
    titleColor = colors.cardAccentHeaderText
    titleWeight = FontWeight.Medium
    descriptionColor = colors.cardAccentHeaderText.copy(alpha = 0.7f)
    iconTint = colors.cardAccentHeaderText
    hasHeaderBottomBorder = false
}
```

Also update the preview composable's Accent Card icon tint (around line 420) to use the new header text token instead of `colors.surface`:
```kotlin
color = TerminalTheme.colors.cardAccentHeaderText,
```
  </action>
  <verify>Build compiles. Visually inspect the preview (or run it) to confirm the Accent card now uses the dedicated card-accent tokens. Grep for `colors.surface` and `colors.textMuted` and `colors.bg` in the Accent branch -- none should remain.</verify>
  <done>CardVariant.Accent uses colors.cardAccentBg for container, colors.cardAccentHeaderBg for header background, colors.cardAccentHeaderText for title/description/icon tint. No generic fallback tokens remain in the Accent branch. Preview icon tint updated to match.</done>
</task>

</tasks>

<verification>
1. `grep -n "cardAccent" TerminalColors.kt` shows all 4 tokens in data class, LocalTerminalColors, TerminalLightColors, and TerminalDarkColors (16 matches total: 4 per location)
2. `grep -n "cardAccent" TerminalCard.kt` shows the 4 tokens used in CardVariant.Accent branch
3. No references to `colors.bg`, `colors.textMuted`, or `colors.surface` remain in the Accent branch
4. Gradle build compiles without errors
</verification>

<success_criteria>
- TerminalColors.kt contains 4 new card-accent token properties with correct light (#E5E5E5, #525252, #E8EBE4, #525252) and dark (#262626, #D4D4D4, #171717, #A3A3A3) values
- CardVariant.Accent in TerminalCard.kt references only the new cardAccent* tokens
- Project compiles cleanly
</success_criteria>

<output>
After completion, create `.planning/quick/6-add-card-accent-bg-color-token-for-cardv/6-SUMMARY.md`
</output>
