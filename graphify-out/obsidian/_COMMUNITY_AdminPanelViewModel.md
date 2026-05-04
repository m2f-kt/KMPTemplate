---
type: community
cohesion: 0.22
members: 9
---

# AdminPanelViewModel

**Cohesion:** 0.22 - loosely connected
**Members:** 9 nodes

## Members
- [[.`CancelRemoveMember hides dialog`()]] - code - app/admin/impl/src/commonTest/kotlin/com/m2f/template/app/admin/AdminPanelViewModelTest.kt
- [[.`ExecuteRemoveMember failure closes dialog and sets error`()]] - code - app/admin/impl/src/commonTest/kotlin/com/m2f/template/app/admin/AdminPanelViewModelTest.kt
- [[.`ExecuteRemoveMember success removes member from list`()]] - code - app/admin/impl/src/commonTest/kotlin/com/m2f/template/app/admin/AdminPanelViewModelTest.kt
- [[.`LoadAdminPanel loads group info and members`()]] - code - app/admin/impl/src/commonTest/kotlin/com/m2f/template/app/admin/AdminPanelViewModelTest.kt
- [[.`LoadAdminPanel with getGroup error shows error`()]] - code - app/admin/impl/src/commonTest/kotlin/com/m2f/template/app/admin/AdminPanelViewModelTest.kt
- [[.`LoadMoreMembers appends to member list`()]] - code - app/admin/impl/src/commonTest/kotlin/com/m2f/template/app/admin/AdminPanelViewModelTest.kt
- [[.`RegisterMemberClicked emits NavigateToRegisterMember`()]] - code - app/admin/impl/src/commonTest/kotlin/com/m2f/template/app/admin/AdminPanelViewModelTest.kt
- [[AdminPanelViewModelTest]] - code - app/admin/impl/src/commonTest/kotlin/com/m2f/template/app/admin/AdminPanelViewModelTest.kt
- [[AdminPanelViewModelTest.kt]] - code - app/admin/impl/src/commonTest/kotlin/com/m2f/template/app/admin/AdminPanelViewModelTest.kt

## Live Query (requires Dataview plugin)

```dataview
TABLE source_file, type FROM #community/AdminPanelViewModel
SORT file.name ASC
```
