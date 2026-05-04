# References — Canonical Catalog

The single source of truth for what may be used in `terminal_design_system.pen`. Read this **before** generating any node. If something you need is not here, see `SKILL.md → Escape hatches` or stop and ask — never invent.

> **How to read this file**
> - **Symbol** — short Kotlin-friendly abbreviation used in atlases (v3 Periodic Table) and shorthand plans.
> - **Pencil ID** — the exact `ref` string to put in `{type:"ref", ref:"<id>"}`.
> - **Kotlin** — the corresponding Compose composable / variant.
> - **Use when** — the design intent that this ref is the right answer for.

---

## 1. Components — 41 reusable refs

Every interactive element, container, badge, alert, list item, etc. in any new design MUST be a `type:"ref"` to one of the IDs below. Customize via `U(instance + "/childId", {...})` or the `descendants` block — never by recreating the shape.

### 1.1 Actions (5 elements, accent color `#6BAF8A`)

| Symbol | Pencil ID | Name                          | Kotlin                              | Use when                                          |
|--------|-----------|-------------------------------|-------------------------------------|---------------------------------------------------|
| Tb-d   | `SpHta`   | Terminal Button/Default       | `TerminalButton(Default)`           | primary action — the one expected next            |
| Tb-s   | `JFaAM`   | Terminal Button/Secondary     | `TerminalButton(Secondary)`         | parallel-rank action next to the primary          |
| Tb-g   | `zNQZK`   | Terminal Button/Ghost         | `TerminalButton(Ghost)`             | tertiary / inline link-like action                |
| Tb-x   | `FQxTP`   | Terminal Button/Destructive   | `TerminalButton(Destructive)`       | irreversible / data-loss action                   |
| Tib    | `qmSDa`   | Terminal Icon Button          | `TerminalIconButton(...)`           | icon-only action — must have a tooltip companion  |

### 1.2 Inputs (8 elements, accent color `#7AA4CA`)

| Symbol | Pencil ID | Name                          | Kotlin                                                                | Use when                                          |
|--------|-----------|-------------------------------|-----------------------------------------------------------------------|---------------------------------------------------|
| Tig    | `eMdeB`   | Terminal Input Group          | `TerminalInput(value, onValueChange, label, ...)`                     | single-line text input + label + helper           |
| Tig-f  | `RFeIS`   | Terminal Input Group/Filled   | `TerminalInput(...)` *(filled appearance is implicit when value ≠ "")*| input variant — no explicit `filled` param yet    |
| Tta    | `mMor5`   | Terminal Textarea             | `TerminalTextarea(...)`                                               | multi-line text input                             |
| Tch-d  | `ryDIe`   | Terminal Checkbox/Default     | `TerminalCheckbox(checked = false, onCheckedChange)`                  | unchecked binary toggle (also tri-state overload) |
| Tch-c  | `ut9mj`   | Terminal Checkbox/Checked     | `TerminalCheckbox(checked = true, onCheckedChange)`                   | checked binary toggle                             |
| Tsw-d  | `4Ibe8`   | Terminal Switch/Default       | `TerminalSwitch(checked = false, onCheckedChange)`                    | unchecked setting toggle                          |
| Tsw-c  | `pWKbD`   | Terminal Switch/Checked       | `TerminalSwitch(checked = true, onCheckedChange)`                     | checked setting toggle                            |
| Tr-d   | `DlDhX`   | Terminal Radio/Default        | `TerminalRadio(selected = false, onClick)`                            | unselected option in an exclusive group           |
| Tr-c   | `JwPq6`   | Terminal Radio/Checked        | `TerminalRadio(selected = true, onClick)`                             | selected option in an exclusive group             |

### 1.3 Containers (6 elements, accent color `#C9A84D`)

