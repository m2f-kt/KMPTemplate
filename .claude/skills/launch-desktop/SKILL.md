---
name: launch-desktop
description: Run/launch the JVM Desktop composeApp with Compose Hot Reload and drive its UI for testing, debugging, verifying, or checking — via the compose-hot-reload MCP (status / get_semantic_tree / click / type_text / scroll / take_screenshot). Use whenever asked to run the app, test a screen, verify a UI change, reproduce a desktop bug, or interact with the running window. Falls back to mcp__computer-use__* only for surfaces the MCP can't reach (OS dialogs, native windows outside the main Compose tree).
---

# Launch Desktop (JVM) — Hot Reload + MCP

Canonical desktop run/verify pipeline. **Run with Compose Hot Reload, drive the UI with the
`compose-hot-reload` MCP.** Edits to `composeApp/src/jvmMain` / `commonMain` (and any module the app
depends on) hot-reload into the live window — no relaunch across many edit→verify iterations.

App identity: main class `com.m2f.template.MainKt`. Runs on JBR (the CHR plugin provisions it —
`~/.gradle/jdks/jetbrains_s_r_o_-25-*`).

> The `compose-hot-reload` MCP server is wired separately (project `.mcp.json`). This skill assumes it
> is already configured; it does not set the server up.

## HARD RULE — one instance only

Never run two app instances. The app holds a SingleInstance lock that refuses a second start (see
`composeApp/src/jvmMain/.../desktop/SingleInstanceGuard.kt`) — do not rely on it; check first.

```bash
pgrep -fl "com.m2f.template.MainKt" | grep -v grep   # empty = none running
```

So pick ONE of the two run modes below per session.

## Run mode A — Attach (user runs via IntelliJ CHR)

Default when the user already hit Run in IntelliJ. Do nothing to launch — just attach:

```text
mcp__compose-hot-reload__status()        # {"connected": true} → ready
```

## Run mode B — I run it (autonomous)

When no instance is up and you need to run it yourself:

```bash
./gradlew :composeApp:hotRunJvm --auto    # run_in_background: true
```

- `--auto` = auto-reload on file change (so edits apply live without a manual reload trigger).
- Background it; watch boot via `Monitor` until the window is up — do NOT poll with sleep loops.
- Entry point auto-detected — no `--mainClass` needed (add it only if it ever complains).
- `hotRunJvm --auto` is detach-safe for headless background launch (boots on JBR 25 and the
  `compose-hot-reload` MCP attaches). Unlike `hotDevJvm` (wants an interactive run), `hotRunJvm` is
  background-friendly. Still honor the one-instance rule.

## First MCP connect is slow

The `compose-hot-reload` MCP server runs `./gradlew … :composeApp:hotMcpServerJvm`. First connect
of a session is a **cold config-cache build (~2-3 min)** → it can exceed the MCP client timeout. Once
warm it reconnects in ~12s ("Reusing configuration cache"). If `status` fails at session start:
wait, or `/mcp` reconnect once warm. Config is in project `.mcp.json`.

## Drive the UI — the MCP loop

```text
1. mcp__compose-hot-reload__status()              # confirm connected
2. mcp__compose-hot-reload__get_semantic_tree()   # JSON: id, role, text, actions, bounds, states
3. act on a node id from the tree:
   - mcp__compose-hot-reload__click(nodeId)        # node must list "onClick" in actions
   - mcp__compose-hot-reload__long_click(nodeId)   # needs "onLongClick"
   - mcp__compose-hot-reload__type_text(nodeId, text)  # REPLACES the field (needs editableText)
   - mcp__compose-hot-reload__scroll(nodeId, deltaX, deltaY)   # needs ScrollBy; +Y = down
   - mcp__compose-hot-reload__scroll_to_index(nodeId, index)   # LazyColumn/Row; 0-based
4. mcp__compose-hot-reload__take_screenshot()     # verify the new state
```

Screenshot after a meaningful state change, not in tight loops. `get_semantic_tree` is cheaper than a
screenshot for asserting text/presence/state — prefer it for assertions, screenshot for visual
fidelity (color, borders, complex layout).

## Edit → verify loop

1. Edit `.kt`. (CHR + `--auto`, or IntelliJ's gutter, applies it live.)
2. `get_semantic_tree` / `take_screenshot` to confirm the change landed.
3. If the tree looks stale, the reload may need a beat — re-screenshot; for structural changes a
   recompose is usually automatic.

## Recipes

- **Navigate:** `get_semantic_tree` → find the nav `Tab` node (role "Tab", e.g. "Settings") →
  `click(nodeId)` → `take_screenshot`.
- **Verify a screen change:** navigate to it → `take_screenshot`, compare against the design/expectation.
- **Form/login:** find email field node → `type_text` → find password node → `type_text` → find the
  submit button (has `onClick`) → `click` → `take_screenshot`.
- **Locale switch:** drive Settings → Language in the UI and screenshot to confirm `StringKey`
  resources resolve. Don't poke `PreferencesStorage` directly — that's an integration test.

## Teardown (mode B only)

```bash
pkill -f "com.m2f.template.MainKt"   # or stop the background bash task by id
```

Never `pkill -9 java` / `cmd+q` — kills the gradle daemon and other JVM tooling.

---

## Appendix: computer-use fallback (only when the MCP can't reach it)

The `compose-hot-reload` MCP sees ONLY the Compose semantic tree of the main app window. Use
`mcp__computer-use__*` for surfaces outside it:

- **Native windows outside the main Compose tree** — any separate `Window`/dialog that is not part of
  the main window's semantic tree. Verify with a full-desktop `mcp__computer-use__screenshot`.
- **OS dialogs** — permission prompts, file pickers.

Flow: `mcp__computer-use__request_access(apps: ["java", "OpenJDK Platform binary"], reason: "…")`
→ `open_application` → `screenshot` → `left_click` / `key` / `computer_batch`. Tier `full` (non-IDE).
Prefer key chords over pixel-clicking chrome; `cmd+w` to close a window, never `cmd+q`.
