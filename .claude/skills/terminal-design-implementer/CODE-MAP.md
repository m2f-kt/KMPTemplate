# Code Map — Pencil → Kotlin Bidirectional Lookup

The reverse of `terminal-design-generator/REFERENCES.md`. This file is keyed by **Pencil ref ID** (left column) so you can answer the question "I see ref `eMdeB` in a design — what do I write in code?" in one lookup.

> Single source of truth: `terminal-design-generator/manifest.json`. This file presents the same data optimized for engineers reading a design and writing Compose.

---

## 1. Pencil ref ID → Kotlin Composable

### Actions

| Pencil ref | Symbol | Kotlin call                                                 | File                                            |
|------------|--------|-------------------------------------------------------------|-------------------------------------------------|
| `SpHta`    | Tb-d   | `TerminalButton(variant = ButtonVariant.Default, ...)`      | `components/button/TerminalButton.kt`           |
| `JFaAM`    | Tb-s   | `TerminalButton(variant = ButtonVariant.Secondary, ...)`    | `components/button/TerminalButton.kt`           |
| `zNQZK`    | Tb-g   | `TerminalButton(variant = ButtonVariant.Ghost, ...)`        | `components/button/TerminalButton.kt`           |
| `FQxTP`    | Tb-x   | `TerminalButton(variant = ButtonVariant.Destructive, ...)`  | `components/button/TerminalButton.kt`           |
| `qmSDa`    | Tib    | `TerminalIconButton(...)`                                   | `components/button/TerminalButton.kt`           |

### Inputs

| Pencil ref | Symbol | Kotlin call                                                                                  | File                                            |
|------------|--------|----------------------------------------------------------------------------------------------|-------------------------------------------------|
| `eMdeB`    | Tig    | `TerminalInput(value, onValueChange, label, placeholder, enabled, isError, errorMessage,…)` | `components/input/TerminalInput.kt`             |
| `RFeIS`    | Tig-f  | `TerminalInput(...)` — *filled appearance is implicit when `value` is non-empty*             | `components/input/TerminalInput.kt`             |
| `mMor5`    | Tta    | `TerminalTextarea(...)`                                                                      | `components/input/TerminalTextarea.kt`          |
| `ryDIe`    | Tch-d  | `TerminalCheckbox(checked = false, onCheckedChange, label, ...)`                             | `components/selection/TerminalCheckbox.kt`      |
| `ut9mj`    | Tch-c  | `TerminalCheckbox(checked = true, onCheckedChange, label, ...)`                              | `components/selection/TerminalCheckbox.kt`      |
| `4Ibe8`    | Tsw-d  | `TerminalSwitch(checked = false, onCheckedChange, label, ...)`                               | `components/selection/TerminalSwitch.kt`        |
| `pWKbD`    | Tsw-c  | `TerminalSwitch(checked = true, onCheckedChange, label, ...)`                                | `components/selection/TerminalSwitch.kt`        |
| `DlDhX`    | Tr-d   | `TerminalRadio(selected = false, onClick, label, ...)`                                       | `components/selection/TerminalRadio.kt`         |
| `JwPq6`    | Tr-c   | `TerminalRadio(selected = true, onClick, label, ...)`                                        | `components/selection/TerminalRadio.kt`         |

### Containers

| Pencil ref | Symbol | Kotlin call                                                | File                                            |
|------------|--------|------------------------------------------------------------|-------------------------------------------------|
| `zpDfd`    | Tc-d   | `TerminalCard(variant = CardVariant.Default) { … }`        | `components/card/TerminalCard.kt`               |
| `zCm6A`    | Tc-a   | `TerminalCard(variant = CardVariant.Accent) { … }`         | `components/card/TerminalCard.kt`               |
| `DeFHT`    | Tc-i   | `TerminalCard(variant = CardVariant.Info) { … }`           | `components/card/TerminalCard.kt`               |
| `U8SOz`    | Tc-h   | `TerminalCard(variant = CardVariant.Highlighted) { … }`    | `components/card/TerminalCard.kt`               |
| `Zs02a`    | Tc-c   | `TerminalCard(variant = CardVariant.Compact) { … }` *or `TerminalCompactCard()`* | `components/card/TerminalCard.kt` |
| `pawmp`    | Td     | `TerminalDivider()`                                        | `components/display/TerminalDivider.kt`         |

