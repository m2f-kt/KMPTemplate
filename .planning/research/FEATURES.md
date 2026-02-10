# Feature Research

**Domain:** KMP Full-Stack Project Template (Ktor server + Compose Multiplatform client)
**Researched:** 2026-02-10
**Confidence:** MEDIUM-HIGH

## Feature Landscape

### Table Stakes (Users Expect These)

Features users assume exist. Missing these = template feels like a starter-kit toy, not a production foundation.

| Feature | Why Expected | Complexity | Notes |
|---------|--------------|------------|-------|
| **Type-safe navigation** | Every CMP template ships navigation. Users refuse to wire it themselves. Without it the app is a single screen. | MEDIUM | Navigation 3 is the forward-looking choice (Alpha on non-Android in CMP 1.10.0, stable on Android). Voyager and Decompose are mature alternatives but represent legacy direction. Use Navigation 3 -- it aligns with JetBrains roadmap and already has Koin integration. |
| **Dependency injection (client-side)** | Server already has Koin. Users expect the client to mirror it. ViewModels without DI is a non-starter. | LOW | Koin Compose Multiplatform (`koin-compose` + `koin-compose-viewmodel`) is the only serious KMP DI with CMP ViewModel support. Already using Koin server-side so this is a natural extension. |
| **Ktor client HTTP layer** | Template has a Ktor server -- users expect a matching client that talks to it. Shared serialization models in `:shared` is the entire point of KMP full-stack. | MEDIUM | Already have `ktor-client` bundle in version catalog. Need: configured HttpClient with auth interceptor, content negotiation, logging. Platform engine selection (CIO for JVM, Darwin for iOS, Js for WASM). |
| **Complete auth flow (login/register/token refresh)** | JWT skeleton exists but no user-facing flow. Auth is the first thing every user tests. If `clone -> run` hits an unfinished auth screen, they leave. | HIGH | Server: user table, registration endpoint, login endpoint, refresh token rotation. Client: login/register screens, token storage, automatic 401 retry with refresh. Mutex-guarded refresh to prevent races. |
| **User management (basic)** | Inseparable from auth. Users expect profile view, password change, logout at minimum. | MEDIUM | Depends on auth flow completion. Server: user CRUD, password update. Client: profile screen, settings. |
| **Shared data models** | The `:shared` module exists but is empty (just `Greeting.kt`). Users expect request/response DTOs shared between client and server. | LOW | kotlinx.serialization `@Serializable` data classes in `:shared`. Already have serialization plugin configured. |
| **Error handling pattern** | Arrow is already in the stack. Users expect a consistent Either/Raise pattern, not ad-hoc try/catch. | MEDIUM | Server already uses `DomainError` with Arrow. Client SDK needs matching `Either<ApiError, T>` responses. Shared error types in `:shared`. Arrow `Raise` DSL for composable error handling. |
| **Material 3 theming** | CMP ships Material3 by default. Users expect dark/light theme toggle, custom color scheme, and typography that looks professional. | LOW | Already using `compose.material3`. Need: custom `Theme.kt` with dynamic color support, dark/light toggle, typography scale. |
| **Local key-value storage** | Every app needs to persist auth tokens, user preferences, onboarding state. Without it the template can't even stay logged in across app restarts. | LOW | DataStore Preferences for KMP (stable since 1.1.0). Platform-specific path providers via expect/actual. Simpler than SQLDelight for key-value needs. |
| **Docker Compose dev environment** | `docker-compose.yml` exists. Users expect `docker compose up` to start PostgreSQL + server and be ready. | LOW | Already partially in place. Ensure postgres service, server service, health checks, and seed data work out of the box. |
| **OpenAPI documentation** | Already configured with Ktor OpenAPI plugin. Users expect working Swagger UI at `/docs`. | LOW | Already in place. Ensure generated spec stays in sync with routes. |
| **Testing infrastructure** | Kotest, Testcontainers, Ktor test host already in version catalog. Users expect example tests that actually run. | MEDIUM | Server: integration test with Testcontainers PostgreSQL. Client: ViewModel unit tests with fake repositories. Shared: model serialization tests. |

### Differentiators (Competitive Advantage)

Features that set this template apart from KMPShip, Multiplatform Kickstarter, AppKickstarter, and JetBrains Wizard. Not required, but this is where you win.