| Symbol | Pencil ID | Name                          | Kotlin                              | Use when                                          |
|--------|-----------|-------------------------------|-------------------------------------|---------------------------------------------------|
| Tc-d   | `zpDfd`   | Terminal Card                 | `TerminalCard(...)`                 | default content surface                           |
| Tc-a   | `zCm6A`   | Terminal Card/Accent          | `TerminalCard(Accent)`              | card with strong visual emphasis (header strip)   |
| Tc-i   | `DeFHT`   | Terminal Card/Info            | `TerminalCard(Info)`                | informational card with `info` semantic           |
| Tc-h   | `U8SOz`   | Terminal Card/Highlighted     | `TerminalCard(Highlighted)`         | featured card — singled out in a list/grid        |
| Tc-c   | `Zs02a`   | Terminal Card/Compact         | `TerminalCard(Compact)`             | dense card for tight grids/lists                  |
| Td     | `pawmp`   | Terminal Divider              | `TerminalDivider()`                 | visual rule between groups                        |

### 1.4 Feedback (7 elements, accent color `#C06060`)

| Symbol | Pencil ID | Name                          | Kotlin                              | Use when                                          |
|--------|-----------|-------------------------------|-------------------------------------|---------------------------------------------------|
| Talr-i | `weM3P`   | Terminal Alert/Info           | `TerminalAlert(Info)`               | neutral system message                            |
| Talr-s | `5E3LA`   | Terminal Alert/Success        | `TerminalAlert(Success)`            | confirmation of completion                        |
| Talr-w | `H79gt`   | Terminal Alert/Warning        | `TerminalAlert(Warning)`            | recoverable issue — user can proceed              |
| Talr-e | `t4WAf`   | Terminal Alert/Error          | `TerminalAlert(Error)`              | blocking issue — user must resolve                |
| Tpr-d  | `OvoQ4`   | Terminal Progress             | `TerminalProgress(value)`           | determinate progress (known total)                |
| Tpr-i  | `YP7h8`   | Terminal Progress/Indeterminate | `TerminalProgress.Indeterminate()` | indeterminate progress (unknown total)            |
| Ttp    | `fHrNh`   | Terminal Tooltip              | `TerminalTooltip(...)`              | on-hover/long-press contextual help               |

### 1.5 Display (13 elements, accent color `#B89D8A`)

| Symbol | Pencil ID | Name                          | Kotlin                              | Use when                                          |
|--------|-----------|-------------------------------|-------------------------------------|---------------------------------------------------|
| Tba-d  | `kkARH`   | Terminal Badge/Default        | `TerminalBadge(Default)`            | neutral status / count                            |
| Tba-a  | `Zek57`   | Terminal Badge/Accent         | `TerminalBadge(Accent)`             | emphasized status — accented                      |
| Tba-s  | `alR2r`   | Terminal Badge/Success        | `TerminalBadge(Success)`            | success status                                    |
| Tba-w  | `AVukf`   | Terminal Badge/Warning        | `TerminalBadge(Warning)`            | warning status                                    |
| Tba-e  | `F5EHO`   | Terminal Badge/Error          | `TerminalBadge(Error)`              | error status                                      |
| Ttbl   | `wRO5v`   | Terminal Table                | `TerminalTable(...)`                | tabular data surface                              |
| Ttr    | `yRRLK`   | Terminal Table Row            | `TerminalTableRow(...)`             | data row inside Terminal Table                    |
| Tl     | `xiRqf`   | Terminal List                 | `TerminalList(...)`                 | vertical list container                           |
| Tli-d  | `1HAho`   | Terminal List Item/Default    | `TerminalListItem(Default)`         | resting list row                                  |
| Tli-h  | `nvEsK`   | Terminal List Item/Hover      | `TerminalListItem(Hover)`           | hovered list row preview                          |
| Tli-s  | `cZWar`   | Terminal List Item/Selected   | `TerminalListItem(Selected)`        | selected list row                                 |
| Tli-x  | `vCa6P`   | Terminal List Item/Disabled   | `TerminalListItem(Disabled)`        | disabled / not-actionable list row                |

