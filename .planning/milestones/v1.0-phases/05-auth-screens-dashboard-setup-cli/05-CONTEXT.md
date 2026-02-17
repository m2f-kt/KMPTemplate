# Phase 5: Auth Screens, Dashboard & Setup CLI - Context

**Gathered:** 2026-02-13
**Status:** Ready for planning

<domain>
## Phase Boundary

Deliver end-to-end user-facing screens and template onboarding: login/signup screens with form validation and OAuth, a sample dashboard with sidebar navigation, profile screens for all user tiers, and a setup CLI script. All screen layouts follow the Pencil design system (`terminal_design_system.pen`).

</domain>

<decisions>
## Implementation Decisions

### Auth screen layout
- Follow Pencil designs exactly — 4 screens in the Auth group:
  - **Login Desktop** (split: brand panel left, form right) — email, password, remember me, reset password, social login
  - **Login Mobile** (centered card) — same fields, compact layout
  - **Register Desktop** (split: brand tagline left, form right) — first/last name, email, password, confirm password, terms checkbox, social login
  - **Register Mobile** (centered card) — same fields, compact
- Responsive: desktop uses split layout, mobile uses centered card

### Social login (OAuth)
- **Google OAuth**: functional on all targets (Android, iOS, Desktop, WASM)
- **Apple Sign-In**: functional on web (WASM) and iOS only — not shown on Android/Desktop
- Replaces GitHub from Pencil design — update button to show Apple instead of GitHub where applicable
- Requires new server-side OAuth endpoints (Phase 2 only built email/password)

### Remember me & reset password
- **Both functional**
- Remember me: persists session (longer token expiry or persistent refresh token)
- Reset password: sends email flow (requires new server endpoint for password reset)

### Auth error handling
- **Combined approach**: field-level inline errors for validation (e.g., "Invalid email format"), alert banner for server errors (e.g., "Invalid credentials", "Network error")
- Accumulated validation errors — multiple field errors shown at once (Arrow zipOrAccumulate)

### Post-login transition
- Direct navigation to dashboard — no intermediate loading state or animation

### Dashboard data
- **Mock/static data** for all dashboard metrics (uptime 99.98%, requests 1.2M, response time 42ms, error rate 0.03%)
- Demonstrates UI components working with realistic-looking data
- Active processes table, recent activity list, deployment status bars — all mock

### User tiers
- **All 5 tiers implemented**: Free, Paid, Premium, Admin, PowerAdmin
- User type modeled as **sealed type** in backend (not a String enum)
- Each tier shows appropriate profile content per Pencil designs (usage limits, upgrade prompts, admin features)
- Requires backend changes to add tier/role as sealed type

### Dashboard navigation
- **Dashboard + Profile are real functional screens**
- Other sidebar nav items (processes, logs, deployments, settings) show placeholder screens
- Mobile uses bottom tab navigation per Pencil design

### Profile functionality
- **Edit profile**: functional — opens editable fields, uses PUT /me endpoint from Phase 2
- **Logout**: functional — clears tokens, navigates back to login screen

### Claude's Discretion
- Form validation timing (on blur vs on submit vs real-time)
- Loading states/skeletons during data fetch
- Password strength requirements display
- Mobile bottom nav exact behavior and transitions
- Setup CLI implementation details (interactive prompts, what gets renamed)
- Placeholder screen content for non-functional nav items

</decisions>

<specifics>
## Specific Ideas

- All screen layouts must match the Pencil designs in `terminal_design_system.pen` (Auth group for auth screens, Dashboard/Profile groups for post-auth screens)
- Pencil shows GitHub + Google social buttons — replace GitHub with Apple (Apple only on web + iOS targets)
- Profile screens have 5 tier variants in Pencil (Free Tier, Paid User, Premium User, Admin User, PowerAdmin) — all with desktop and mobile layouts
- Dashboard has terminal CLI aesthetic throughout ("$ system_overview", "$ authenticate", etc.)

</specifics>

<deferred>
## Deferred Ideas

- Real server metrics (actual uptime, request counts) — could replace mock data in a future enhancement
- Functional processes/logs/deployments/settings screens — each could be its own phase
- GitHub OAuth — user chose Google + Apple instead, but GitHub could be added later

</deferred>

---

*Phase: 05-auth-screens-dashboard-setup-cli*
*Context gathered: 2026-02-13*