### Feedback

| Pencil ref | Symbol  | Kotlin call                                                | File                                            |
|------------|---------|------------------------------------------------------------|-------------------------------------------------|
| `weM3P`    | Talr-i  | `TerminalAlert(variant = AlertVariant.Info, text, ...)`    | `components/feedback/TerminalAlert.kt`          |
| `5E3LA`    | Talr-s  | `TerminalAlert(variant = AlertVariant.Success, text, ...)` | `components/feedback/TerminalAlert.kt`          |
| `H79gt`    | Talr-w  | `TerminalAlert(variant = AlertVariant.Warning, text, ...)` | `components/feedback/TerminalAlert.kt`          |
| `t4WAf`    | Talr-e  | `TerminalAlert(variant = AlertVariant.Error, text, ...)`   | `components/feedback/TerminalAlert.kt`          |
| `OvoQ4`    | Tpr-d   | `TerminalProgress(progress = <Float>, ...)`                | `components/feedback/TerminalProgress.kt`       |
| `YP7h8`    | Tpr-i   | `TerminalProgress(progress = null, ...)` — null sentinel = indeterminate | `components/feedback/TerminalProgress.kt` |
| `fHrNh`    | Ttp     | `TerminalTooltip(text) { … }`                              | `components/feedback/TerminalTooltip.kt`        |

### Display

