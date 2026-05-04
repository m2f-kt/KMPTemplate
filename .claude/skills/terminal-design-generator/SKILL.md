---
name: terminal-design-generator
description: Use whenever generating, extending, or modifying designs in `terminal_design_system.pen`. Constrains all output to existing component refs (the 41 reusable component IDs) and design tokens (`$--terminal-*`, `$--btn-*`, etc.) — strictly forbids hardcoded hex colors, raw font names, off-scale spacing, and hallucinated components. Read REFERENCES.md before generating any node.
---

# Terminal Design Generator

A reference-only design-generation contract for `terminal_design_system.pen`. Every node produced under this skill must trace back to an existing component ID or design token. No exceptions without an explicit, documented escape hatch.

## When this skill applies

- The active editor is `terminal_design_system.pen` (or any file that imports its components).
- The user asks to add, modify, or extend a design — screen, dashboard, profile variant, marketing surface, atlas section, anything visual.
- An agent is generating a `.pen` design from a spec, ticket, or natural-language request.

If you are NOT touching this `.pen` file, this skill does not apply.

## Mission (one sentence)

Every component in any new design is a `type: "ref"` to one of the 41 reusable IDs; every color, font, spacing, and radius is a `$--` variable; nothing is invented.

## Three hard rules

### Rule 1 — REF over re-creation
If a button is needed, you MUST use one of the 5 existing button refs (`SpHta`, `JFaAM`, `zNQZK`, `FQxTP`, `qmSDa`). You may NOT construct a new button by composing rectangles, text, and fills. Same for inputs, cards, alerts, badges, checkboxes, switches, radios, lists, list items, tables, table rows, progress, tooltips, dividers, kbd, avatars.

```javascript
// REQUIRED
btn = I(parent, {type: "ref", ref: "SpHta"})
U(btn + "/<labelChildId>", {content: "Sign in"})

// FORBIDDEN
btn = I(parent, {type: "frame", fill: "#525252", padding: 8, ...})
I(btn,  {type: "text", content: "Sign in", fill: "#FAFAFA"})
```

### Rule 2 — TOKEN over literal
Every fill, stroke, font, font-size, spacing, gap, padding, and radius MUST be a `$--` variable from REFERENCES.md. No hex colors. No raw font names. No off-scale spacing values. No magic numbers.

```javascript
// REQUIRED
I(parent, {type: "frame", fill: "$--terminal-surface", padding: 16, gap: 8, cornerRadius: "$--terminal-radius"})
I(parent, {type: "text", content: "$ STATUS", fill: "$--terminal-text-dim", fontFamily: "$--terminal-font", fontSize: 11})

// FORBIDDEN
I(parent, {type: "frame", fill: "#171717", padding: 17, cornerRadius: 5})
I(parent, {type: "text", content: "$ STATUS", fill: "#737373", fontFamily: "JetBrains Mono", fontSize: 11})
```

### Rule 3 — SCALE over guess
Spacing/sizing MUST come from the canonical scale: `[0, 4, 8, 12, 16, 20, 24, 32, 48, 64]`. Typography MUST come from the canonical scale: `[9, 10, 11, 12, 13, 14, 16, 20, 24, 28, 32, 36, 48]`. If a need falls between two scale values, snap to the nearest scale value — do not invent.

## Workflow (run in this exact order)

### Step 0 — Open the references
**Always** read `REFERENCES.md` (sibling file in this skill folder) before generating any node. It contains the canonical lookup tables for all 41 components and all design tokens. If a need cannot be satisfied from REFERENCES.md, see "Escape hatches" below — do not improvise.

### Step 1 — Plan in references, not pixels
Translate the design intent into a list of refs and tokens BEFORE you write any operation. Format:

```
INTENT: A profile screen header with avatar, name, and edit button.

PLAN:
- container       → frame · fill=$--terminal-bg · padding=24 · gap=16
- avatar          → ref=6cukz (Terminal Avatar)
- name            → text · content="Marc" · fill=$--terminal-text · fontFamily=$--terminal-font · fontSize=20
- editBtn         → ref=JFaAM (Terminal Button/Secondary) · descendants override label="Edit"
```

If any line cannot fill its `→` slot from REFERENCES.md, stop and resolve before generating.

### Step 2 — Generate using only refs + tokens
Use `batch_design` with operations that match the plan 1:1. Every `I` of a component-shaped thing is `type: "ref"`. Every property that has a token MUST use the token.

### Step 3 — Self-validate (mandatory before reporting "done")
Run the validation checklist below over the diff you just produced. If any item fails, fix it before continuing.

## Validation checklist

After every `batch_design` call, scan your operations for these violations:

- [ ] No `type: "frame"`/`type: "rectangle"`/`type: "text"` ad-hoc construction of something a reusable ref already covers (button, input, card, alert, badge, checkbox, switch, radio, table, list item, progress, tooltip, divider, kbd, avatar, textarea).
- [ ] No literal hex (`#XXXXXX`) on any `fill`, `stroke.fill`, `effect.color`, etc. Every color is a `$--` variable.
- [ ] No `fontFamily` other than `$--terminal-font`.
- [ ] No `fontSize` outside `[9, 10, 11, 12, 13, 14, 16, 20, 24, 28, 32, 36, 48]`.
- [ ] No `padding`/`gap`/`width`/`height` integer outside `[0, 4, 8, 12, 16, 20, 24, 32, 48, 64]` (except for absolute-position layouts where geometric coordinates are unavoidable).
- [ ] No `cornerRadius` other than `0` or `$--terminal-radius`.
- [ ] Every `type: "ref"` resolves to one of the 41 IDs in REFERENCES.md.
- [ ] Component-instance customization happens via `U(instance + "/childId", {...})` or descendants overrides — never by recreating the component shape inline.

If all 8 pass → done. If any fail → fix in a follow-up `batch_design` (or rebuild the offending operation) before declaring the work complete.

## Escape hatches (use sparingly, document inline)

There are exactly THREE situations where deviating from a token is permitted. Each requires a one-line `name` annotation explaining why.

1. **Absolute-position canvas geometry**. When using `layout: "none"` (e.g., a Bohr-model atom, a transit map station coordinate, an annotated callout), `x`/`y` are positions, not spacing — they may be any integer. Tag the parent frame name with `// reason: positional canvas`.

2. **Chart-specific accent colors**. The chart palette uses `$--chart-bar-1`, `$--chart-series-1`, `$--chart-bar-highlight`, etc. — use those. If a chart legitimately needs a sixth distinct hue (e.g., a 6-series stacked bar) and the palette only defines 3, request an extension to the palette FIRST. Do not inline a hex.

3. **Atlas-only metaphor accents**. The five atlases (Periodic Table, Transit Map, Blueprint Set, etc.) define their own metaphor-specific colors (e.g., AUTH-line green `#6BAF8A`, PROFILE-line red `#C06060`). Those atlas colors are themselves a documented secondary palette — they live in `REFERENCES.md` under "Atlas-only colors". Use them only inside their owning atlas frame; never leak them into product screens.

If your need falls outside these three, the answer is **add a token first, then design** — not "inline a hex".

## Required pattern: instance customization

When a refed component needs different content/colors/sizes than the master:

```javascript
// 1. Insert the ref
btn = I(parent, {type: "ref", ref: "SpHta"})

// 2. Override individual descendants (DO NOT rebuild the component)
U(btn + "/<labelChildId>", {content: "Save changes"})

// 3. For multiple overrides at insert time, use the descendants block:
btn = I(parent, {type: "ref", ref: "SpHta", descendants: {
  "<labelChildId>": {content: "Save changes"},
  "<iconChildId>":  {iconFontName: "check"}
}})
```

To find the descendant child IDs of a component, run `batch_get` with `readDepth: 3` on the component's master ID before customizing.

## Forbidden anti-patterns (with examples)

| Anti-pattern | Why it's forbidden | Do this instead |
|---|---|---|
| `fill: "#525252"` | Hardcoded color | `fill: "$--btn-primary-bg"` |
| `fontFamily: "JetBrains Mono"` | Hardcoded font | `fontFamily: "$--terminal-font"` |
| Building a button from a frame + text | Duplicates the design system | `type: "ref", ref: "SpHta"` and override label |
| `padding: 17` | Off-scale | `padding: 16` (snap to scale) |
| `cornerRadius: 5` | Off-scale | `cornerRadius: "$--terminal-radius"` |
| `fontSize: 15` | Off-scale | `fontSize: 14` or `16` |
| Inventing a "Terminal Modal" component | Hallucinated | Compose existing refs (`zpDfd` Card + `SpHta`/`zNQZK` buttons) |
| Copying a Terminal Button master and tweaking it inline | Drift from canonical | `type: "ref"` + descendants override |

## Why this matters

This skill exists because the bridge from design to Kotlin code only works if the mapping is **deterministic**:

- A design ref ID (`SpHta`) maps 1:1 to a Kotlin call (`TerminalButton(variant = Default)`).
- A token (`$--btn-primary-bg`) maps 1:1 to a Compose color (`TerminalTheme.colors.btnPrimaryBg`).
- A Pencil instance customization maps 1:1 to a composable parameter.

Every hardcoded hex, every off-scale spacing, every reinvented component breaks that chain. When the chain breaks, engineers stop trusting designs, designers stop trusting the system, and the design system stops being a system.

## Quick start template

For any new design, the first thing you write should look like this skeleton:

```javascript
// REFS USED:    Tig (eMdeB), Tb-d (SpHta), Tb-g (zNQZK), Tax (t4WAf)
// TOKENS USED: $--terminal-bg, $--terminal-text, $--terminal-font, $--terminal-radius

screen = I(document, {
  type: "frame",
  name: "Login Screen",
  fill: "$--terminal-bg",
  layout: "vertical",
  padding: 32,
  gap: 24,
  width: 1440,
  height: 900
})

// ... refs and tokens, never literals
```

If you can't write this header truthfully (because you used hex or invented a component), the design is not done.
