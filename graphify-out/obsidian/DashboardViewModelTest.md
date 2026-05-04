---
source_file: "app/dashboard/impl/src/commonTest/kotlin/com/m2f/template/app/dashboard/DashboardViewModelTest.kt"
type: "code"
community: "DashboardViewModel"
location: "L13"
tags:
  - graphify/code
  - graphify/EXTRACTED
  - community/DashboardViewModel
---

# DashboardViewModelTest

## Connections
- [[.`AdminPanelClicked emits NavigateToAdmin when groupId is set`()]] - `method` [EXTRACTED]
- [[.`AdminPanelClicked emits NavigateToAdmin with null groupId for system admin without groups`()]] - `method` [EXTRACTED]
- [[.`LoadDashboard toggles loading state`()]] - `method` [EXTRACTED]
- [[.`LoadDashboard with admin membership sets isAdmin true`()]] - `method` [EXTRACTED]
- [[.`LoadDashboard with member-only membership does not set isAdmin`()]] - `method` [EXTRACTED]
- [[.`LoadDashboard with system admin AND group admin membership gets groupId`()]] - `method` [EXTRACTED]
- [[.`LoadDashboard with system admin role sets isAdmin true even without group memberships`()]] - `method` [EXTRACTED]
- [[.`LogoutClicked emits NavigateToLogin event`()]] - `method` [EXTRACTED]
- [[.`NavItemSelected updates selectedNavItem`()]] - `method` [EXTRACTED]
- [[DashboardViewModelTest.kt]] - `contains` [EXTRACTED]

#graphify/code #graphify/EXTRACTED #community/DashboardViewModel