### 1.6 Utility (2 elements, accent color `#6B6B6B`)

| Symbol | Pencil ID | Name                          | Kotlin                              | Use when                                          |
|--------|-----------|-------------------------------|-------------------------------------|---------------------------------------------------|
| Tav    | `6cukz`   | Terminal Avatar               | `TerminalAvatar(...)`               | user identity glyph                               |
| Tk     | `8cfy6`   | Terminal Kbd                  | `TerminalKbd("⌘K")`                 | render a keyboard chord                           |

### Decision tree — picking the right ref

```
need user action?
├─ primary call to action ────────────── Tb-d  (SpHta)
├─ alongside a primary ─────────────────── Tb-s  (JFaAM)
├─ inline / tertiary ──────────────────── Tb-g  (zNQZK)
├─ destructive ────────────────────────── Tb-x  (FQxTP)
└─ icon-only (with tooltip!) ───────────── Tib   (qmSDa)

need user input?
├─ single-line text ────────────────────── Tig   (eMdeB) or Tig-f (RFeIS)
├─ multi-line text ─────────────────────── Tta   (mMor5)
├─ binary on/off (form) ─────────────────── Tch   (ryDIe / ut9mj)
├─ binary on/off (settings) ─────────────── Tsw   (4Ibe8 / pWKbD)
└─ exclusive choice ────────────────────── Tr    (DlDhX / JwPq6)

need to communicate state?
├─ system message ──────────────────────── Talr  (weM3P / 5E3LA / H79gt / t4WAf)
├─ chip / count / status ────────────────── Tba   (kkARH / Zek57 / alR2r / AVukf / F5EHO)
├─ progress ──────────────────────────────── Tpr   (OvoQ4 / YP7h8)
└─ on-hover help ────────────────────────── Ttp   (fHrNh)

need a container?
├─ default surface ─────────────────────── Tc-d  (zpDfd)
├─ emphasized ──────────────────────────── Tc-a  (zCm6A) or Tc-h (U8SOz)
├─ informational ────────────────────────── Tc-i  (DeFHT)
├─ dense / list cell ────────────────────── Tc-c  (Zs02a)
└─ separator only ──────────────────────── Td    (pawmp)

need to display data?
├─ tabular ──────────────────────────────── Ttbl + Ttr (wRO5v + yRRLK)
└─ list ─────────────────────────────────── Tl + Tli-* (xiRqf + 1HAho/nvEsK/cZWar/vCa6P)

need user identity? ──────────────────────── Tav (6cukz)
need a kbd shortcut glyph? ────────────────── Tk  (8cfy6)
```

---

## 2. Color tokens

All `fill`, `stroke.fill`, and `effect.color` properties MUST be `$--*` variables. The variables are theme-aware (Light / Dark) — using them keeps the design responsive to the active mode automatically.

### 2.1 Surface tokens (the canvas, not the content)

| Token                  | Light  | Dark   | Use for                                                   |
|------------------------|--------|--------|-----------------------------------------------------------|
| `$--terminal-bg`       | #FAFAFA | #0A0A0A | top-level page background                                 |
| `$--terminal-surface`  | #FFFFFF | #171717 | raised surface (card body, modal, sheet)                  |
| `$--terminal-inset`    | #F5F5F5 | #1F1F1F | recessed surface (input field, disabled, code block)      |
| `$--terminal-border`   | #D4D4D4 | #262626 | hairline border between adjacent surfaces                 |

### 2.2 Text tokens (3-tier hierarchy)

| Token                     | Light   | Dark    | Use for                                                |
|---------------------------|---------|---------|--------------------------------------------------------|
| `$--terminal-text`        | #171717 | #E5E5E5 | primary text — titles, body                            |
| `$--terminal-text-muted`  | #525252 | #A3A3A3 | secondary text — captions, labels                      |
| `$--terminal-text-dim`    | #737373 | #525252 | tertiary text — meta, dividers, hints                  |