| Pencil ref | Symbol | Kotlin call                                                  | File                                            |
|------------|--------|--------------------------------------------------------------|-------------------------------------------------|
| `kkARH`    | Tba-d  | `TerminalBadge(variant = BadgeVariant.Default, text, ...)`   | `components/feedback/TerminalBadge.kt`          |
| `Zek57`    | Tba-a  | `TerminalBadge(variant = BadgeVariant.Accent, text, ...)`    | `components/feedback/TerminalBadge.kt`          |
| `alR2r`    | Tba-s  | `TerminalBadge(variant = BadgeVariant.Success, text, ...)`   | `components/feedback/TerminalBadge.kt`          |
| `AVukf`    | Tba-w  | `TerminalBadge(variant = BadgeVariant.Warning, text, ...)`   | `components/feedback/TerminalBadge.kt`          |
| `F5EHO`    | Tba-e  | `TerminalBadge(variant = BadgeVariant.Error, text, ...)`     | `components/feedback/TerminalBadge.kt`          |
| `wRO5v`    | Ttbl   | `TerminalTable(headers, rows) { … }` *or `TerminalSelectableTable(...)` for multi-select* | `components/data/TerminalTable.kt` |
| `yRRLK`    | Ttr    | `TerminalTableRow { TerminalTableCell { ... } }` *or `TerminalSelectableTableRow(...)*  | `components/data/TerminalTable.kt` |
| `xiRqf`    | Tl     | `TerminalList { … }`                                         | `components/data/TerminalList.kt`               |
| `1HAho`    | Tli-d  | `TerminalListItem(state = ListItemState.Default, ...)`       | `components/data/TerminalList.kt`               |
| `nvEsK`    | Tli-h  | `TerminalListItem(state = ListItemState.Hover, ...)`         | `components/data/TerminalList.kt`               |
| `cZWar`    | Tli-s  | `TerminalListItem(state = ListItemState.Selected, ...)`      | `components/data/TerminalList.kt`               |
| `vCa6P`    | Tli-x  | `TerminalListItem(state = ListItemState.Disabled, ...)`      | `components/data/TerminalList.kt`               |

### Utility

| Pencil ref | Symbol | Kotlin call                | File                                            |
|------------|--------|----------------------------|-------------------------------------------------|
| `6cukz`    | Tav    | `TerminalAvatar(...)`      | `components/display/TerminalAvatar.kt`          |
| `8cfy6`    | Tk     | `TerminalKbd(text = "⌘K")` | `components/display/TerminalKbd.kt`             |

---

## 2. Pencil token → Kotlin accessor

### Surface (page/card/border)

| Pencil token              | Kotlin accessor                          |
|---------------------------|------------------------------------------|
| `$--terminal-bg`          | `TerminalTheme.colors.bg`                |
| `$--terminal-surface`     | `TerminalTheme.colors.surface`           |
| `$--terminal-inset`       | `TerminalTheme.colors.inset`             |
| `$--terminal-border`      | `TerminalTheme.colors.border`            |

### Text

| Pencil token              | Kotlin accessor                          |
|---------------------------|------------------------------------------|
| `$--terminal-text`        | `TerminalTheme.colors.text`              |
| `$--terminal-text-muted`  | `TerminalTheme.colors.textMuted`         |
| `$--terminal-text-dim`    | `TerminalTheme.colors.textDim`           |

### Semantic (foreground × background pairs)

| Pencil token              | Kotlin accessor                          |
|---------------------------|------------------------------------------|
| `$--terminal-accent`      | `TerminalTheme.colors.accent`            |
| `$--terminal-accent-muted`| `TerminalTheme.colors.accentMuted`       |
| `$--terminal-success`     | `TerminalTheme.colors.success`           |
| `$--terminal-success-bg`  | `TerminalTheme.colors.successBg`         |
| `$--terminal-warning`     | `TerminalTheme.colors.warning`           |
| `$--terminal-warning-bg`  | `TerminalTheme.colors.warningBg`         |
| `$--terminal-error`       | `TerminalTheme.colors.error`             |
| `$--terminal-error-bg`    | `TerminalTheme.colors.errorBg`           |
| `$--terminal-info`        | `TerminalTheme.colors.info`              |
| `$--terminal-info-bg`     | `TerminalTheme.colors.infoBg`            |

### Component-scoped (use only inside the matching component family)

#### Buttons

| Pencil token                    | Kotlin accessor                              |
|---------------------------------|----------------------------------------------|
| `$--btn-primary-bg`             | `TerminalTheme.colors.btnPrimaryBg`          |
| `$--btn-primary-text`           | `TerminalTheme.colors.btnPrimaryText`        |
| `$--btn-primary-hover-bg`       | `TerminalTheme.colors.btnPrimaryHoverBg`     |
| `$--btn-secondary-bg`           | `TerminalTheme.colors.btnSecondaryBg`        |
| `$--btn-secondary-text`         | `TerminalTheme.colors.btnSecondaryText`      |
| `$--btn-secondary-border`       | `TerminalTheme.colors.btnSecondaryBorder`    |
| `$--btn-secondary-hover-bg`     | `TerminalTheme.colors.btnSecondaryHoverBg`   |
| `$--btn-ghost-text`             | `TerminalTheme.colors.btnGhostText`          |
| `$--btn-ghost-hover-bg`         | `TerminalTheme.colors.btnGhostHoverBg`       |
| `$--btn-destructive-bg`         | `TerminalTheme.colors.btnDestructiveBg`      |
| `$--btn-destructive-text`       | `TerminalTheme.colors.btnDestructiveText`    |
| `$--btn-destructive-hover-bg`   | `TerminalTheme.colors.btnDestructiveHoverBg` |
| `$--btn-success-bg`             | `TerminalTheme.colors.btnSuccessBg`          |
| `$--btn-success-text`           | `TerminalTheme.colors.btnSuccessText`        |
| `$--btn-success-hover-bg`       | `TerminalTheme.colors.btnSuccessHoverBg`     |
| `$--btn-disabled-bg`            | `TerminalTheme.colors.btnDisabledBg`         |
| `$--btn-disabled-text`          | `TerminalTheme.colors.btnDisabledText`       |
| `$--btn-disabled-border`        | `TerminalTheme.colors.btnDisabledBorder`     |
| `$--btn-font-size`              | *no Kotlin counterpart — use `TerminalTheme.typography.sm`* |
| `$--btn-padding-x` / `-x-sm`    | *no Kotlin counterpart — use `TerminalTheme.spacing.lg` / `.md`* |
| `$--btn-padding-y`              | *no Kotlin counterpart — use `TerminalTheme.spacing.sm`* |
| `$--btn-gap`                    | *no Kotlin counterpart — use `TerminalTheme.gap.sm`* |
| `$--btn-icon-size`              | *no Kotlin counterpart — pass `Modifier.size(14.dp)` *(token TODO)* |

#### Cards

| Pencil token                    | Kotlin accessor                              |
|---------------------------------|----------------------------------------------|
| `$--card-accent-bg`             | `TerminalTheme.colors.cardAccentBg`          |
| `$--card-accent-header-bg`      | `TerminalTheme.colors.cardAccentHeaderBg`    |
| `$--card-accent-header-text`    | `TerminalTheme.colors.cardAccentHeaderText`  |
| `$--card-accent-body-text`      | `TerminalTheme.colors.cardAccentBodyText`    |

#### Tables

| Pencil token                       | Kotlin accessor                            |
|------------------------------------|--------------------------------------------|
| `$--table-header-bg`               | `TerminalTheme.colors.tableHeaderBg`       |
| `$--table-header-text`             | `TerminalTheme.colors.tableHeaderText`     |
| `$--table-row-text-primary`        | `TerminalTheme.colors.tableRowTextPrimary` |
| `$--table-row-text-secondary`      | `TerminalTheme.colors.tableRowTextSecondary` |
| `$--table-row-selected-bg`         | `TerminalTheme.colors.tableRowSelectedBg`  |

#### Charts

| Pencil token                | Kotlin accessor                         |
|-----------------------------|-----------------------------------------|
| `$--chart-bg`               | `TerminalTheme.colors.chartBg`          |
| `$--chart-axis`             | `TerminalTheme.colors.chartAxis`        |
| `$--chart-axis-text`        | `TerminalTheme.colors.chartAxisText`    |
| `$--chart-grid`             | `TerminalTheme.colors.chartGrid`        |
| `$--chart-bar-1`            | `TerminalTheme.colors.chartBar1`        |
| `$--chart-bar-2`            | `TerminalTheme.colors.chartBar2`        |
| `$--chart-bar-3`            | `TerminalTheme.colors.chartBar3`        |
| `$--chart-bar-highlight`    | `TerminalTheme.colors.chartBarHighlight`|
| `$--chart-series-1`         | `TerminalTheme.colors.chartSeries1`     |
| `$--chart-series-1-muted`   | `TerminalTheme.colors.chartSeries1Muted`|
| `$--chart-series-2`         | `TerminalTheme.colors.chartSeries2`     |

#### Badges (Pencil-only — no Kotlin field)

The Pencil tokens `$--badge-default-bg/text/border`, `$--badge-accent-bg/text`, `$--badge-success-bg/text`, `$--badge-warning-bg/text`, `$--badge-error-bg/text` exist in Pencil but **`TerminalBadge` does not consume them** — it composes from semantic tokens (`success` / `warning` / `error`), `inset`, `border`, `text`, `accent`. When you see a `$--badge-*` token in a design, just call `TerminalBadge(variant = ...)` with the matching `BadgeVariant` and let the composable resolve the colors.

#### Checkbox

| Pencil token (TODO)         | Kotlin accessor                    |
|-----------------------------|------------------------------------|
| `$--checkbox-bg` *(not in Pencil yet)* | `TerminalTheme.colors.checkboxBg` |

### Typography

| Pencil `fontSize` | Kotlin accessor                                   |
|-------------------|---------------------------------------------------|
| 9                 | `TerminalTheme.typography.chartAxis`              |
| 10                | `TerminalTheme.typography.xxs`                    |
| 11                | `TerminalTheme.typography.xs`                     |
| 12                | `TerminalTheme.typography.sm`                     |
| 13                | `TerminalTheme.typography.base`                   |
| 14                | `TerminalTheme.typography.md`                     |
| 32                | `TerminalTheme.typography.xxl`                    |
| `$--terminal-font`| `TerminalTheme.typography.fontFamily` *(JetBrains Mono)* |

> Sizes 16/20/24/28/36/48 in Pencil are reserved for atlas hero/title art. They have no production typography token. If a screen needs a size between `md` (14) and `xxl` (32), raise the question — don't inline.

### Spacing (`padding`, fixed dp)

| Pencil dp | Kotlin accessor              |
|-----------|------------------------------|
| 4         | `TerminalTheme.spacing.xs`   |
| 8         | `TerminalTheme.spacing.sm`   |
| 12        | `TerminalTheme.spacing.md`   |
| 16        | `TerminalTheme.spacing.lg`   |
| 20        | `TerminalTheme.spacing.xl`   |

### Gap (`Arrangement.spacedBy` / sibling gap)

| Pencil dp | Kotlin accessor              |
|-----------|------------------------------|
| 4         | `TerminalTheme.gap.xs`       |
| 8         | `TerminalTheme.gap.sm`       |
| 12        | `TerminalTheme.gap.md`       |
| 16        | `TerminalTheme.gap.lg`       |
| 24        | `TerminalTheme.gap.xl`       |

### Corner radius

| Pencil dp        | Kotlin accessor              |
|------------------|------------------------------|
| 0                | `TerminalTheme.radius.none`  |
| 2                | `TerminalTheme.radius.xs`    |
| 4 *(`$--terminal-radius`)* | `TerminalTheme.radius.sm` |
| 6                | `TerminalTheme.radius.md`    |
| 12               | `TerminalTheme.radius.lg`    |
| 24               | `TerminalTheme.radius.pill`  |
| 9999 *(circle)*  | `TerminalTheme.radius.full` *(or use `CircleShape` for clarity)* |

### Code-only scales (no Pencil token yet)

| Group     | Kotlin accessors                                      |
|-----------|-------------------------------------------------------|
| Borders   | `TerminalTheme.borders.{thin,default,thick}` *(1/2/3 dp)* |
| Opacity   | `TerminalTheme.opacity.{full,high,medium,low}` *(1.0/0.75/0.5/0.25)* |
| Shadows   | `TerminalTheme.shadows.{none,sm,md,lg}`              |

---

## 3. Code-only Composables (no Pencil ref)

These exist in `app:designsystem` but have no Pencil counterpart. Use them when implementing — but they don't appear in design files until they're added to Pencil.

| Kotlin call                               | File                                                     | Use when                                          |
|-------------------------------------------|----------------------------------------------------------|---------------------------------------------------|
| `TerminalText(...)`                       | `components/TerminalText.kt`                             | text primitive — composed by everything else      |
| `TerminalBarChart(...)`                   | `components/data/TerminalBarChart.kt`                    | bar chart visualisation                           |
| `TerminalLineChart(...)`                  | `components/data/TerminalLineChart.kt`                   | line chart                                        |
| `TerminalRadarChart(...)`                 | `components/data/TerminalRadarChart.kt`                  | radar/spider chart                                |
| `TerminalDropdownMenu(...) + TerminalDropdownMenuItem(...)` | `components/data/TerminalDropdownMenu.kt` | popup menu surface + rows                         |
| `TerminalReorderableList(...) + rememberReorderState()` | `components/data/TerminalReorderableList.kt` | drag-to-reorder generic list                     |
| `TerminalSwipeReveal(...) + TerminalDeleteAction(...)` | `components/data/TerminalSwipeReveal.kt` | swipe-to-reveal + built-in delete affordance      |
| `TerminalSelectableTable(...) + TerminalSelectableTableRow(...)` | `components/data/TerminalTable.kt` | multi-select table variant                        |
| `TerminalPasswordInput(...)`              | `components/input/TerminalInput.kt`                      | password field with eye-toggle                    |
| `rememberImagePickerLauncher()`           | `components/picker/ImagePicker.kt`                       | multiplatform image picker (no UI ref)            |

---

## 4. Common patterns — implementation recipes

### Pattern: a Pencil card with a primary + ghost button

Pencil:
```javascript
card = I(parent, {type: "ref", ref: "U8SOz"})  // Tc-h Highlighted
buttonsRow = I(card+"/<contentSlotId>", {type: "frame", layout: "horizontal", gap: 12})
primary = I(buttonsRow, {type: "ref", ref: "SpHta"})
ghost   = I(buttonsRow, {type: "ref", ref: "zNQZK"})
```

Kotlin:
```kotlin
TerminalCard(variant = CardVariant.Highlighted) {
    Row(horizontalArrangement = Arrangement.spacedBy(TerminalTheme.gap.md)) {
        TerminalButton(variant = ButtonVariant.Default, onClick = onSave, text = "Save")
        TerminalButton(variant = ButtonVariant.Ghost,   onClick = onCancel, text = "Cancel")
    }
}
```

### Pattern: form field with error

Pencil:
```javascript
field = I(form, {type: "ref", ref: "eMdeB"})          // Tig
U(field+"/<labelId>", {content: "Email"})
U(field+"/<errorId>", {content: "Invalid email"})     // shown only when isError
```

Kotlin:
```kotlin
TerminalInput(
    value = state.email,
    onValueChange = { onIntent(LoginIntent.Email(it)) },
    label = "Email",
    isError = state.emailError != null,
    errorMessage = state.emailError,
)
```

### Pattern: status row (avatar + name + status badge)

Pencil:
```javascript
row = I(list, {type: "frame", layout: "horizontal", gap: 12, alignItems: "center"})
avatar = I(row, {type: "ref", ref: "6cukz"})           // Tav
name   = I(row, {type: "text", content: "Marc", fill: "$--terminal-text", fontSize: 12})
badge  = I(row, {type: "ref", ref: "alR2r"})           // Tba-s Success
U(badge+"/<textId>", {content: "Active"})
```

Kotlin:
```kotlin
Row(
    horizontalArrangement = Arrangement.spacedBy(TerminalTheme.gap.md),
    verticalAlignment = Alignment.CenterVertically,
) {
    TerminalAvatar(initials = "M")
    BasicText(text = "Marc", style = TerminalTheme.typography.sm.copy(color = TerminalTheme.colors.text))
    TerminalBadge(variant = BadgeVariant.Success, text = "Active")
}
```

---

## 5. Anti-patterns to avoid

| If you write…                                 | …it should have been                                    |
|-----------------------------------------------|---------------------------------------------------------|
| `Color(0xFF525252)`                           | `TerminalTheme.colors.btnPrimaryBg`                     |
| `FontFamily.Monospace`                        | `TerminalTheme.typography.fontFamily`                   |
| `fontSize = 11.sp`                            | `style = TerminalTheme.typography.xs`                   |
| `Modifier.padding(16.dp)`                     | `Modifier.padding(TerminalTheme.spacing.lg)`            |
| `Arrangement.spacedBy(8.dp)`                  | `Arrangement.spacedBy(TerminalTheme.gap.sm)`            |
| `RoundedCornerShape(4.dp)`                    | `RoundedCornerShape(TerminalTheme.radius.sm)`           |
| `Box { /* fake button */ }`                   | `TerminalButton(variant = ..., onClick = ..., text = ...)` |
| `Row { Text("Active") }` *for a status chip*  | `TerminalBadge(variant = BadgeVariant.Success, text = "Active")` |
| Re-implementing TerminalCard from primitives  | `TerminalCard(variant = ...) { ... }`                   |

If your PR contains any cell from the left column, the design-to-code chain has broken. Fix before merging.
