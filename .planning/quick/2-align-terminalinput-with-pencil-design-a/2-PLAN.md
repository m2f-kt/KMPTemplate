---
phase: quick-02
plan: 01
type: execute
wave: 1
depends_on: []
files_modified:
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/input/TerminalInput.kt
autonomous: true

must_haves:
  truths:
    - "TerminalInput shows a '>' prefix that is textMuted when empty, success (green) when filled"
    - "TerminalInput border disappears when the field has content"
    - "TerminalInput uses correct spacing: padding 12v/16h, label gap 6dp, inner gap 8dp"
    - "TerminalInput uses typography.base (13sp) for input text and fontWeight Medium for labels"
    - "TerminalPasswordInput toggles between masked (eye-off) and visible (eye-open) states"
    - "Preview shows all 5 states: empty, filled, labeled, password masked, password visible"
  artifacts:
    - path: "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/input/TerminalInput.kt"
      provides: "TerminalInput + TerminalPasswordInput composables"
      contains: "TerminalPasswordInput"
  key_links:
    - from: "TerminalInput"
      to: "TerminalTheme.colors.success"
      via: "prefix color when value.isNotEmpty()"
      pattern: "colors\\.success"
    - from: "TerminalPasswordInput"
      to: "TerminalInput"
      via: "wraps TerminalInput with trailingIcon and visualTransformation"
      pattern: "TerminalInput\\("
---

<objective>
Align TerminalInput with the Pencil design specification and create TerminalPasswordInput.

Purpose: Current TerminalInput diverges from the Pencil design in 7 areas (prefix, border logic, padding, typography, label weight, gaps, inner layout). This plan fixes all mismatches and adds the password variant with eye toggle icon.

Output: Updated TerminalInput.kt with both composables and comprehensive preview.
</objective>

<execution_context>
@./.claude/get-shit-done/workflows/execute-plan.md
@./.claude/get-shit-done/templates/summary.md
</execution_context>

<context>
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/input/TerminalInput.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalTheme.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalTypography.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalColors.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalRadius.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalBorders.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/selection/TerminalCheckbox.kt
</context>

<tasks>

<task type="auto">
  <name>Task 1: Fix TerminalInput to match Pencil design</name>
  <files>app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/input/TerminalInput.kt</files>
  <action>
Rewrite the TerminalInput composable in-place to match the Pencil design spec exactly. All 7 divergences must be fixed:

**1. Add terminal ">" prefix inside the input field Row:**
- Render `BasicText(text = ">")` as the FIRST element inside the Row, before leadingIcon or innerTextField.
- Prefix color logic: `if (value.isNotEmpty()) colors.success else colors.textMuted`
- Prefix style: `typography.base.copy(fontWeight = FontWeight.Bold, color = prefixColor)` -- fontSize=13sp via typography.base, fontWeight=700 via FontWeight.Bold.

**2. Border disappears when filled:**
- Replace the always-present `.border(borders.thin, borderColor, shape)` with conditional logic.
- When `isError` -> always show border with `colors.error`.
- When `value.isEmpty()` (empty/unfilled) -> show border with `colors.border`.
- When `value.isNotEmpty()` and NOT error -> NO border modifier at all.
- Implementation: use `.then(if (showBorder) Modifier.border(borders.thin, borderColor, shape) else Modifier)` where `showBorder = isError || value.isEmpty()`.

**3. Fix padding to [12v, 16h]:**
- Change from `.padding(horizontal = spacing.md, vertical = spacing.sm)` (12h, 8v)
- To `.padding(horizontal = spacing.lg, vertical = spacing.md)` (16h, 12v).

**4. Fix input text typography to base (13sp):**
- Change `textStyle = typography.sm.copy(color = colors.text)` to `textStyle = typography.base.copy(color = colors.text)`.
- Change placeholder style from `typography.sm.copy(color = colors.textDim)` to `typography.base.copy(color = colors.textDim)`.

**5. Fix label fontWeight to Medium (500):**
- Change label style from `typography.xs.copy(color = colors.textMuted)` to `typography.xs.copy(color = colors.textMuted, fontWeight = FontWeight.Medium)`.

**6. Fix label-to-input gap from gap.xs (4dp) to 6.dp:**
- Replace `Spacer(modifier = Modifier.height(gap.xs))` after the label with `Spacer(modifier = Modifier.height(6.dp))`.
- 6dp is not a standard token value (gap tokens are 4, 8, 12, 16, 24), so use literal `6.dp`.

