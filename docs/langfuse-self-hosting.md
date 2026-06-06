# Langfuse observability (self-hosted, opt-in)

The template ships generic Langfuse + OpenTelemetry tracing in `server:core:observability`.
It is **opt-in**: with no Langfuse keys set the OTel exporter is never installed and the
server runs byte-identically.

## Quick start (local self-hosted)

```bash
./gradlew devUpLangfuse        # brings up the `langfuse` compose profile (web on http://localhost:3000)
```

This starts a fully self-contained stack behind the `langfuse` Docker Compose profile —
`langfuse-web` (port 3000) plus internal `langfuse-postgres`, `langfuse-clickhouse`,
`langfuse-redis`, and a dedicated `langfuse-minio` (no port clash with the app's own
Postgres/MinIO/MailHog from `./gradlew devUp`). The web container is headless-initialized
on first boot with an org/project and the dev API keys below — no manual UI step.

The default `./gradlew devUp` does **not** start Langfuse, so day-to-day dev stays lean.

## Configuration (`.env`)

```
LANGFUSE_HOST=http://localhost:3000
LANGFUSE_PUBLIC_KEY=pk-lf-local-dev
LANGFUSE_SECRET_KEY=sk-lf-local-dev
LANGFUSE_ENVIRONMENT=development
```

`Env.Observability.langfuseEnabled` is derived: both keys present ⇒ enabled. Keyless ⇒
disabled ⇒ exporter not installed.

**Precedence gotcha:** a shell-exported `LANGFUSE_*` overrides `.env`. `:server:run`
injects the committed `.env` into the JVM (via `EnvLoader`), so `.env` wins there; for
other entry points run `unset LANGFUSE_PUBLIC_KEY LANGFUSE_SECRET_KEY LANGFUSE_HOST`
if you previously exported cloud keys.

To use **Langfuse Cloud** instead, point `LANGFUSE_HOST` at `https://cloud.langfuse.com`
and use your project keys (skip `devUpLangfuse`).

## Wiring it into an agent

`server:core:observability` provides the building blocks; the consuming feature module
installs them on its Koog agent:

- `ProcessLifetimeOtelSdk.forLangfuse(LangfuseConfig(...))` — a process-lifetime OTel SDK
  with the `NonClosingOpenTelemetry` wrapper + simple span processor (the interlocking fix
  that prevents flat/partial traces). Call `setShutdownOnAgentClose(true)` on the feature.
- `LangfuseSpanAdapter` — reshapes Koog spans for self-hosted Langfuse v3. Two baked-in
  gotchas: `trace.output` is stamped on the **inference** span (not just the root), and
  prompt/node identity goes through **freeform metadata**, never the reserved int field.
- `TracePrivacy.traceContentAllowed(environment, consent)` + `buildTraceAttributes(...)` —
  redaction gate so prompt/response content is only attached when policy allows.

## Prompt management + evals (optional)

`server:core:observability` also ships the Langfuse Public-API REST clients
(`LangfusePromptClient` / `ScoresClient` / `DatasetClient`), a hot-path-free
`LangfusePromptProvider` (stale-on-error, falls back to a constant), and a generic
eval/judge harness (`Judge` interface, `JudgeSuite`/`allPass`, `PromotionGate`).
Supply your own `PromptCatalog` (defaults: `EmptyPromptCatalog` / `ExamplePromptCatalog`)
and domain `Judge`s; `FaithfulnessJudge` + `InjectionResistanceJudge` ship as defaults.

See the `koog-langfuse-tracing` and `langfuse-prompt-management` skills for the full
rationale and end-to-end verification steps.

## Security note

The `SALT` / `ENCRYPTION_KEY` / `NEXTAUTH_SECRET` / `*_PASSWORD` in the compose profile are
**local-dev values** — regenerate them (`openssl rand -hex 32`) for any non-local deployment.
