# Project State

## Project Reference

See: .planning/PROJECT.md (updated 2026-02-10)

**Core value:** A developer can clone this template, run the setup CLI, and immediately have a working full-stack app with auth, database, DI, AI agents, and a component library -- no infrastructure decisions required.
**Current focus:** Phase 4 Complete (verified) - Ready for Phase 5

## Current Position

Phase: 4 of 6 (Navigation & UI Components) -- VERIFIED COMPLETE
Plan: 7 of 7 in current phase (04-01 through 04-07 complete)
Status: Phase 04 Verified Complete
Last activity: 2026-02-13 -- Completed quick task 17: Implement TerminalLineChart and TerminalBarChart from Pencil design

Progress: [█████████████░__] 77% (17/22 plans)

## Performance Metrics

**Velocity:**
- Total plans completed: 17
- Average duration: ~9 min
- Total execution time: ~155 min

**By Phase:**

| Phase | Plans | Total | Avg/Plan |
|-------|-------|-------|----------|
| 1 | 4/4 | ~46 min | ~12 min |
| 2 | 3/3 | ~58 min | ~19 min |
| 3 | 3/3 | ~15 min | ~5 min |
| 4 | 7/7 | ~36 min | ~5 min |

**Recent Trend:**
- Last 5 plans: 04-03 (~2 min), 04-04 (~3 min), 04-05 (~3 min), 04-06 (~9 min), 04-07 (~7 min)
- Trend: Fast execution when building on established patterns

*Updated after each plan completion*

## Accumulated Context

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

### Blockers/Concerns

- [Research]: Phase 3 (Client SDK) flagged for research -- token refresh mutex patterns in KMP, WASM DataStore limitations
- [Research]: Phase 6 (AI Agents) flagged for research -- Koog 0.6.1 is new, MCP integration and tool sandboxing patterns emerging

## Session Continuity

Last session: 2026-02-13
Stopped at: Completed quick-17 (Implement TerminalLineChart and TerminalBarChart) -- 11 chart tokens, ChartDataPoint/ChartSeries/BarData models, Canvas rendering
Resume file: None