| Feature | Value Proposition | Complexity | Notes |
|---------|-------------------|------------|-------|
| **Koog AI agent infrastructure** | No other KMP template ships with AI agent infrastructure. Koog is JetBrains' official framework, has a native Ktor plugin (`ai.koog:koog-ktor`), and supports tool calling, MCP, sub-agents, persistence, and history compression. This is the single biggest differentiator. | HIGH | Install Koog Ktor plugin on server. Provide: sample agent route with ReAct strategy, tool registration example, MCP integration example, streaming response support. Configure via `application.yaml`. Multi-provider support (OpenAI, Anthropic, Google, Ollama). |
| **Either-based client SDK** | Most templates use raw Ktor calls or basic Result. A typed `ApiClient` returning `Either<ApiError, T>` with Arrow `Raise` DSL is dramatically better DX. Combined with shared error types this eliminates an entire class of bugs. No competitor does this. | MEDIUM | Build `ApiClient` in `:shared` or new `:sdk` module. Wrap Ktor HttpClient. Map HTTP errors to sealed `ApiError` hierarchy. Use `either { }` blocks in ViewModels. Token refresh interceptor returns Either, not exceptions. |
| **Setup CLI / init script** | JetBrains Wizard generates projects but cannot customize an existing template (rename packages, set project name, configure DB credentials, generate secrets). A `./init.sh` or Gradle task that does this turns a 30-minute manual process into 60 seconds. Multiplatform Kickstarter has scripts but they are basic. | MEDIUM | Kotlin script or shell script that: renames root package from `com.m2f.template` to user's package, updates `applicationId`, sets project name in `settings.gradle.kts`, generates JWT secret, creates `.env` from `.env.example`, runs `docker compose up -d`. |
| **Sample dashboard screen** | Users want to see a working, data-driven screen after clone. A dashboard with real API calls, state management, loading/error states proves the architecture works end-to-end. No OSS template ships a full-stack working dashboard. | MEDIUM | Dashboard screen showing data fetched from server via SDK. Uses Navigation 3, ViewModel with Koin, Either error handling, loading/error/success states. Pull-to-refresh. Responsive layout (phone vs tablet/desktop). |
| **Arrow-based server error handling with context receivers** | The existing `DomainError` pattern using Kotlin context parameters is ahead of the curve. Most templates use basic exception handlers. This is a genuine architectural advantage worth showcasing. | LOW | Already partially built. Extend to cover all common error cases. Document the pattern. Ensure new developers understand it. |
| **Compose Hot Reload** | Already in the plugin list (`composeHotReload`). Most templates don't enable it. Sub-second UI iteration is a massive DX win. | LOW | Already configured. Ensure it works reliably. Document usage. |
| **Structured observability** | Ktor monitoring bundle is configured (call logging, call ID, Micrometer/Prometheus). Combined with Koog's OpenTelemetry support, this gives production-grade observability from day one. No competitor template ships this. | MEDIUM | Already partially in place. Add: structured logging with correlation IDs, Prometheus endpoint, Grafana dashboard config, Koog agent tracing. |
| **R2DBC / reactive database** | Using Exposed R2DBC is unusual and forward-looking. Most templates use blocking JDBC. Reactive database access under coroutines is a genuine technical differentiator. | LOW | Already configured. Ensure migration system works. Add example reactive queries. |
| **Multi-target client** | Android, iOS, Desktop (JVM), WASM/Web from one codebase. Most competitor templates target only Android + iOS. Desktop and Web targets are genuine extras. | LOW | Already configured in `composeApp/build.gradle.kts`. Ensure all targets compile and run. |

### Anti-Features (Commonly Requested, Often Problematic)

Features that seem good but create problems in a template context.