### 2.3 Semantic tokens (foreground × background pairs)

| Token                         | Light   | Dark    | Use with paired bg                          |
|-------------------------------|---------|---------|---------------------------------------------|
| `$--terminal-accent`          | #525252 | #A3A3A3 | accent foreground                           |
| `$--terminal-accent-muted`    | #E5E5E5 | #262626 | accent background                           |
| `$--terminal-success`         | #3D7A4A | #5A9E6A | success foreground                          |
| `$--terminal-success-bg`      | #E8F0EA | #1A231C | success background                          |
| `$--terminal-warning`         | #8A7030 | #B89D4A | warning foreground                          |
| `$--terminal-warning-bg`      | #F0ECE0 | #252218 | warning background                          |
| `$--terminal-error`           | #9A4444 | #C06060 | error foreground                            |
| `$--terminal-error-bg`        | #F0E8E8 | #251A1A | error background                            |
| `$--terminal-info`            | #3D6080 | #6090B8 | info foreground                             |
| `$--terminal-info-bg`         | #E8EDF0 | #1A2025 | info background                             |

### 2.4 Component-scoped tokens (use only inside the matching component family)

> These exist to keep component refs internally consistent. You normally do **not** touch them — the refs already use them. You only reach for them when overriding a slot inside a component instance.

#### Buttons (`--btn-*`)

```
--btn-primary-bg / --btn-primary-text / --btn-primary-hover-bg
--btn-secondary-bg / --btn-secondary-text / --btn-secondary-border / --btn-secondary-hover-bg
--btn-ghost-text / --btn-ghost-hover-bg
--btn-destructive-bg / --btn-destructive-text / --btn-destructive-hover-bg
--btn-success-bg / --btn-success-text / --btn-success-hover-bg
--btn-disabled-bg / --btn-disabled-text / --btn-disabled-border
```

#### Badges (`--badge-*`)

```
--badge-default-bg / --badge-default-text / --badge-default-border
--badge-accent-bg / --badge-accent-text
--badge-success-bg / --badge-success-text
--badge-warning-bg / --badge-warning-text
--badge-error-bg / --badge-error-text
```

#### Cards (`--card-*`)

```
--card-accent-bg / --card-accent-body-text
--card-accent-header-bg / --card-accent-header-text
```

#### Tables (`--table-*`)

```
--table-header-bg / --table-header-text
--table-row-text-primary / --table-row-text-secondary
--table-row-selected-bg
```

#### Charts (`--chart-*`) — for chart sections only

```
--chart-bg / --chart-axis / --chart-axis-text / --chart-grid
--chart-bar-1 / --chart-bar-2 / --chart-bar-3 / --chart-bar-highlight
--chart-series-1 / --chart-series-1-muted / --chart-series-2
```

---

## 3. Typography

The Kotlin theme ships **7 named text styles** — one per row below. Every text node must use a name from this scale. Inline `.sp` literals are forbidden in code; in Pencil, use the matching `fontSize` integer.

| Name         | Pencil `fontSize` | Kotlin                                | Use for                                  |
|--------------|------------------|---------------------------------------|------------------------------------------|
| `chartAxis`  | 9                | `TerminalTheme.typography.chartAxis`  | chart axis labels, tiny metadata         |
| `xxs`        | 10               | `TerminalTheme.typography.xxs`        | captions, badge text, table headers      |
| `xs`         | 11               | `TerminalTheme.typography.xs`         | section labels (`$ ...`), small body     |
| `sm`         | 12               | `TerminalTheme.typography.sm`         | body / table cells / inline meta         |
| `base`       | 13               | `TerminalTheme.typography.base`       | default body text                        |
| `md`         | 14               | `TerminalTheme.typography.md`         | sub-headers (`# ...`), card titles       |
| `xxl`        | 32               | `TerminalTheme.typography.xxl`        | hero titles (`> TERMINAL_OPS`)           |

