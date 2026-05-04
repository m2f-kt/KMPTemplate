---
source_file: "server/privacy/impl/src/test/kotlin/com/m2f/server/privacy/service/ConsentServiceTest.kt"
type: "code"
community: "ConsentService"
location: "L24"
tags:
  - graphify/code
  - graphify/EXTRACTED
  - community/ConsentService
---

# ConsentServiceTest

## Connections
- [[.`getActiveConsents deduplicates by type returning latest`()]] - `method` [EXTRACTED]
- [[.`getActiveConsents returns all consent types with defaults for missing records`()]] - `method` [EXTRACTED]
- [[.`getActiveConsents returns granted=false after consent is withdrawn`()]] - `method` [EXTRACTED]
- [[.`getRequiredConsents returns outdated when no consents exist`()]] - `method` [EXTRACTED]
- [[.`getRequiredConsents returns up-to-date when all consents match current version`()]] - `method` [EXTRACTED]
- [[.`grantConsent creates a record and can be retrieved`()]] - `method` [EXTRACTED]
- [[.`withdrawConsent creates a withdrawal record`()]] - `method` [EXTRACTED]
- [[ConsentServiceTest.kt]] - `contains` [EXTRACTED]

#graphify/code #graphify/EXTRACTED #community/ConsentService