| Feature | Why Requested | Why Problematic | Alternative |
|---------|---------------|-----------------|-------------|
| **In-app purchases / payments** | KMPShip includes RevenueCat integration. Users ask for monetization. | Payment SDKs are platform-specific, version-sensitive, require merchant accounts, and break on every major OS update. Tight coupling to RevenueCat/Stripe locks users in. A template should not make payment vendor choices. | Document where payment integration hooks should go. Provide the auth + user management foundation that payments build on. Link to RevenueCat KMP guide. |
| **Push notifications** | KMPShip includes them. Expected for mobile apps. | Firebase/APNs setup requires account credentials, certificates, and platform-specific configuration that cannot work out of the box in a template. Broken push notifications in a template are worse than no push notifications. | Provide the server-side notification model and API. Document Firebase/APNs integration guide. Keep client notification handling as an add-on module, not core. |
| **Analytics SDK integration** | Templates like KMPShip bundle analytics. | Specific analytics SDKs (Firebase Analytics, Mixpanel) require account setup and API keys. They add weight, require privacy disclosures, and the choice is highly vendor-specific. | Provide an analytics abstraction interface (`AnalyticsTracker`) in `:shared` with a no-op default. Document how to plug in Firebase/Mixpanel/Amplitude. Server-side: Prometheus metrics already covers backend analytics. |
| **Full ORM / database abstraction** | Developers want Room or full Exposed DAO patterns. | Over-abstracting the database in a template hides how it works and makes customization harder. Templates should teach patterns, not hide them. | Ship thin repository layer over Exposed. Show raw queries for learning. Provide migration example. Let users add DAO layer if desired. |
| **GraphQL** | Some users prefer GraphQL over REST. | Adds massive complexity (schema definition, code generation, resolver layer). Forces an opinionated choice that alienates REST users. Ktor's REST + OpenAPI is already well-configured. | Keep REST + OpenAPI. If GraphQL is needed, it can be added as a separate module. The Ktor client is transport-agnostic. |
| **Multi-module feature architecture** | Enterprise templates like openMF have deep module hierarchies. | Over-modularization in a template increases build times, complicates navigation between features, and overwhelms new users. A template should be graspable in 15 minutes. | Start with 4-5 modules (`:shared`, `:composeApp`, `:server`, `:server:core:*`). Document how to extract feature modules when the app grows. Provide the DI infrastructure that makes extraction easy. |
| **Internationalization / i18n** | Multiplatform Kickstarter includes FIGS translations. | Translation files are content, not architecture. Bundling them in a template adds noise. The Compose resources system already supports it natively. | Document how to use Compose Multiplatform resources for i18n. Don't bundle translation files. |
| **CI/CD pipeline** | Several competitors include GitHub Actions. | CI/CD is highly environment-specific (GitHub vs GitLab vs Bitbucket, self-hosted vs cloud). A template's CI config often needs to be rewritten entirely. | Provide a minimal `.github/workflows/ci.yml` that runs tests and builds. Don't include deployment workflows -- those depend on infrastructure choices the template cannot know. |
| **Social login (Google/Apple/GitHub)** | KMPShip bundles social auth. | Each social provider requires OAuth app registration, platform-specific SDKs (Google Sign-In, Sign in with Apple), and credentials that do not work out of the box. | Ship email/password auth that works immediately. Document social login integration points. Provide the server-side OAuth callback structure but don't bundle platform SDKs. |

## Feature Dependencies

```
[Shared Data Models]
    |
    +--requires--> [kotlinx.serialization config] (already done)
    |
    +--enables--> [Ktor Client HTTP Layer]
    |                 |
    |                 +--requires--> [Platform engine selection]
    |                 |
    |                 +--enables--> [Either-based Client SDK]
    |                                   |
    |                                   +--requires--> [Arrow core in :shared]
    |                                   |
    |                                   +--enables--> [Auth Flow (client)]
    |                                   |                 |
    |                                   |                 +--requires--> [Local Storage (token persistence)]
    |                                   |                 |
    |                                   |                 +--requires--> [Auth Flow (server)]
    |                                   |                 |                 |
    |                                   |                 |                 +--requires--> [User table + migrations]
    |                                   |                 |                 |
    |                                   |                 |                 +--requires--> [JWT security] (already done)
    |                                   |                 |
    |                                   |                 +--enables--> [User Management]
    |                                   |
    |                                   +--enables--> [Sample Dashboard]
    |                                                     |
    |                                                     +--requires--> [Navigation]
    |                                                     |
    |                                                     +--requires--> [DI (client-side)]
    |                                                     |
    |                                                     +--requires--> [Material 3 Theming]
    |
    +--enables--> [Koog AI Agent Infrastructure]
                      |
                      +--requires--> [Ktor server] (already done)
                      |
                      +--requires--> [Koog Ktor plugin]
                      |
                      +--enhances--> [Sample Dashboard] (AI-powered features demo)

[Setup CLI] -- independent, can be built at any phase

[Compose Hot Reload] -- independent, already configured

[Observability] -- independent, already partially configured
    |
    +--enhances--> [Koog AI Agents] (OpenTelemetry tracing)
```