**Font family**: every text uses `$--terminal-font` ↔ `TerminalTheme.typography.fontFamily` (resolves to JetBrains Mono).

> ⚠️ Sizes 16/20/24/28/36/48 are reserved for **atlas hero/title art only** (the design-document zones), where the `xxl` step is too big. They are *not* in the production typography scale; use them only inside atlas frames.

---

## 4. Spacing & Gap scales

Two distinct scales exist in code. Use `spacing` for `padding`/fixed sizes; use `gap` for `Arrangement.spacedBy` / flex `gap` between siblings. They overlap on small values and diverge at the top end.

### Spacing (`padding`, fixed dp)

| Name | Pencil dp | Kotlin                              |
|------|-----------|-------------------------------------|
| xs   | 4         | `TerminalTheme.spacing.xs`          |
| sm   | 8         | `TerminalTheme.spacing.sm`          |
| md   | 12        | `TerminalTheme.spacing.md`          |
| lg   | 16        | `TerminalTheme.spacing.lg`          |
| xl   | 20        | `TerminalTheme.spacing.xl`          |

### Gap (`gap` between siblings)

| Name | Pencil dp | Kotlin                              |
|------|-----------|-------------------------------------|
| xs   | 4         | `TerminalTheme.gap.xs`              |
| sm   | 8         | `TerminalTheme.gap.sm`              |
| md   | 12        | `TerminalTheme.gap.md`              |
| lg   | 16        | `TerminalTheme.gap.lg`              |
| xl   | 24        | `TerminalTheme.gap.xl`              |

> ⚠️ Values **24, 32, 48, 64** are reserved for **atlas/hero zones only** (page-level dividers in design documents). Production code must use `spacing` (max 20) or `gap` (max 24). If a screen genuinely needs more, raise the question in design review — don't inline.

For absolutely-positioned canvas nodes (`layout: "none"`), `x`/`y`/`width`/`height` may be any integer — these are coordinates, not spacing. Tag the parent frame name with `// reason: positional canvas`.

---

## 5. Corner radius

| Name | Pencil dp | Kotlin                              | Use for                                            |
|------|-----------|-------------------------------------|----------------------------------------------------|
| none | 0         | `TerminalTheme.radius.none`         | full-bleed surfaces, square corners                |
| xs   | 2         | `TerminalTheme.radius.xs`           | progress track, micro-chip                         |
| sm   | 4         | `TerminalTheme.radius.sm` *(`$--terminal-radius`)* | every default rounded surface (cards, buttons, alerts, badges) |
| md   | 6         | `TerminalTheme.radius.md`           | inputs, mid-density containers                     |
| lg   | 12        | `TerminalTheme.radius.lg`           | feature cards, modals                              |
| pill | 24        | `TerminalTheme.radius.pill`         | pill-shaped chips, switch tracks                   |
| full | 9999      | `TerminalTheme.radius.full`         | circles (avatars), use with `CircleShape` preferred|

If you need a half-rounded shape, pass a `[topLeft, topRight, bottomRight, bottomLeft]` array — each corner value must come from this column.

---

## 5b. Borders, Opacity, Shadows (code-only scales)

These three scales exist in `TerminalTheme` but have **no Pencil counterpart yet**. When generating in code, use the Kotlin token; when designing in Pencil, use a literal until the variable lands.

| Group     | Token                                | Value     |
|-----------|--------------------------------------|-----------|
| Borders   | `TerminalTheme.borders.thin`         | 1.dp      |
|           | `TerminalTheme.borders.default`      | 2.dp      |
|           | `TerminalTheme.borders.thick`        | 3.dp      |
| Opacity   | `TerminalTheme.opacity.full`         | 1.0       |
|           | `TerminalTheme.opacity.high`         | 0.75      |
|           | `TerminalTheme.opacity.medium`       | 0.5       |
|           | `TerminalTheme.opacity.low`          | 0.25      |
| Shadows   | `TerminalTheme.shadows.sm`           | composite |
|           | `TerminalTheme.shadows.md`           | composite |
|           | `TerminalTheme.shadows.lg`           | composite |

