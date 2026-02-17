# Phase 4: Navigation & UI Components - Context

**Gathered:** 2026-02-12
**Status:** Ready for planning

<domain>
## Phase Boundary

Type-safe multiplatform navigation between screens, a complete reusable component library (all 41 components from the Pencil design system), and a custom theme system with Light/Dark mode -- all working identically on every KMP target (Android, iOS, Desktop, WASM).

</domain>

<decisions>
## Implementation Decisions

### Theme Token Mapping
- Mirror Pencil design token names 1:1 in Compose: `TerminalTheme.colors.bg`, `.surface`, `.accent`, `.text`, `.textMuted`, `.textDim`, `.border`, `.inset`, `.accentMuted`
- Semantic colors: `.success`, `.successBg`, `.warning`, `.warningBg`, `.error`, `.errorBg`, `.info`, `.infoBg`
- All theme properties exposed as CompositionLocals (colors, spacing, typography, shadows, opacity, corner radius)
- Spacing as CompositionLocal: `LocalTerminalSpacing.current` with `.xs` (4px), `.sm` (8px), `.md` (12px), `.lg` (16px), `.xl` (20px)
- Gap as CompositionLocal: `LocalTerminalGap.current` with `.xs` (4px), `.sm` (8px), `.md` (12px), `.lg` (16px), `.xl` (24px)
- Shadows as CompositionLocal: `LocalTerminalShadows.current` with `.none`, `.sm`, `.md`, `.lg`
- Opacity as CompositionLocal: `LocalTerminalOpacity.current` with `.full` (1.0), `.high` (0.75), `.medium` (0.50), `.low` (0.25)
- Corner radius as CompositionLocal: `LocalTerminalRadius.current` with `.none` (0), `.sm` (4), `.md` (6), `.lg` (12), `.pill`, `.full`

### Typography
- Single font: JetBrains Mono for all UI elements
- Exact Pencil sizes, no mobile adaptation: text-xs (11px), text-sm (12px), text-base (13px), text-md (14px), text-2xl (32px)
- Three weights: normal (400), semibold (600), bold (700)

### Component Scope
- Implement ALL 41 components from the Pencil design system (`terminal_design_system.pen`)
- Components: Buttons (Default, Secondary, Ghost, Destructive, Icon), Input Group (Default, Filled), Textarea, Cards (Default, Accent, Info, Highlighted, Compact), Alerts (Info, Success, Warning, Error), Badges (Default, Accent, Success, Warning, Error), Checkbox (Default, Checked), Switch (Default, Checked), Radio (Default, Checked), Table + Table Row, Progress (Default, Indeterminate), Tooltip, Kbd, Avatar, Divider, List + List Items (Default, Hover, Selected, Disabled)

### Component API Design
- Consolidated components with enum/parameter variants, NOT separate composables per variant
- Example: `TerminalButton(variant = ButtonVariant.Secondary)` not `TerminalButtonSecondary()`
- Single component per logical type (TerminalButton, TerminalCard, TerminalAlert, TerminalBadge, TerminalCheckbox, TerminalSwitch, TerminalRadio, TerminalListItem, etc.)

### Theme Coupling
- Every component reads ALL styling from theme CompositionLocals -- no hardcoded values
- Dark mode works automatically through theme switching

### Pencil MCP as Source of Truth
- The executing agent uses Pencil MCP tools (`batch_get`, `get_variables`, `get_screenshot`) ad hoc to read exact component specs, token values, and visual references during implementation
- No build-time dependency on .pen files -- values are extracted and written into Kotlin source at implementation time
- The .pen file (`terminal_design_system.pen`) serves as the design reference, not a runtime artifact

### Border Tokens
- Three thicknesses: thin (1px), default (2px), thick (3px)
- Border color from `--terminal-border` token

### Claude's Discretion
- Navigation route hierarchy and auth/main graph separation
- Deep linking configuration
- Back stack behavior
- Dark mode toggle mechanism (system-follow vs manual)
- Component internal implementation patterns (state management, animation)
- File organization for 41 components (single file vs package per component group)

</decisions>

<specifics>
## Specific Ideas

- Design system file: `terminal_design_system.pen` (located at `/Users/marc/Downloads/terminal_design_system.pen`, also staged in repo as `terminal_design_system.pen`)
- The design system has both Light and Dark theme frames with matching token documentation
- JetBrains Mono monospace font gives the entire UI a "terminal" aesthetic
- Muted, desaturated color palette -- not vibrant Material colors
- The design system includes detailed visual specifications for every component state (default, hover, selected, disabled, checked)

</specifics>

<deferred>
## Deferred Ideas

None -- discussion stayed within phase scope

</deferred>

---

*Phase: 04-navigation-ui-components*
*Context gathered: 2026-02-12*
