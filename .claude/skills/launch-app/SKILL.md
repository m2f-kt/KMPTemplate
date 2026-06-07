---
name: launch-app
description: Launch the full app for dev/testing — backend + JVM Desktop frontend (Hot Reload) on free ports, then drive the running UI via the compose-hot-reload MCP. Use whenever asked to run/start/launch the app, bring up the stack, test or verify a screen end-to-end, reproduce a bug against a live backend, or interact with the running window. For frontend-only driving see launch-desktop; this skill also starts and wires the backend.
---

# launch-app — run backend + hot-reload frontend, drive via MCP

Brings up the whole stack for interactive dev/testing:
- **Backend** (`:server:run`) on a free port (prefers `.env` `PORT`/8080; next free port if busy).
- **Frontend** (JVM Desktop, **Hot Reload** enabled) via the `compose-hot-reload` MCP — launched and driven through MCP tools.
- The desktop app auto-targets whatever port the backend landed on (no recompile): `dev-run.sh` writes the URL to `$TMPDIR/template-dev-server.url` and `PlatformConfig.jvm.kt#defaultBaseUrl` reads it (env `SERVER_BASE_URL` overrides).

## Prerequisites (check, don't assume)
1. Docker services up (Postgres/MinIO/MailHog): `./gradlew devUp` if the backend needs them.
2. The `compose-hot-reload` MCP server is configured in `.mcp.json` (task `:composeApp:hotMcpServerJvm`, plugin `composeHotReload` ≥ 1.2.0-alpha01). If the MCP tools aren't available in this session, tell the user to reload so Claude Code picks up `.mcp.json`.

## Steps

### 1. Start the backend on a free port
```bash
bash scripts/dev-run.sh
```
This picks a free port, writes the url-file, starts `:server:run` detached, and waits until it answers HTTP. Note the printed `backend : http://localhost:<port>`. (Add `--web` to also start the wasmJs web frontend on a free port. Stop later with `bash scripts/dev-run.sh --stop`.)

If the backend log shows it needs Docker (DB/MinIO), run `./gradlew devUp` first, then re-run.

### 2. Launch + drive the desktop frontend via the compose-hot-reload MCP
The `compose-hot-reload` MCP runs the desktop app with Hot Reload and exposes UI tools. Use them to launch and interact:
- `status` — is the app running / connected.
- `get_semantic_tree` — read the current UI tree (find nodes to act on).
- `click` / `type_text` / `scroll` — perform the actions the user asked for.
- `take_screenshot` — verify visually.

Typical loop: `get_semantic_tree` → locate the target → `click`/`type_text` → `take_screenshot` to confirm. Because Hot Reload is on, code edits to `composeApp`/`app:*` reflect live — re-`take_screenshot` after an edit instead of restarting.

The app reads the url-file from step 1, so it talks to the backend you just started even on a non-default port.

> Fallback if the MCP can't reach a surface (OS dialogs, native windows): drive directly with
> `./gradlew :composeApp:hotRunJvm --mainClass=com.m2f.template.MainKt` and use `mcp__computer-use__*`.

### 3. Teardown
```bash
bash scripts/dev-run.sh --stop   # stops the backend started in step 1
```
The MCP-launched frontend is stopped by the MCP/session.

## Notes
- Port logic lives in `scripts/dev-run.sh` (`lsof`-based free-port scan, macOS + Linux).
- Only the backend's port is dynamic; the desktop app discovers it via the url-file / `SERVER_BASE_URL`.
- For frontend-only interaction against an already-running backend, use the **launch-desktop** skill.