**7. Fix inner Row layout to use gap=8 via Arrangement.spacedBy:**
- Replace the current Row (which uses Spacer + padding for spacing between leadingIcon, content, trailingIcon) with `Row(horizontalArrangement = Arrangement.spacedBy(gap.sm), verticalAlignment = Alignment.CenterVertically)`.
- gap.sm = 8dp which matches Pencil's gap=8.
- Remove the `Spacer(modifier = Modifier.padding(end = gap.sm))` before leadingIcon content and `Spacer(modifier = Modifier.padding(start = gap.sm))` after trailing icon. The Arrangement.spacedBy handles all gaps automatically.
- The ">" prefix is a separate element in the Row, then leadingIcon (if any), then the content Box(weight=1f), then trailingIcon (if any).

**8. Fix corner radius from radius.md (6dp) to radius.sm (4dp):**
- Pencil specifies cornerRadius=4. Change `RoundedCornerShape(radius.md)` to `RoundedCornerShape(radius.sm)`.

**Additional required parameter for Task 2 integration:**
- Add a `visualTransformation` parameter: `visualTransformation: VisualTransformation = VisualTransformation.None`.
- Pass it to BasicTextField: `visualTransformation = visualTransformation`.
- Import: `androidx.compose.ui.text.input.VisualTransformation`.

**Import additions needed:**
- `androidx.compose.ui.text.font.FontWeight`
- `androidx.compose.ui.text.input.VisualTransformation`

Remove `leadingIcon` parameter entirely -- the ">" prefix replaces the concept of a generic leading icon. The Pencil design has no arbitrary leading icon slot; only the terminal prefix. Keep `trailingIcon` since it's used for the password eye toggle.
  </action>
  <verify>
Build the designsystem module to confirm no compilation errors:
```
cd /Users/marc/IdeaProjects/Template && ./gradlew :app:designsystem:compileKotlinJvm 2>&1 | tail -20
```
Verify the prefix character ">" appears in the file, `colors.success` is referenced for filled prefix, `FontWeight.Bold` for prefix, `FontWeight.Medium` for label, `spacing.lg` and `spacing.md` for padding, `typography.base` for input text, `radius.sm` for corner radius, `6.dp` for label gap, and conditional border logic using `value.isEmpty()`.
  </verify>
  <done>
TerminalInput matches Pencil design: ">" prefix (muted when empty, green when filled), conditional border (gone when filled), correct padding 12v/16h, typography.base (13sp) for input text, fontWeight.Medium label, 6dp label gap, 8dp inner gap via Arrangement.spacedBy, radius.sm (4dp) corners.
  </done>
</task>

<task type="auto">
  <name>Task 2: Create TerminalPasswordInput and update Preview</name>
  <files>app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/input/TerminalInput.kt</files>
  <action>
Add a `TerminalPasswordInput` composable in the SAME file (below TerminalInput), plus a Canvas-based eye toggle icon, and update the @Preview function.

**TerminalPasswordInput composable:**

```kotlin
@Composable
fun TerminalPasswordInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "",
    enabled: Boolean = true,
    isError: Boolean = false,
    errorMessage: String? = null,
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val colors = TerminalTheme.colors

    TerminalInput(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        enabled = enabled,
        isError = isError,
        errorMessage = errorMessage,
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = { passwordVisible = !passwordVisible },
                    ),
            ) {
                EyeIcon(
                    open = passwordVisible,
                    color = colors.textDim,
                    modifier = Modifier.size(16.dp),
                )
            }
        },
    )
}
```

Imports needed: `remember`, `mutableStateOf`, `getValue`, `setValue`, `PasswordVisualTransformation`, `clickable`, `MutableInteractionSource`.

**EyeIcon composable (private, Canvas-based):**

Draw a 16x16 eye icon using Canvas, consistent with TerminalCheckbox/TerminalRadio pattern of Canvas-based icons.

