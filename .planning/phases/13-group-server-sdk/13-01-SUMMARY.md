# Plan 13-01 Summary: Shared Models, DTOs, Routes, AppError.Group

**Status:** Complete
**Duration:** ~3 min
**Commits:** 1

## What was built
- GroupRole sealed class (OWNER/ADMIN/MEMBER) with flat string serialization via GroupRoleSerializer
- 7 @Serializable DTOs: GroupResponse, CreateGroupRequest, UpdateGroupRequest, AddMemberRequest, RegisterMemberRequest, MemberResponse, PaginatedMemberResponse
- Groups @Resource route class with 9 nested endpoint definitions (create, ById, update, delete, listAll, members, addMember, removeMember, registerMember)
- AppError.Group sealed class with 4 error variants (NotFound, Forbidden, AlreadyExists, MemberAlreadyExists)

## Key files
- `core/models/src/commonMain/kotlin/com/m2f/template/models/GroupRole.kt` -- GroupRole sealed class
- `core/models/src/commonMain/kotlin/com/m2f/template/models/dto/GroupDtos.kt` -- All group DTOs + PaginatedMemberResponse
- `core/models/src/commonMain/kotlin/com/m2f/template/models/routes/ApiRoutes.kt` -- Groups @Resource routes
- `core/models/src/commonMain/kotlin/com/m2f/template/models/AppError.kt` -- AppError.Group hierarchy

## Decisions
- PaginatedMemberResponse is a concrete type (not generic PaginatedResponse<T>) to avoid kotlinx.serialization generic type complexity
- GroupRole uses same PrimitiveSerialDescriptor pattern as UserRole for consistent wire format
- Timestamps in DTOs are ISO strings (not kotlinx.datetime) for maximum KMP compatibility

## Self-Check: PASSED
- core:models compiles on metadata and JVM targets
- All DTOs are @Serializable
- Groups @Resource has 9 endpoint definitions
- AppError.Group has 4 error variants
