---
type: community
cohesion: 0.10
members: 21
---

# Server Test Helpers

**Cohesion:** 0.10 - loosely connected
**Members:** 21 nodes

## Members
- [[.createDatabase()]] - code - server/files/impl/src/test/kotlin/com/m2f/server/files/TestHelpers.kt
- [[.embed()]] - code - server/ai/impl/src/test/kotlin/com/m2f/server/ai/TestHelpers.kt
- [[.sendEmail()_2]] - code - server/groups/impl/src/test/kotlin/com/m2f/server/groups/TestHelpers.kt
- [[.start()_1]] - code - server/files/impl/src/test/kotlin/com/m2f/server/files/TestHelpers.kt
- [[.start()_2]] - code - server/files/impl/src/test/kotlin/com/m2f/server/files/TestHelpers.kt
- [[.stop()]] - code - server/files/impl/src/test/kotlin/com/m2f/server/files/TestHelpers.kt
- [[.stop()_1]] - code - server/files/impl/src/test/kotlin/com/m2f/server/files/TestHelpers.kt
- [[FakeEmbeddingProvider]] - code - server/ai/impl/src/test/kotlin/com/m2f/server/ai/TestHelpers.kt
- [[NoOpEmailService]] - code - server/groups/impl/src/test/kotlin/com/m2f/server/groups/TestHelpers.kt
- [[TestDatabase]] - code - server/files/impl/src/test/kotlin/com/m2f/server/files/TestHelpers.kt
- [[TestHelpers.kt]] - code - server/files/impl/src/test/kotlin/com/m2f/server/files/TestHelpers.kt
- [[TestMinIO]] - code - server/files/impl/src/test/kotlin/com/m2f/server/files/TestHelpers.kt
- [[createTestBucket()]] - code - server/files/impl/src/test/kotlin/com/m2f/server/files/TestHelpers.kt
- [[createTestEmbedder()]] - code - server/ai/impl/src/test/kotlin/com/m2f/server/ai/TestHelpers.kt
- [[createTestToken()]] - code - server/files/impl/src/test/kotlin/com/m2f/server/files/TestHelpers.kt
- [[createTestUser()]] - code - server/files/impl/src/test/kotlin/com/m2f/server/files/TestHelpers.kt
- [[documentTestApp()]] - code - server/ai/impl/src/test/kotlin/com/m2f/server/ai/TestHelpers.kt
- [[fileTestApp()]] - code - server/files/impl/src/test/kotlin/com/m2f/server/files/TestHelpers.kt
- [[groupTestApp()]] - code - server/groups/impl/src/test/kotlin/com/m2f/server/groups/TestHelpers.kt
- [[invitationTestApp()]] - code - server/groups/impl/src/test/kotlin/com/m2f/server/groups/TestHelpers.kt
- [[testConfiguration()]] - code - server/files/impl/src/test/kotlin/com/m2f/server/files/TestHelpers.kt

## Live Query (requires Dataview plugin)

```dataview
TABLE source_file, type FROM #community/Server_Test_Helpers
SORT file.name ASC
```