```kotlin
@Composable
private fun EyeIcon(
    open: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.5.dp.toPx()
        val centerY = h * 0.5f

        // Eye outline: an almond/lens shape using two arcs
        // Top arc from left to right curving up
        // Bottom arc from left to right curving down
        val eyePath = androidx.compose.ui.graphics.Path().apply {
            // Left point
            moveTo(w * 0.1f, centerY)
            // Top arc (curves upward)
            cubicTo(
                w * 0.25f, h * 0.2f,   // control point 1
                w * 0.75f, h * 0.2f,   // control point 2
                w * 0.9f, centerY,      // end point (right)
            )
            // Bottom arc (curves downward, back to left)
            cubicTo(
                w * 0.75f, h * 0.8f,
                w * 0.25f, h * 0.8f,
                w * 0.1f, centerY,
            )
            close()
        }

        drawPath(
            path = eyePath,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )

        if (open) {
            // Pupil: filled circle in center
            drawCircle(
                color = color,
                radius = w * 0.1f,
                center = Offset(w * 0.5f, centerY),
            )
        } else {
            // Slash line: diagonal line from top-left to bottom-right through the eye
            drawLine(
                color = color,
                start = Offset(w * 0.25f, h * 0.25f),
                end = Offset(w * 0.75f, h * 0.75f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
        }
    }
}
```

Imports needed for EyeIcon: `androidx.compose.ui.graphics.Path`, `androidx.compose.ui.graphics.drawscope.Stroke`, `androidx.compose.ui.geometry.Offset`, `androidx.compose.ui.graphics.StrokeCap`, `androidx.compose.ui.graphics.Color`.

**Update the @Preview function** to show all important states:

```kotlin
@Preview
@Composable
private fun TerminalInputPreview() {
    TerminalTheme {
        Column(
            modifier = Modifier
                .background(TerminalTheme.colors.bg)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Empty input (shows border, muted prefix)
            TerminalInput(
                value = "",
                onValueChange = {},
                label = "username",
                placeholder = "Enter text...",
            )
            // Filled input (no border, green prefix)
            TerminalInput(
                value = "Hello",
                onValueChange = {},
                label = "username",
            )
            // Error state
            TerminalInput(
                value = "bad@",
                onValueChange = {},
                label = "Email",
                isError = true,
                errorMessage = "Invalid email",
            )
            // Password masked (eye-off icon)
            TerminalPasswordInput(
                value = "mySecretP4ss",
                onValueChange = {},
                label = "password",
                placeholder = "Enter password...",
            )
            // Password empty
            TerminalPasswordInput(
                value = "",
                onValueChange = {},
                label = "password",
                placeholder = "Enter password...",
            )
        }
    }
}
```

Note: The password preview with `value = "mySecretP4ss"` will show masked because `passwordVisible` defaults to `false`. This matches the Pencil "filled password" variant.
  </action>
  <verify>
Build the designsystem module:
```
cd /Users/marc/IdeaProjects/Template && ./gradlew :app:designsystem:compileKotlinJvm 2>&1 | tail -20
```
Verify TerminalPasswordInput exists in the file, EyeIcon is defined as private, the preview includes both TerminalInput and TerminalPasswordInput calls, and PasswordVisualTransformation is used.
  </verify>
  <done>
TerminalPasswordInput wraps TerminalInput with password masking toggle. Canvas-based EyeIcon renders open (pupil) and closed (slash) states at 16x16. Preview shows all 5 design states: empty, filled, error, password-masked, password-empty.
  </done>
</task>

</tasks>

<verification>
After both tasks, run a full module build:
```
cd /Users/marc/IdeaProjects/Template && ./gradlew :app:designsystem:compileKotlinJvm
```

Grep the output file to confirm all Pencil design tokens are applied:
- `">"` prefix character exists
- `colors.success` for filled prefix color
- `FontWeight.Bold` for prefix, `FontWeight.Medium` for label
- `spacing.lg` (16dp horizontal padding), `spacing.md` (12dp vertical padding)
- `typography.base` for input text (13sp)
- `radius.sm` (4dp corners)
- `6.dp` label gap
- `Arrangement.spacedBy(gap.sm)` for inner row
- Conditional border: `value.isEmpty()` check
- `TerminalPasswordInput` composable
- `EyeIcon` private composable
- `PasswordVisualTransformation`
</verification>

<success_criteria>
1. TerminalInput renders ">" prefix in textMuted color when empty, success color when filled
2. Border appears only when input is empty or in error state
3. Padding is 12dp vertical, 16dp horizontal (matching Pencil [12,16])
4. Input text uses typography.base (13sp), not typography.sm (12sp)
5. Label uses fontWeight Medium (500)
6. Label-to-input gap is 6dp
7. Inner element gap is 8dp via Arrangement.spacedBy
8. Corner radius is 4dp (radius.sm)
9. TerminalPasswordInput toggles between masked and visible text
10. Canvas-based EyeIcon shows pupil (open) or slash (closed)
11. Module compiles without errors
</success_criteria>

<output>
After completion, create `.planning/quick/2-align-terminalinput-with-pencil-design-a/2-SUMMARY.md`
</output>