### Dependency Notes

- **Client SDK requires Shared Data Models:** The SDK wraps API calls using shared DTOs. Models must exist first.
- **Auth Flow requires both Client SDK and Server endpoints:** Full-stack feature. Server endpoints must exist before client can consume them. Token storage (local storage) must exist before client can persist sessions.
- **Sample Dashboard requires Navigation + DI + SDK + Theming:** This is the integration proof. It sits at the top of the dependency chain and should be built last.
- **Koog AI agents are independent of client features:** Server-only feature that can be built in parallel with client work. Only connects to dashboard if you want an AI-powered demo screen.
- **Setup CLI is fully independent:** Can be built at any time. Does not depend on or block other features.

## MVP Definition

### Launch With (v1)

Minimum viable template -- what's needed for `clone -> run -> see working app`.

- [x] Shared data models (request/response DTOs) -- enables full-stack type sharing
- [ ] Ktor client HTTP layer with platform engines -- enables client-server communication
- [ ] Either-based client SDK -- the DX differentiator, builds on Ktor client
- [ ] Navigation 3 integration -- app needs multiple screens
- [ ] Koin client-side DI with ViewModel support -- ViewModels need injection
- [ ] Complete auth flow (server + client) -- login/register/token refresh end-to-end
- [ ] Local key-value storage (DataStore) -- token persistence, user preferences
- [ ] Material 3 theming (dark/light) -- professional look out of the box
- [ ] Basic user management (profile, logout) -- inseparable from auth
- [ ] Docker Compose polish -- `docker compose up` must just work
- [ ] Setup CLI / init script -- first-run experience

### Add After Validation (v1.x)

Features to add once core architecture is proven end-to-end.

- [ ] Koog AI agent infrastructure -- biggest differentiator, but not blocking core app functionality
- [ ] Sample dashboard screen -- proves the architecture, but requires all v1 pieces working first
- [ ] Structured observability (Prometheus dashboard, correlation IDs) -- production concern, not template validation
- [ ] Testing examples (integration + unit + shared) -- important but secondary to working app

### Future Consideration (v2+)

Features to defer until the template has users and feedback.

- [ ] Analytics abstraction interface -- design once users report actual analytics needs
- [ ] Notification server model -- design once push notification patterns stabilize in KMP
- [ ] Additional AI agent examples (multi-agent, persistence, MCP tools) -- expand after basic agent infra proves useful
- [ ] Adaptive layouts (phone/tablet/desktop responsive) -- Material3 Adaptive is still maturing in CMP

## Feature Prioritization Matrix

| Feature | User Value | Implementation Cost | Priority |
|---------|------------|---------------------|----------|
| Complete auth flow (server + client) | HIGH | HIGH | P1 |
| Either-based client SDK | HIGH | MEDIUM | P1 |
| Navigation 3 integration | HIGH | MEDIUM | P1 |
| Koin client-side DI | HIGH | LOW | P1 |
| Shared data models | HIGH | LOW | P1 |
| Local key-value storage | HIGH | LOW | P1 |
| Material 3 theming | MEDIUM | LOW | P1 |
| Setup CLI / init script | HIGH | MEDIUM | P1 |
| Koog AI agent infrastructure | HIGH | HIGH | P2 |
| Sample dashboard screen | HIGH | MEDIUM | P2 |
| User management (profile/settings) | MEDIUM | MEDIUM | P2 |
| Testing examples | MEDIUM | MEDIUM | P2 |
| Structured observability | MEDIUM | MEDIUM | P2 |
| Docker Compose polish | MEDIUM | LOW | P1 |
| Compose Hot Reload docs | LOW | LOW | P3 |
| Analytics abstraction | LOW | LOW | P3 |

**Priority key:**
- P1: Must have for launch -- without these the template is incomplete
- P2: Should have, add when possible -- these are differentiators and polish
- P3: Nice to have, future consideration

## Competitor Feature Analysis

