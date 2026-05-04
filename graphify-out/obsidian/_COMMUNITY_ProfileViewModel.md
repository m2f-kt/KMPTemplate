---
type: community
cohesion: 0.33
members: 6
---

# ProfileViewModel

**Cohesion:** 0.33 - loosely connected
**Members:** 6 nodes

## Members
- [[.`load profile failure shows server error`()]] - code - app/profile/impl/src/commonTest/kotlin/com/m2f/template/app/profile/ProfileViewModelTest.kt
- [[.`load profile populates model with user data`()]] - code - app/profile/impl/src/commonTest/kotlin/com/m2f/template/app/profile/ProfileViewModelTest.kt
- [[.`logout emits NavigateToLogin event`()]] - code - app/profile/impl/src/commonTest/kotlin/com/m2f/template/app/profile/ProfileViewModelTest.kt
- [[.`save profile shows success in model`()]] - code - app/profile/impl/src/commonTest/kotlin/com/m2f/template/app/profile/ProfileViewModelTest.kt
- [[ProfileViewModelTest]] - code - app/profile/impl/src/commonTest/kotlin/com/m2f/template/app/profile/ProfileViewModelTest.kt
- [[ProfileViewModelTest.kt]] - code - app/profile/impl/src/commonTest/kotlin/com/m2f/template/app/profile/ProfileViewModelTest.kt

## Live Query (requires Dataview plugin)

```dataview
TABLE source_file, type FROM #community/ProfileViewModel
SORT file.name ASC
```
