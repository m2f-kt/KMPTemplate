---
name: sync-design
description: Audit (or apply) the design ↔ code mapping for terminal_design_system.pen ↔ app:designsystem. Defaults to read-only audit; pass `--apply` to write patches.
---

Invoke the `terminal-design-sync` skill with the user's argument string `$ARGUMENTS`.

If `$ARGUMENTS` is empty → run audit mode (read-only). Produce the diff report at `.claude/skills/terminal-design-sync/last-sync-report.md` and summarize the counts in chat. Ask the user before applying anything.

If `$ARGUMENTS` contains `--apply` → run the full audit → apply → verify pipeline. Show the diff first, get confirmation, then patch `manifest.json`, `REFERENCES.md`, and `CODE-MAP.md`. Re-run the 3 verification agents at the end.

If `$ARGUMENTS` contains `--verify` → run audit + spawn the 3 verification agents (system / style / mapping). No writes.

Read `.claude/skills/terminal-design-sync/SKILL.md` for the workflow and apply-mode safety rules. Never violate the safety rules — they are non-negotiable.
