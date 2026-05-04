---
source_file: "app/privacy/impl/src/commonTest/kotlin/com/m2f/template/app/privacy/AccountDeletionViewModelTest.kt"
type: "code"
community: "AccountDeletionViewModel"
location: "L15"
tags:
  - graphify/code
  - graphify/EXTRACTED
  - community/AccountDeletionViewModel
---

# AccountDeletionViewModelTest

## Connections
- [[.`cancelDeletion calls SDK and emits DeletionCancelled`()]] - `method` [EXTRACTED]
- [[.`confirmDeletion calls SDK logs out and navigates to login`()]] - `method` [EXTRACTED]
- [[.`full deletion flow with proceedToReAuth`()]] - `method` [EXTRACTED]
- [[.`load checks for pending deletion and shows SCHEDULED step`()]] - `method` [EXTRACTED]
- [[.`load fetches user email from profile`()]] - `method` [EXTRACTED]
- [[.`load with no pending deletion stays at WARNING`()]] - `method` [EXTRACTED]
- [[.`log out triggers logout and sends LoggedOut event`()]] - `method` [EXTRACTED]
- [[.`proceedToReAuth advances to RE_AUTH step without setting password`()]] - `method` [EXTRACTED]
- [[.`reAuthenticate sets password and advances to REASON step`()]] - `method` [EXTRACTED]
- [[.`setReason updates reason and advances to CONFIRM step`()]] - `method` [EXTRACTED]
- [[.`skip reason moves to confirm step with empty reason`()]] - `method` [EXTRACTED]
- [[AccountDeletionViewModelTest.kt]] - `contains` [EXTRACTED]

#graphify/code #graphify/EXTRACTED #community/AccountDeletionViewModel