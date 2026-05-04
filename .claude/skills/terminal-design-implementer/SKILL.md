---
name: terminal-design-implementer
description: Use when implementing a Pencil design (.pen) in Kotlin Multiplatform code. Looks up which Composable to call and which TerminalTheme token to use for any Pencil ref/variable in `terminal_design_system.pen`. Reverse of `terminal-design-generator`. Read CODE-MAP.md before writing any Composable.
---

# Terminal Design Implementer

The reverse direction: given a Pencil design that uses ref `eMdeB` and token `$--terminal-bg`, this skill tells you to write `TerminalInput(...)` inside a `Box(Modifier.background(TerminalTheme.colors.bg))`. It is the lookup table that bridges design hand-off → Compose code.

## When this skill applies

- You are about to implement a screen, component, or section that already exists as a design in `terminal_design_system.pen` (or the design atlas).
- You are reading a `batch_get` result from Pencil and need to map every `type:"ref"` instance and every `$--token` to a Kotlin call.
- You are reviewing PR code and want to verify the engineer used the right ref → composable mapping.

If you are designing in Pencil (not implementing in Kotlin), use `terminal-design-generator` instead.

## Mission (one sentence)

Translate every Pencil ref ID and every `$--` token into the exact Kotlin Composable call and `TerminalTheme.<group>.<property>` accessor — no guesswork, no hardcoded `Color(0x...)`, no `RoundedCornerShape(4.dp)` inline.

## Three implementation rules

### Rule 1 — Compose what the design refs
Every Pencil `type:"ref"` you see in the design becomes the matching Kotlin Composable. The mapping is in `CODE-MAP.md` and is canonical. If the design says `ref:"SpHta"`, the code says `TerminalButton(variant = ButtonVariant.Default, onClick = {...})`. No alternatives.

### Rule 2 — Bind tokens, not values
Every `$--terminal-*` / `$--btn-*` / `$--card-*` etc. token in the design becomes a `TerminalTheme.<group>.<property>` lookup. Never resolve a token to its hex value and inline it in code — always go through the theme.

### Rule 3 — Honor the scale
Every `padding`/`gap`/`fontSize`/`cornerRadius` value in the design must come from a real Kotlin token. Use the scales in `CODE-MAP.md`:
- spacing: `TerminalTheme.spacing.{xs,sm,md,lg,xl}` (4/8/12/16/20)
- gap: `TerminalTheme.gap.{xs,sm,md,lg,xl}` (4/8/12/16/24)
- typography: `TerminalTheme.typography.{chartAxis,xxs,xs,sm,base,md,xxl}` (9/10/11/12/13/14/32 sp)
- radius: `TerminalTheme.radius.{none,xs,sm,md,lg,pill,full}` (0/2/4/6/12/24/9999 dp)

If a design uses `padding: 16`, the code says `Modifier.padding(TerminalTheme.spacing.lg)` — never `Modifier.padding(16.dp)`.

## Workflow (run in this exact order)

### Step 0 — Open the lookup table
**Always** read `CODE-MAP.md` (sibling file) before implementing. It is the authoritative bidirectional mapping between Pencil refs/tokens and Kotlin symbols. The companion `manifest.json` (in the sister skill `terminal-design-generator`) is the machine-readable version of the same data.

### Step 1 — Enumerate refs and tokens in the design
Before writing any Compose code, list every Pencil ref ID and every `$--` token referenced by the design. Format:

```
DESIGN INVENTORY (from batch_get of frame ABCDE):
- 1× ref "SpHta"       → TerminalButton(Default)
- 2× ref "eMdeB"       → TerminalInput
- 1× ref "t4WAf"       → TerminalAlert(Error)
- fill: $--terminal-bg → TerminalTheme.colors.bg
- padding: 16          → TerminalTheme.spacing.lg
- gap: 12              → TerminalTheme.gap.md
```

If any line has no resolution in CODE-MAP.md, stop and ask — never invent.

### Step 2 — Compose top-down
Write the Composable function tree mirroring the Pencil node tree. Each Pencil frame becomes a `Column`/`Row`/`Box` with the matching layout and tokens. Each ref becomes a Composable call. Customizations applied via Pencil descendants overrides become Composable parameters.