| Feature | KMPShip | Multiplatform Kickstarter | AppKickstarter (OSS) | JetBrains Wizard | Our Approach |
|---------|---------|--------------------------|----------------------|------------------|--------------|
| Auth flow | Full (Apple/Google/Email) | Basic (login/register) | None | None | Email/password with JWT refresh. Server + client end-to-end. Social login documented, not bundled. |
| Navigation | Custom (unspecified) | Voyager | Voyager | None | Navigation 3 (forward-looking, JetBrains-backed) |
| DI | Koin | Koin | Koin | None | Koin (server + client unified) |
| Local storage | Room | Unspecified | SQLDelight + multiplatform-settings | None | DataStore Preferences (lightweight, official Google/JetBrains) |
| Client SDK pattern | Raw HTTP calls | Raw Ktor client | Raw Ktor client | None | `Either<ApiError, T>` with Arrow Raise DSL -- unique in market |
| AI integration | "AI-ready architecture" (no actual AI) | None | None | None | Full Koog infrastructure with working agent routes, tool calling, MCP -- genuine AI capability |
| Error handling | Basic try/catch | Basic | Basic | None | Arrow Either + context parameters + sealed DomainError hierarchy -- most sophisticated in market |
| Server framework | None (client-only) | Ktor (basic) | None (client-only) | None | Ktor with OpenAPI, R2DBC, migrations, monitoring -- most complete server |
| Setup experience | Manual | Scripts (basic) | Manual | Wizard (generates new project) | Init script: rename, configure, start -- best for template use case |
| Payments | RevenueCat | None | None | None | Deliberately excluded. Documented integration points. |
| Push notifications | Included | None | None | None | Deliberately excluded. Server model only. |
| Targets | Android + iOS | Android + iOS | Android + iOS + Desktop | Configurable | Android + iOS + Desktop + WASM/Web -- most targets |
| Testing | Setup only | None | None | None | Working examples with Testcontainers, Kotest, Ktor test host |
| Observability | None | None | None | None | Prometheus, structured logging, call tracing, Koog OpenTelemetry |

## Sources

- [JetBrains KMP Wizard](https://kmp.jetbrains.com/) -- official project generator, baseline comparison
- [KMPShip](https://www.kmpship.app) -- commercial KMP starter kit, feature-rich but client-only (MEDIUM confidence, commercial product)
- [Multiplatform Kickstarter](https://multiplatformkickstarter.com/) -- commercial full-stack template with Ktor (MEDIUM confidence, commercial product)
- [AppKickstarter OSS Template](https://github.com/AppKickstarter/Kotlin-Multiplatform-Template) -- open-source KMP template with Voyager, SQLDelight, Koin (HIGH confidence, GitHub)
- [openMF KMP Project Template](https://github.com/openMF/kmp-project-template) -- enterprise multi-module template (HIGH confidence, GitHub)
- [Compose Multiplatform 1.10.0 Release](https://blog.jetbrains.com/kotlin/2026/01/compose-multiplatform-1-10-0/) -- Navigation 3 multiplatform support (HIGH confidence, JetBrains blog)
- [Navigation 3 in CMP Docs](https://kotlinlang.org/docs/multiplatform/compose-navigation-3.html) -- official Navigation 3 documentation (HIGH confidence, official docs)
- [Koog Documentation](https://docs.koog.ai/) -- official Koog AI agent framework docs (HIGH confidence, official docs)
- [Koog Ktor Plugin](https://docs.koog.ai/ktor-plugin/) -- Ktor integration details (HIGH confidence, official docs)
- [Koin Compose Multiplatform](https://insert-koin.io/docs/quickstart/cmp/) -- Koin CMP setup guide (HIGH confidence, official docs)
- [Koin Navigation 3 Integration](https://insert-koin.io/docs/reference/koin-compose/navigation3/) -- Koin + Nav3 (HIGH confidence, official docs)
- [DataStore for KMP](https://developer.android.com/kotlin/multiplatform/datastore) -- official DataStore KMP guide (HIGH confidence, official docs)
- [Arrow Either for Error Handling](https://proandroiddev.com/how-to-use-arrows-either-for-exception-handling-in-your-application-a73574b39d07) -- Either pattern guide (MEDIUM confidence, community article)
- [Ktor Full-Stack KMP Tutorial](https://ktor.io/docs/full-stack-development-with-kotlin-multiplatform.html) -- official Ktor full-stack guide (HIGH confidence, official docs)
- [Token Refresh with Ktor KMP](https://medium.com/@lahirujay/token-refresh-implementation-with-ktor-in-kotlin-multiplatform-mobile-f4d77b33b355) -- token refresh pattern (LOW confidence, single community source)

---
*Feature research for: KMP Full-Stack Project Template*
*Researched: 2026-02-10*
