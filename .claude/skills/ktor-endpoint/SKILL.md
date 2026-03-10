---
name: ktor-endpoint
description: Scaffold new Ktor server endpoints with routes, service, repository, table, migration, DI wiring, and tests. Use when creating server-side API endpoints, adding new server features, or building backend functionality.
---

# Ktor Endpoint Scaffolding

## Workflow

### Step 1: Classify

Ask the user:
- Which server module? (auth, groups, files, ai, or NEW module)
- Endpoint details: HTTP method, path, auth required?, request/response DTOs

### Step 2: Create/Update @Resource Route

File: `core/models/src/commonMain/kotlin/com/m2f/template/models/routes/ApiRoutes.kt`

Pattern:
```kotlin
@Serializable
@Resource("/api/parent")
class Parent {
    @Serializable
    @Resource("action")
    class Action(val parent: Parent = Parent())
}
```

### Step 3: Create/Update DTOs

File: `core/models/src/commonMain/kotlin/com/m2f/template/models/dto/XxxDtos.kt`

Pattern: `@Serializable` data classes. All fields with kotlinx.serialization.

### Step 4: Create/Update Routes File

File: `server/<module>/src/main/kotlin/com/m2f/server/<module>/routes/XxxRoutes.kt`

Pattern:
```kotlin
fun Route.xxxRoutes(service: XxxService) {
    authenticate {  // only if auth required
        post<Route.Action> {
            conduitAuth(HttpStatusCode.Created) {  // conduitAuth for authenticated, conduit for public
                val request = getModel<ActionRequest>()
                service.doAction(userId, request)
            }
        }
    }
}
```

- `conduit()` for public endpoints, `conduitAuth()` for authenticated
- `conduitAuth` provides `userId` in its lambda scope
- `getModel<T>()` deserializes request body
- For role-restricted: wrap in `withRole(UserRole.Admin, UserRole.PowerAdmin) { ... }`

### Step 5: Create/Update Service

File: `server/<module>/src/main/kotlin/com/m2f/server/<module>/service/XxxService.kt`

Pattern:
```kotlin
class XxxService(
    private val repository: XxxRepository,
    // other dependencies
) {
    context(raise: Raise<DomainError>)
    suspend fun doAction(request: ActionRequest): ActionResponse {
        // Validation with ensure()
        // Business logic
        // Repository calls
    }
}
```

- All business methods use `context(raise: Raise<DomainError>)`
- Use Arrow's `ensure()`, `zipOrAccumulate()` for validation
- Never throw exceptions — always raise domain errors

### Step 6: Create/Update Repository (if new table needed)

File: `server/<module>/src/main/kotlin/com/m2f/server/<module>/repository/XxxRepository.kt`

Pattern:
```kotlin
class XxxRepository(private val db: R2dbcDatabase) {
    suspend fun findById(id: Uuid): XxxRecord? = suspendTransaction(db = db) {
        XxxTable.select(XxxTable.columns)
            .where { XxxTable.id eq id }
            .singleOrNull()
            ?.toXxxRecord()
    }

    suspend fun insert(...): Uuid = suspendTransaction(db = db) {
        XxxTable.insert { ... }[XxxTable.id]
    }
}

data class XxxRecord(val id: Uuid, ...)
```

- Always use `suspendTransaction(db = db) { ... }`
- Return Record data classes, not raw ResultRow
- Extension function `.toXxxRecord()` for mapping

### Step 7: Create Table (if new)

File: `server/<module>/src/main/kotlin/com/m2f/server/<module>/tables/XxxTable.kt`

Pattern:
```kotlin
object XxxTable : Table("table_name") {
    val id = uuid("id").autoGenerate()
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
    override val primaryKey = PrimaryKey(id)
}
```

### Step 8: Create Migration

File: `server/<module>/src/main/kotlin/com/m2f/server/<module>/Xxx.kt` (module entry file)

Pattern:
```kotlin
internal class CreateXxxTableMigration : Migration {
    override val version: String = "YYYYMMDDHHMMSS"  // timestamp-based
    override val description: String = "Create xxx table"
    override suspend fun migrate() {
        SchemaUtils.create(XxxTable)
    }
}

fun registerXxxMigrations() {
    MigrationRegistry.register(CreateXxxTableMigration())
}
```

Then call `registerXxxMigrations()` in `Application.kt`'s `config { }` block.

### Step 9: Wire Koin DI

File: `server/<module>/src/main/kotlin/com/m2f/server/<module>/di/XxxModule.kt`

Pattern:
```kotlin
val xxxModule = module {
    single { XxxRepository(get<R2dbcDatabase>()) }
    single { XxxService(get(), ...) }
}
```

Then include in `serverModule`: `includes(xxxModule)`

### Step 10: Wire Routes in Application.kt

File: `server/src/main/kotlin/com/m2f/template/Application.kt`

In the `routing { }` block:
```kotlin
val xxxService: XxxService by inject()
xxxRoutes(xxxService)
```

### Step 11: Create Domain Errors (if needed)

File: `server/<module>/src/main/kotlin/com/m2f/server/<module>/errors/XxxErrors.kt`

Pattern:
```kotlin
class XxxNotFound : DomainError {
    override val status = HttpStatusCode.NotFound
    override val code = "xxx_not_found"
    override val detail = "Xxx not found"
}
```

### Step 12: Write Tests

File: `server/<module>/src/test/kotlin/com/m2f/server/<module>/XxxRoutesTest.kt`

Pattern:
```kotlin
class XxxRoutesTest {
    companion object {
        private lateinit var database: R2dbcDatabase
        @BeforeClass @JvmStatic fun setup() {
            TestDatabase.start()
            database = TestDatabase.createDatabase()
        }
    }

    @Test
    fun `test endpoint description`() = runTest {
        xxxTestApp(database) {
            val client = createClient { install(ContentNegotiation) { json() } }
            val response = client.post("/api/path") {
                bearerAuth(createTestToken(userId, UserRole.User))
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            response.status shouldBe HttpStatusCode.Created
            val body = response.body<ResponseType>()
            body.field shouldBe expected
        }
    }
}
```

- Server tests use Kotest `shouldBe` assertions

### Step 13: Verify Compilation

```bash
./gradlew :core:models:compileCommonMainKotlinMetadata
./gradlew :server:<module>:compileKotlin
./gradlew :server:<module>:test
```

## Critical Rules

- ALWAYS use Arrow Raise, never try/catch in services
- ALWAYS use `suspendTransaction` for DB operations
- ALWAYS register new migrations in Application.kt
- NEVER put business logic in routes — routes only call services
- Server tests use Kotest `shouldBe` assertions
- New modules need `build.gradle.kts` with `server-module-convention` plugin and entry in `settings.gradle.kts`
