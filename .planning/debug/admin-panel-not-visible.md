---
status: diagnosed
trigger: "Admin Panel is not accessible for admin or poweradmin users"
created: 2026-02-21T00:00:00Z
updated: 2026-02-21T00:00:00Z
---

## Current Focus

hypothesis: The admin panel visibility is gated on GROUP-level role (GroupRole.Admin/Owner), not SYSTEM-level role (UserRole.Admin/PowerAdmin). A user with system Admin role but no group memberships will never see the admin panel.
test: Traced DashboardViewModel.LoadDashboard → sdk.getMyMemberships() → checks GroupRole.level >= GroupRole.Admin.level
expecting: Confirmed — the role check is on GroupRole, not UserRole
next_action: Return diagnosis

## Symptoms

expected: Users with system-level Admin or PowerAdmin role should see an "Admin Panel" nav item in the Dashboard sidebar/bottom nav
actual: No admin panel button appears regardless of system role
errors: None — silent failure, the button simply doesn't render
reproduction: Log in as admin or poweradmin user → navigate to Dashboard → no admin nav item visible
started: Since Phase 14 implementation

## Eliminated

(none — first hypothesis confirmed)

## Evidence

- timestamp: 2026-02-21T00:01:00Z
  checked: DashboardModel.kt default state
  found: isAdmin defaults to false, groupId defaults to null
  implication: Admin panel button only shows when isAdmin == true

- timestamp: 2026-02-21T00:02:00Z
  checked: DashboardSidebar.kt line 121, DashboardBottomNav.kt line 58
  found: Both use `if (isAdmin)` to conditionally add the admin nav item
  implication: The button display is correctly gated on isAdmin state

- timestamp: 2026-02-21T00:03:00Z
  checked: DashboardViewModel.kt lines 28-42 (LoadDashboard handler)
  found: Calls sdk.getMyMemberships() and checks `membership.groupRole.level >= GroupRole.Admin.level`
  implication: isAdmin is set based on GROUP membership role, not system UserRole

- timestamp: 2026-02-21T00:04:00Z
  checked: GroupRole.kt — Admin.level = 1, Owner.level = 2, Member.level = 0
  found: Level comparison is correct for group roles
  implication: If user has a group membership with Admin or Owner group role, isAdmin becomes true

- timestamp: 2026-02-21T00:05:00Z
  checked: UserRole.kt vs GroupRole.kt
  found: Two completely separate role systems — UserRole (system: User/Admin/PowerAdmin) vs GroupRole (group: Member/Admin/Owner)
  implication: A user can be system Admin but have ZERO group memberships → getMyMemberships returns empty list → isAdmin stays false

- timestamp: 2026-02-21T00:06:00Z
  checked: getMyMemberships server endpoint (GroupRoutes.kt line 33-37, GroupService.kt line 168-178)
  found: Queries membershipRepository.findByUserId — returns only actual group memberships
  implication: System-level admin/poweradmin users who haven't been added to any group get an empty list

- timestamp: 2026-02-21T00:07:00Z
  checked: DashboardViewModel.kt line 29 — ifLeft handler
  found: `ifLeft = { /* Silently ignore — user may not be in any group */ }`
  implication: API errors are silently swallowed, so even network failures won't surface

- timestamp: 2026-02-21T00:08:00Z
  checked: AppNavHost.kt DashboardRoute composable (lines 181-205)
  found: Navigation is correctly wired — DashboardEvent.NavigateToAdmin triggers navController.navigate(AdminPanelRoute)
  implication: Navigation plumbing is correct; the problem is upstream (isAdmin never becomes true)

## Resolution

root_cause: |
  The DashboardViewModel determines admin visibility exclusively from GROUP-level memberships 
  (GroupRole), not from SYSTEM-level roles (UserRole). When LoadDashboard fires, it calls 
  sdk.getMyMemberships() which returns the user's group memberships. It then checks if any 
  membership has groupRole.level >= GroupRole.Admin.level (i.e., group Admin or Owner).
  
  The problem: A user with system-level UserRole.Admin or UserRole.PowerAdmin who has NOT been 
  added to any group will get an empty membership list from getMyMemberships(). Since there are 
  no memberships to check, isAdmin stays at its default value of false, and the admin nav item 
  never renders.
  
  This is a DESIGN GAP — the system has two separate role hierarchies:
  1. UserRole (system-level): User < Admin < PowerAdmin — controls server endpoint access
  2. GroupRole (group-level): Member < Admin < Owner — controls group-level permissions
  
  The dashboard only checks GroupRole but the user expects system-level Admin/PowerAdmin to 
  also grant admin panel access. The admin panel is group-scoped (AdminPanelRoute takes a groupId),
  so even if we showed the button, there would be no groupId to navigate to for users without 
  group memberships.
  
  There are two sub-problems:
  1. System admins without group memberships see no admin panel at all
  2. Even if isAdmin were set, AdminPanelClicked requires a non-null groupId (line 52-55)

fix: (not applied — diagnosis only)
verification: (not applicable)
files_changed: []
