# Project State

## Project Reference

See: .planning/PROJECT.md (updated 2026-02-10)

**Core value:** A developer can clone this template, run the setup CLI, and immediately have a working full-stack app with auth, database, DI, AI agents, and a component library -- no infrastructure decisions required.
**Current focus:** Phase 8 - Type-Safe Shared Routes (gap closure)

## Current Position

Phase: 8 (Type-Safe Shared Routes)
Plan: 3 of 3 in current phase (3 complete)
Status: Phase 8 Complete
Last activity: 2026-02-15 - Completed 08-03 (SDK Client Migration)

Progress: [████████████████████] 100% (38/38 plans)

## Performance Metrics

**Velocity:**
- Total plans completed: 38
- Average duration: ~7 min
- Total execution time: ~262 min

**By Phase:**

| Phase | Plans | Total | Avg/Plan |
|-------|-------|-------|----------|
| 1 | 4/4 | ~46 min | ~12 min |
| 2 | 3/3 | ~58 min | ~19 min |
| 3 | 3/3 | ~15 min | ~5 min |
| 4 | 7/7 | ~36 min | ~5 min |
| 5 | 11/11 | ~50 min | ~5 min |
| 6 | 3/3 | ~24 min | ~8 min |
| 6.1 | 2/2 | ~5 min | ~3 min |
| 7 | 2/2 | ~14 min | ~7 min |

| 8 | 3/3 | ~11 min | ~4 min |

**Recent Trend:**
- Last 5 plans: 08-03 (~5 min), 08-02 (~2 min), 08-01 (~4 min), 07-02 (~6 min), 07-01 (~8 min)
- Trend: Phase 8 complete -- full stack type-safe routing achieved

*Updated after each plan completion*

## Accumulated Context

### Roadmap Evolution

- Phase 06.1 inserted after Phase 6: add the current chat agent exploration refactor (URGENT)

### Decisions

Decisions are logged in PROJECT.md Key Decisions table.
Recent decisions affecting current work:

- [Roadmap]: 6 phases derived from 32 requirements at standard depth
- [Roadmap]: Cross-cutting requirements (CC-01, CC-02) mapped to Phase 1 since Arrow Raise patterns must be established before features
- [Roadmap]: DX-01 (Setup CLI) placed in Phase 5 with dashboard since CLI needs final project structure
- [Roadmap]: Phase 6 (AI Agents) depends only on Phase 2, enabling parallel execution with Phases 3-5
- [01-01]: Used hardcoded plugin coordinates in buildSrc (dynamic libs.plugins unreliable in buildSrc classpath)
- [01-01]: Removed logback from ktor-monitoring bundle; server logging moves to Log4j
- [01-02]: Used id("com.android.library") instead of alias because AGP is on buildSrc classpath
- [01-02]: Error response functions take code+message params for structured error codes
- [01-02]: DomainError.toAppError() added as interface method for shared error mapping
- [01-03]: KoinApplication composable wraps App for all client targets (single DI initialization point)
- [01-03]: Server uses install(Koin) Ktor plugin with configurationModule + serverModule
- [01-03]: Validation helpers use context(raise: Raise<FieldError>) pattern with raise.ensure() calls
- [01-04]: Used EcsLayout.json (Elastic Common Schema) for server JSON log format
- [01-04]: Kermit exposed as api() in shared module for transitive availability
- [01-04]: SLF4J bridged to Log4j2 via log4j-slf4j2-impl, logback removed
- [01-fix]: Forced kotlin-stdlib version resolution to match compiler 2.2.10 (Arrow 2.2.0 pulls in 2.2.21, breaking WASM)
- [02-01]: Used kotlin.uuid.Uuid with @OptIn(ExperimentalUuidApi) for Exposed 1.0 table columns
- [02-01]: Used top-level Exposed operators (eq, and, greater) instead of deprecated SqlExpressionBuilder
- [02-01]: Used getKoin().declare(database) for R2dbcDatabase runtime DI registration
- [02-01]: Called registerAuthMigrations() in main() before startDatabase() for migration ordering
- [02-01]: Arrow zipOrAccumulate with withError maps FieldError to IncorrectInput for accumulated validation
- [02-02]: Same InvalidCredentials error for both missing user and wrong password (prevents user enumeration)
- [02-02]: Uuid.parse() for JWT userId string to kotlin.uuid.Uuid conversion (consistent with Exposed layer)
- [02-02]: Refresh token rotation revokes old token before issuing new (fail-safe design)
- [02-03]: Used io.ktor.server.application for createRouteScopedPlugin (not io.ktor.server.routing)
- [02-03]: Uuid.parse() for string-to-UUID conversion in service layer
- [02-03]: Optional field validation with zipOrAccumulate: null fields pass through, non-null fields validated
- [03-02]: Used multiplatform-settings-no-arg Settings() factory for cross-platform Settings creation (no expect/actual needed)
- [03-02]: Cast Settings() to ObservableSettings for Flow-based preference observation (all no-arg platforms support it)
- [03-02]: multiplatform-settings 1.3.0 confirmed compatible with Kotlin 2.3.10 on all targets including WasmJs
- [03-01]: Used class simpleName matching for exception mapping in apiCall (uniform across KMP targets)
- [03-01]: CancellationException re-thrown in apiCall to preserve structured concurrency
- [03-01]: CIO engine used for both JVM and WasmJs targets (CIO has WasmJs support in Ktor 3.x)
- [03-03]: AuthInterceptor uses URLBuilder.buildString().contains() for refresh endpoint detection (encodedPath unavailable in Ktor 3.4.0)
- [03-03]: apiCall<T> checks T::class == Unit::class to skip body deserialization for logout endpoint
- [03-03]: SharedModule uses Koin includes() to compose storageModule and sdkModule transitively
- [03-03]: SdkModule uses Koin getProperty for BASE_URL with localhost default, overridable per platform
- [04-01]: All routes defined as @Serializable data objects in navigation/Routes.kt (type-safe, compile-time checked)
- [04-01]: popUpTo<LoginRoute> { inclusive = true } clears auth back stack on login success
- [04-01]: MaterialTheme removed from App.kt; TerminalTheme will wrap AppNavHost in plan 04-02
- [04-01]: BasicText with hardcoded colors for placeholder screens (to be replaced by TerminalTheme components)
- [04-02]: Custom TerminalShadow data class used instead of CMP DropShadow (API availability uncertain in 1.10.1)
- [04-02]: isSystemInDarkTheme() available from compose.foundation alone (no Material3 needed in designsystem)
- [04-02]: Removed compose.material3 from app:auth and app:dashboard (terminal design system replaces it)
- [04-02]: Font files use underscore naming for Compose Resources accessor generation
- [04-03]: [SUPERSEDED by quick-01] Accent card now uses dark header bg instead of left edge
- [quick-01]: TerminalCompactCard is a separate composable (not a variant flag) due to fundamentally different horizontal Row layout
- [quick-01]: Accent variant uses colors.textMuted for header bg (closest token to Pencil #525252 dark header)
- [quick-01]: borders.default (2dp) for Highlighted variant border width; borders.thin (1dp) for all others
- [04-03]: TerminalInput uses decorationBox parameter on BasicTextField for custom border/padding/placeholder/icons
- [04-03]: Spacer with Modifier.padding used for icon gaps inside input fields
- [quick-02]: Removed leadingIcon parameter from TerminalInput; Pencil design specifies only ">" prefix, no generic leading icon
- [quick-02]: Literal 6.dp for label gap (Pencil specifies 6dp, which falls between gap.xs=4 and gap.sm=8)
- [quick-02]: Canvas-based EyeIcon for password toggle (follows TerminalCheckbox/TerminalRadio Canvas icon pattern)
- [04-04]: Canvas-based checkmark drawing for TerminalCheckbox (cross-platform consistency over unicode)
- [04-04]: Canvas-based radio ring+dot for TerminalRadio (pixel-perfect circles)
- [04-04]: animateDpAsState for TerminalSwitch knob sliding animation (150ms tween)
- [04-04]: Popup composable for TerminalTooltip overlay (Foundation-level, no Material3)
- [04-04]: drawBehind for TerminalAlert accent left border (avoids nested layout)
- [04-05]: PlaceholderScreen updated to use TerminalText and theme tokens instead of hardcoded Color values
- [04-05]: ListItemState enum with 4 states for state-driven TerminalListItem styling
- [04-06]: Used androidx.compose.ui.tooling.preview.Preview (not org.jetbrains.compose) -- JetBrains library re-exports AndroidX annotation
- [04-06]: TerminalColors uses `bg` property (not `background`) for background color token
- [04-06]: Preview functions use private visibility + TerminalTheme wrapper + colors.bg background Column pattern
- [quick-03]: Primary button uses dark gray (#525252/#D4D4D4) not green accent -- matches Pencil design
- [quick-03]: Disabled is not a ButtonVariant; handled via enabled param with specific btnDisabled* color tokens
- [quick-03]: Literal dp values (16/12/8) for button padding since Pencil tokens dont map to existing spacing tokens
- [quick-03]: Foundation hover pattern: MutableInteractionSource + collectIsHoveredAsState + hoverable modifier
- [quick-04]: @TerminalPreview annotation uses AndroidUiModes for uiMode constants (consistent with commonMain usage)
- [quick-04]: TerminalPreview lives in theme package alongside TerminalTheme (design-system-level concern)
- [quick-04]: widthDp=1024 for Desktop preview to simulate wide-screen/landscape layout
- [quick-05]: String-only changes in preview function -- no structural or composable modifications
- [quick-06]: Dedicated per-variant color tokens (cardAccent*) instead of reusing generic theme colors for CardVariant.Accent
- [quick-07]: Literal dp values for badge padding (10dp/4dp) and icon gap (6dp) since Pencil tokens dont map to spacing tokens
- [quick-07]: Literal 10.sp fontSize for badge (Pencil specifies 10, typography.xs is 11.sp)
- [quick-07]: FontWeight.Medium for Default badge variant, SemiBold for Accent/Success/Warning/Error
- [quick-08]: RoundedCornerShape(2.dp) literal for track corners (Pencil specifies 2dp, no matching token)
- [quick-08]: Brush.linearGradient from accent to accent@50% alpha for indeterminate indicator
- [quick-08]: Column wrapper with conditional label row for backward-compatible label=null API
- [quick-09]: Literal 10.sp for header fontSize (Pencil specifies 10, typography.xs is 11.sp)
- [quick-09]: Literal dp values (16dp/10dp header, 16dp/12dp row) since Pencil tokens dont map to spacing tokens
- [quick-09]: RowScope extension for TerminalTableCell to enable weight(1f) usage
- [quick-10]: Literal 10.dp for switch track radius and label gap (Pencil 10dp between radius.md=6 and radius.lg=12)
- [quick-10]: btnPrimaryBg for switch on-track (consistent with checkbox checked fill; both #525252 in light)
- [quick-10]: 16dp inline table checkbox in 32dp touch target cell (proportionally smaller than standalone 18dp)
- [quick-10]: triStateToggleable for tri-state checkbox accessibility (ToggleableState.On/Off/Indeterminate)
- [quick-11]: Leading/trailing content lambdas changed from () -> Unit to (Color) -> Unit for state-aware icon coloring
- [quick-11]: showBottomBorder removed entirely from TerminalListItem (Pencil has no individual item borders)
- [quick-11]: drawBehind pattern from TerminalAlert reused for selected state left accent border
- [quick-11]: Literal dp values (16dp/12dp/2dp) since Pencil tokens dont map to existing spacing tokens
- [quick-12]: Popup onDismissRequest used for dismiss-on-click-outside (Foundation Popup supports it)
- [quick-12]: U+22EF midline horizontal ellipsis for three-dots trigger character
- [quick-12]: Preview renders menu content directly in Box (Popup not visible in static previews)
- [quick-15]: Animatable<Float> for swipe offset (allows imperative snapTo + spring animateTo)
- [quick-15]: Fixed revealWidth Dp parameter (default 80dp) instead of SubcomposeLayout measurement
- [quick-15]: Separate pointerInput blocks for tap and drag detection (chaining avoids gesture conflicts)
- [quick-15]: mutableIntStateOf/mutableFloatStateOf for ReorderState (Compose primitive state performance)
- [quick-15]: graphicsLayer for drag visual feedback (translationY, scaleX/Y 1.02, shadowElevation 8f)
- [quick-17]: Canvas-based Path.arcTo for bar top-corner radius (rounded top-left/top-right, flat bottom)
- [quick-17]: Non-highlight bars cycle through chartBar1/2/3 by index; highlight bars use dedicated chartBarHighlight token
- [quick-17]: Series1 area fill uses chartSeries1Muted token directly; series2+ uses seriesColor.copy(alpha=0.15f)
- [quick-18]: Modifier.offset with Dp * cos/sin for radial axis label positioning around hexagon
- [quick-18]: 0.35 radius factor for hexagon, 0.44 for label radius to position labels outside
- [quick-18]: Back-to-front series rendering (reversed iteration) so first series draws on top with data point dots
- [quick-19]: clipRect for line chart animation (reveals entire chart left-to-right, simpler than path progress)
- [quick-19]: barHeight multiplier for bar chart (bars grow from baseline, no position recalculation needed)
- [quick-19]: clampedValue multiplier for radar chart (polygons expand from center, dots scale naturally)
- [quick-20]: IndicationNodeFactory (modern API) for custom ripple instead of deprecated Indication interface
- [quick-20]: bounded=true for buttons/cards/lists; bounded=false for checkbox/switch/radio (small touch targets)
- [quick-20]: .clickable(interactionSource, indication) replaces .hoverable().clickable() (handles hover internally)
- [quick-20]: Theme text color at 0.12f alpha as default ripple color (subtle terminal aesthetic)
- [quick-21]: cursorBrush = SolidColor(colors.text) for themed cursor color (was platform-default black)
- [quick-21]: CompositionLocalProvider(LocalTextSelectionColors) for themed selection handles (was platform-default blue)
- [05-02]: withError field name remapping for validateName in zipOrAccumulate (firstName/lastName use same shared validator)
- [05-02]: Local validation on submit (not on each keystroke), clear individual field errors on change
- [05-02]: RegisterViewModel validates all 6 fields with zipOrAccumulate (accumulated errors, not fail-fast)
- [05-06]: sed_inplace function wrapper instead of variable expansion for reliable macOS/Linux quoting
- [05-06]: Three distinct package mappings: com.m2f.template->new, com.m2f.server->first_two.server, com.m2f.core->first_two.core
- [05-06]: Database default is 'application' (matching actual DataSource.kt), not 'template' as plan assumed
- [05-06]: cp -R + rm -rf instead of mv for cross-platform directory move reliability
- [05-06]: Delete .iml files rather than updating (IDE regenerates them)
- [05-01]: UserTier is a sealed class (not enum) with fromString() -- exhaustive when expressions enforce tier handling
- [05-01]: RegisterRequest uses firstName/lastName; server concatenates to name for storage
- [05-01]: UserResponse.tier extension property bridges wire format (String role) to sealed type
- [05-01]: Password reset tokens hashed with SHA-256, expire after 1 hour
- [05-01]: Server-side OAuth via Ktor oauth provider (Google + Apple) with empty default env vars
- [05-01]: forgotPassword always returns success to prevent user enumeration
- [05-01]: HttpClient(CIO) provided via Koin for server-side OAuth userinfo calls
- [05-04]: BoxWithConstraints with 840dp breakpoint for desktop/mobile responsive dashboard layout
- [05-04]: Terminal-themed symbols (>, ~, #, $, %) for sidebar/bottom nav icons (no icon library, Foundation-only)
- [05-04]: PlaceholderScreen uses TerminalCard + TerminalBadge for consistent terminal aesthetic
- [05-03]: showAppleSignIn() expect/actual for platform-conditional Apple button (iOS + WASM true, Android + JVM false)
- [05-03]: Shared LoginFormContent composable used by both desktop and mobile layouts (avoid duplication)
- [05-03]: Canvas-based status dot in brand panel footer (consistent with Canvas icon patterns in design system)
- [05-03]: Unicode box-drawing chars for ASCII art in brand panel (cross-platform consistency)
- [Phase 05]: successBg color token used instead of successMuted (plan token name mismatch corrected to match TerminalColors)
- [05-07]: Server OAuth callback uses respondRedirect with JWT in query params instead of JSON response
- [05-07]: Redirect URI validated against server-side allowlist (WASM URL, mobile scheme, desktop localhost) to prevent open-redirect
- [05-07]: JVM Desktop uses temporary localhost:9876 ServerSocket + MutableStateFlow for OAuth callback reception
- [05-07]: either{} wrapping Raise-based OAuthService calls for redirect error handling in callback routes
- [05-07]: expect/actual class OAuthHandler with serverBaseUrl constructor param for platform-specific OAuth browser opening
- [05-08]: State-based content switching: selectedNavItem drives when() block in content area, sidebar/bottom nav persistent
- [05-08]: [SUPERSEDED by 05-10] Profile content was injected as composable lambda slot; now navigates to standalone ProfileRoute
- [05-08]: [SUPERSEDED by 05-10] Desktop/mobile profile embedding removed; ProfileRoute renders standalone
- [05-08]: ProfileRoute kept as standalone route in AppNavHost (now the only profile route, not a fallback)
- [05-09]: Single find command replaces 9 hardcoded SOURCE_SETS loops (dynamic module discovery)
- [05-09]: Process substitution < <(find ...) avoids subshell variable loss for bash array population
- [05-10]: Profile navigates via navController.navigate(ProfileRoute) instead of embedded composable slot
- [05-10]: DashboardScreen exposes single onProfileClick callback, removing onShowProfile/onHideProfile/profileContent
- [05-11]: rememberMe=true default on saveTokens() preserves backward compatibility for register/refresh/OAuth flows
- [05-11]: Session-only flag stored in Settings (not in-memory) so clearSessionTokens() works across app restarts
- [05-11]: LaunchedEffect(Unit) for startup token check placed before OAuth callback LaunchedEffect for correct ordering
- [05-11]: LoginRoute remains startDestination; LaunchedEffect redirects to DashboardRoute to avoid recomposition issues
- [06-01]: AI_ENABLED env var defaults to false for safe server startup without AI configuration
- [06-01]: 4 AI error types: AgentFailed, AgentNotFound, ConversationNotFound, ProviderUnavailable
- [06-01]: AiErrors follow AuthErrors.kt pattern: data class implementing DomainError with context(RoutingContext) respond()
- [06-02]: PersistenceStorageProvider<AgentCheckpointPredicateFilter> is the actual Koog 0.6.2 interface (3 methods, not 5 from research docs)
- [06-02]: AgentCheckpointData serialized via PersistenceUtils.defaultCheckpointJson (Koog-provided Json instance)
- [06-02]: ToolSet at ai.koog.agents.core.tools.reflect.ToolSet, annotations at ai.koog.agents.core.tools.annotations
- [06-03]: Standalone agent executors (SingleLLMPromptExecutor + OpenAILLMClient) instead of Koog Ktor plugin for simplicity and testability
- [06-03]: AIAgentService.Companion.invoke() factory pattern for agent service creation (manages lifecycle, createAgentAndRun)
- [06-03]: ensureAiEnabled() helper for Raise context parameter compatibility (extension functions on Raise don't resolve in context parameter lambdas)
- [06-03]: GPT4o as default model for both agents (tool-calling support, good speed/quality balance)
- [06.1-01]: Per-request AIAgent instead of singleton AIAgentService for streaming callback injection
- [06.1-01]: Removed SayToUser/AskUser/ExitTool tools -- plain text IS the response with streaming
- [06.1-01]: AIAgentGraphStrategy<String, Any> output type to match reference implementation pattern
- [06.1-02]: SSE route outside authenticate{} block with manual JWT query param validation for browser EventSource compatibility
- [06.1-02]: ktor-server-sse added to server module (not transitively available from server:ai implementation dependency)
- [06.1-02]: Typed SSE events: conversation (metadata), message (content chunks), done (completion), error (failures)
- [quick-23]: this@sse for ServerSSESession access inside getAuth crossinline lambda (context receiver name not directly accessible)
- [quick-24]: JWT read from Authorization header first, query param 'token' as fallback for browser WebSocket clients
- [quick-24]: ChatStreamFrame with completed boolean flag for stream termination signaling
- [quick-24]: WebSocket errors sent as JSON ErrorResponse frames before close with appropriate close codes
- [07-01]: Custom KSerializer (UserRoleSerializer) for flat-string wire format instead of @SerialName polymorphic approach
- [07-01]: UserRole.fromString() defaults to User for unrecognized roles (safe fallback)
- [07-01]: Exposed select().count() for UserRepository.count() (no selectAll in R2DBC API)
- [07-02]: Hardcoded roleId-to-UserRole when() mapping instead of join with RolesTable (simpler for template)
- [07-02]: Old role varchar left in DB to avoid risky ALTER TABLE DROP COLUMN over R2DBC
- [07-02]: Suppressed createMissingTablesAndColumns deprecation (template project, not production)
- [08-01]: Regular classes (not data classes) for @Resource without properties -- no-arg constructor requirement
- [08-01]: WebSocket path as companion const in Ai.Chat (KTOR-4369: type-safe routing unsupported for WebSockets)
- [08-03]: href(ResourcesFormat(), Auth.Refresh()) for type-safe refresh path detection instead of hardcoded string
- [08-03]: URLBuilder.buildString().contains() for path matching (encodedPath unavailable on URLBuilder in Ktor 3.4.0)
- [08-02]: conduit/conduitAuth helpers unchanged inside type-safe resource handlers (same RoutingContext receiver)
- [08-02]: withRole(UserRole.Admin) wraps get<Users.ById> correctly (transparent route selector)
- [08-02]: WebSocket uses full-path Ai.Chat.WS_PATH constant since route() wrapper removed

### Pending Todos

None yet.

### Quick Tasks Completed

| # | Description | Date | Commit | Directory |
|---|-------------|------|--------|-----------|
| 1 | Align TerminalCard with Pencil design — structured header/content/footer, 5 variants | 2026-02-12 | 4516ed7 | [1-align-design-system-components-with-penc](./quick/1-align-design-system-components-with-penc/) |
| 2 | Align TerminalInput with Pencil design — prefix, conditional border, password variant | 2026-02-12 | 8a93f6d | [2-align-terminalinput-with-pencil-design-a](./quick/2-align-terminalinput-with-pencil-design-a/) |
| 3 | Align TerminalButton with Pencil design — 5 variants, hover, disabled tokens, padding/typography | 2026-02-12 | 5fff28c | [3-align-terminalbutton-with-pencil-design-](./quick/3-align-terminalbutton-with-pencil-design-/) |
| 4 | Create @TerminalPreview multi-mode annotation — Light, Dark, Desktop previews across 17 files | 2026-02-12 | 045f538 | [4-create-custom-multi-mode-preview-annotat](./quick/4-create-custom-multi-mode-preview-annotat/) |
| 5 | Align TerminalCard previews with Pencil design — process_info, active_session, system_info, featured_process, process.log | 2026-02-12 | 09aa429 | [5-align-terminalcard-previews-network-stat](./quick/5-align-terminalcard-previews-network-stat/) |
| 6 | Add 4 card-accent color tokens and wire into CardVariant.Accent | 2026-02-12 | 0601e7a | [6-add-card-accent-bg-color-token-for-cardv](./quick/6-add-card-accent-bg-color-token-for-cardv/) |
| 7 | Align TerminalBadge with Pencil design -- sm radius, btnPrimary Accent, icon param, font weights | 2026-02-12 | e8ae2bf | [7-align-terminalbadge-with-pencil-design](./quick/7-align-terminalbadge-with-pencil-design/) |
| 8 | Align TerminalProgress with Pencil design -- label/percentage, 8dp track, accentMuted, gradient indicator | 2026-02-12 | fb28c8a | [8-align-terminalprogress-with-pencil-desig](./quick/8-align-terminalprogress-with-pencil-desig/) |
| 9 | Align TerminalTable with Pencil design -- 4 table tokens, composable row content, TerminalTableCell | 2026-02-12 | 8f5ba33 | [9-align-table-components-with-pencil-desig](./quick/9-align-table-components-with-pencil-desig/) |
| 10 | Align toggles with Pencil design -- tri-state checkbox, switch alignment, selectable table | 2026-02-12 | 811eb08 | [10-align-toggles-with-pencil-design-and-add](./quick/10-align-toggles-with-pencil-design-and-add/) |
| 11 | Align TerminalListItem with Pencil design -- per-state colors, drawBehind selected border, color lambdas | 2026-02-12 | 6ce7515 | [11-align-terminallistitem-with-pencil-desig](./quick/11-align-terminallistitem-with-pencil-desig/) |
| 12 | Add contextual dropdown menu to TerminalListItem -- TerminalDropdownMenu, ellipsis trigger, menuItems param | 2026-02-12 | afdff59 | [12-add-contextual-dropdown-menu-to-terminal](./quick/12-add-contextual-dropdown-menu-to-terminal/) |
| 13 | Fix white corner artifacts on TerminalDropdownMenu -- remove .shadow() modifier | 2026-02-12 | 50dde46 | [13-fix-white-corner-artifacts-on-terminaldr](./quick/13-fix-white-corner-artifacts-on-terminaldr/) |
| 14 | Fix ui-tooling dependency -- move to Android debugImplementation only | 2026-02-12 | 0f92825 | [14-fix-ui-tooling-dependency-restrict-to-de](./quick/14-fix-ui-tooling-dependency-restrict-to-de/) |
| 15 | Add swipe-to-reveal actions and drag-to-reorder gestures | 2026-02-12 | 32de96a | [15-add-swipe-to-reveal-actions-and-drag-to-](./quick/15-add-swipe-to-reveal-actions-and-drag-to-/) |
| 16 | Fix TerminalSwipeReveal -- IntrinsicSize.Min height, opaque foreground bg, single awaitEachGesture | 2026-02-12 | a0f1688 | [16-fix-terminalswipereveal-actions-visible-](./quick/16-fix-terminalswipereveal-actions-visible-/) |
| 17 | Implement TerminalLineChart and TerminalBarChart from Pencil design -- 11 chart tokens, area gradient, tier bars | 2026-02-13 | 08075a2 | [17-implement-chart-components-from-pencil-d](./quick/17-implement-chart-components-from-pencil-d/) |
| 18 | Implement TerminalRadarChart composable -- hexagonal grid, multi-series polygons, axis labels | 2026-02-13 | eedb8b5 | [18-implement-terminalradarchart-composable-](./quick/18-implement-terminalradarchart-composable-/) |
| 19 | Add entry animations to chart components -- 800ms EaseOutCubic on line, bar, radar charts | 2026-02-13 | 2c36aec | [19-add-entry-animations-to-chart-components](./quick/19-add-entry-animations-to-chart-components/) |
| 20 | Add ripple click effect to all clickable components -- IndicationNodeFactory, 300ms expand, 200ms fade | 2026-02-13 | 16c8989 | [20-add-ripple-click-effect-to-all-clickable](./quick/20-add-ripple-click-effect-to-all-clickable/) |
| 21 | Fix TerminalInput cursor and selection handle colors to use theme tokens | 2026-02-13 | e988534 | [21-fix-terminalinput-cursor-and-selection-h](./quick/21-fix-terminalinput-cursor-and-selection-h/) |
| 22 | Fix checkbox/switch/radio ripple effects drawing beyond component bounds -- bounded=true | 2026-02-13 | dcd5e4f | [22-fix-checkbox-ripple-effect-drawing-beyon](./quick/22-fix-checkbox-ripple-effect-drawing-beyon/) |
| 23 | Implement getAuth SSE authentication helper and refactor chat/stream route | 2026-02-14 | 3901b99 | [23-implement-sse-getauth-function-in-error-](./quick/23-implement-sse-getauth-function-in-error-/) |
| 24 | Switch chat streaming from SSE to WebSocket with header-based JWT auth | 2026-02-15 | a5e6896 | [24-switch-chat-streaming-from-sse-to-websoc](./quick/24-switch-chat-streaming-from-sse-to-websoc/) |

### Blockers/Concerns

- [Research]: Phase 3 (Client SDK) flagged for research -- token refresh mutex patterns in KMP, WASM DataStore limitations
- [Research]: Phase 6 (AI Agents) flagged for research -- Koog 0.6.1 is new, MCP integration and tool sandboxing patterns emerging

## Session Continuity

Last session: 2026-02-15
Stopped at: Completed 08-03-PLAN.md (SDK Client Migration) -- Phase 8 complete
Resume file: All 38 plans complete across 8 phases