---

## 6. Atlas-only colors (secondary palette)

These colors live **outside** the `$--terminal-*` token system and exist only for the metaphor of a specific atlas. They are documented here so they're not "hardcoded hex" — they're a recognized secondary palette. **Never use them in product screens.**

### 6.1 Periodic Table (v3) — category accents

| Group       | Color   | Symbol prefix examples |
|-------------|---------|------------------------|
| Actions     | `#6BAF8A` | Tb-* / Tib            |
| Inputs      | `#7AA4CA` | Tig / Tta / Tch / Tsw |
| Containers  | `#C9A84D` | Tc-* / Td             |
| Feedback    | `#C06060` | Talr-* / Tpr / Ttp    |
| Display     | `#B89D8A` | Tba-* / Ttbl / Tl     |
| Utility     | `#6B6B6B` | Tav / Tk              |

### 6.2 Transit Map (v4) — line colors

| Line          | Color   | Module mapping        |
|---------------|---------|-----------------------|
| Auth          | `#6BAF8A` | `:app:auth`         |
| Dashboard     | `#C9A84D` | `:app:dashboard`    |
| Documents     | `#7AA4CA` | `:app:documents`    |
| Profile       | `#C06060` | `:app:profile`      |
| Admin         | `#9B7AC4` | `:app:admin`        |
| DesignSystem  | `#B89D8A` | `:app:designsystem` |

### 6.3 Blueprint Set (v5) — drafting palette

| Use            | Color    |
|----------------|----------|
| Sheet bg       | `#0A0A0A` |
| Drawing surface| `#0D0D0D` |
| Header strip   | `#171717` |
| Light border   | `#2A2A2E` |
| Wall stroke    | `#A3A3A3` (heavy) / `#6B6B6B` (light) |
| Dim text       | `#525252` |
| Annotation     | `#6BAF8A` (callout) |

---

## 7. Quick lookup — by intent

| I need to render…                                | Use ref                                |
|--------------------------------------------------|----------------------------------------|
| A primary CTA                                    | `SpHta`                                |
| A "Cancel" alongside a primary                   | `JFaAM`                                |
| A "Learn more" link in a paragraph               | `zNQZK`                                |
| A "Delete account" red button                    | `FQxTP`                                |
| A close (×) icon button                          | `qmSDa` (+ tooltip)                    |
| An email field with label + helper               | `eMdeB`                                |
| A multi-line description field                   | `mMor5`                                |
| A "remember me" checkbox                         | `ryDIe` / `ut9mj`                      |
| A "dark mode" setting toggle                     | `4Ibe8` / `pWKbD`                      |
| One option in a "Plan: Free / Paid / Premium"    | `DlDhX` / `JwPq6`                      |
| A success toast                                  | `5E3LA`                                |
| A blocking error banner                          | `t4WAf`                                |
| A status chip ("Active" / "Suspended")           | `alR2r` / `AVukf` / `F5EHO`            |
| A featured plan card                             | `U8SOz`                                |
| A normal content card                            | `zpDfd`                                |
| A users list                                     | `xiRqf` + repeated `1HAho`             |
| A selected row in a users list                   | `cZWar`                                |
| A data table with rows                           | `wRO5v` + `yRRLK`                      |
| A loading spinner                                | `YP7h8`                                |
| A tooltip on hover                               | `fHrNh`                                |
| A `⌘K` keyboard shortcut hint                    | `8cfy6`                                |
| A user avatar in a header                        | `6cukz`                                |
| A horizontal rule between sections               | `pawmp`                                |