### Step 3 — Self-validate
Before reporting "done", scan your generated code for these violations:

- [ ] No `Color(0x...)` literals — all colors from `TerminalTheme.colors.*`
- [ ] No raw `FontFamily(...)` — text styles come from `TerminalTheme.typography.*`
- [ ] No inline `.sp` outside special letterSpacing — pick a typography style
- [ ] No raw `.dp` for paddings/gaps — use spacing/gap tokens
- [ ] No raw `RoundedCornerShape(N.dp)` — use `TerminalTheme.radius.*`
- [ ] Every Composable from `app:designsystem` matches a Pencil ref in the source design
- [ ] Customization happens via Composable parameters or trailing-lambda content slots — never by re-implementing the component

If any check fails → fix before declaring complete.

## Reverse pattern: ref instance → Composable invocation

Pencil source:
```javascript
btn = I(parent, {type: "ref", ref: "SpHta"})
U(btn + "/<labelChildId>", {content: "Sign in"})
```

Kotlin equivalent:
```kotlin
TerminalButton(
    variant = ButtonVariant.Default,
    onClick = { viewModel.onEvent(LoginEvent.Submit) },
    text = "Sign in",
)
```

The Pencil `descendants` block on a ref maps to **named parameters** of the Kotlin Composable. Find the parameter list in the Composable's source (`app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/<file>.kt`) — they are documented per-ref in `CODE-MAP.md`.

## When the design uses something not in CODE-MAP

If a design references something that's **not** in CODE-MAP, three possibilities:

1. **It's in `code_only_components`** (charts, dropdown, swipe reveal, etc.) — these have no Pencil ref but are valid Composables. Use them directly per `CODE-MAP.md → Code-only Composables`.
2. **It's a Pencil-only token** (`$--badge-*`, `$--btn-padding-x`, etc.) — the Pencil designer used a token that has no Kotlin counterpart yet. Implement using the closest semantic substitute (`$--badge-success-bg` → `TerminalTheme.colors.successBg`) and flag for follow-up.
3. **It's brand-new** — pause and surface to the team. Don't invent a new Composable; the design is ahead of the code, and the right answer is to update both `terminal-design-generator/manifest.json` AND add the new Composable to `app:designsystem` first.

## Quick start template

For any new Composable you're implementing from a design, the first thing you write should look like this:

```kotlin
// PENCIL DESIGN: terminal_design_system.pen → frame "Login Screen" (id: <ABCDE>)
// REFS USED:    eMdeB×2, SpHta, zNQZK, t4WAf
// TOKENS USED:  TerminalTheme.colors.bg, .colors.text, .spacing.lg, .gap.md, .typography.md

@Composable
fun LoginScreen(
    state: LoginModel,
    onIntent: (LoginIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalTheme.colors.bg)
            .padding(TerminalTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(TerminalTheme.gap.md),
    ) {
        // every node below is a Composable from app:designsystem
        TerminalInput(value = state.email, onValueChange = { onIntent(LoginIntent.Email(it)) }, label = "Email")
        TerminalInput(value = state.password, onValueChange = { onIntent(LoginIntent.Password(it)) }, label = "Password")
        if (state.error != null) {
            TerminalAlert(variant = AlertVariant.Error, text = state.error)
        }
        TerminalButton(variant = ButtonVariant.Default, onClick = { onIntent(LoginIntent.Submit) }, text = "Sign in")
        TerminalButton(variant = ButtonVariant.Ghost, onClick = { onIntent(LoginIntent.ForgotPassword) }, text = "Forgot?")
    }
}
```

If you cannot fill the `REFS USED` and `TOKENS USED` header truthfully (because you used hex or invented a Composable), the implementation is not done.

## Why this exists

Without this skill, engineers reading a Pencil design end up:
- Squinting at hex values in the design and pasting `Color(0xFF...)` into code (instant token drift)
- Re-implementing `TerminalButton` because they didn't know it existed
- Approximating `padding=16` with `padding(15.dp)` because no one told them the spacing scale exists
- Building a one-off `Card` from `Box` + `border` because nobody pointed at `TerminalCard`

CODE-MAP.md collapses all of those failure modes into a single deterministic lookup. Every design ref → one Composable. Every design token → one `TerminalTheme.<group>.<property>` accessor. The chain holds.
