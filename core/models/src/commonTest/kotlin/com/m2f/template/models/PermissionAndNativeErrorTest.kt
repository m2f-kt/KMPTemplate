package com.m2f.template.models

import com.m2f.template.models.localization.StringKey
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.serialization.json.Json
import kotlin.test.Test

class PermissionAndNativeErrorTest {

    private val json = Json

    @Test
    fun `Permission Denied carries the offending permission and code`() {
        val error = AppError.Permission.Denied(Permission.Microphone)
        error.permission shouldBe Permission.Microphone
        error.code shouldBe "PERMISSION_DENIED"
        error.message shouldBe "Permission was denied: Microphone"
    }

    @Test
    fun `Permission Restricted and NotDetermined expose distinct codes`() {
        AppError.Permission.Restricted(Permission.Camera).code shouldBe "PERMISSION_RESTRICTED"
        AppError.Permission.NotDetermined(Permission.Location).code shouldBe "PERMISSION_NOT_DETERMINED"
    }

    @Test
    fun `Native Unavailable defaults its message and Failure carries a reason`() {
        AppError.Native.Unavailable().message shouldBe
            "Native integration is not available on this platform"
        AppError.Native.Unavailable().code shouldBe "NATIVE_UNAVAILABLE"

        val failure = AppError.Native.Failure(reason = "tap create returned null")
        failure.code shouldBe "NATIVE_FAILURE"
        failure.message shouldBe "Native integration failed: tap create returned null"
    }

    @Test
    fun `Permission Denied round-trips through polymorphic JSON`() {
        val original: AppError = AppError.Permission.Denied(Permission.Accessibility)
        val decoded = json.decodeFromString<AppError>(json.encodeToString(original))
        decoded shouldBe original
    }

    @Test
    fun `Native Failure round-trips through polymorphic JSON`() {
        val original: AppError = AppError.Native.Failure(reason = "bridge closed")
        val decoded = json.decodeFromString<AppError>(json.encodeToString(original))
        decoded shouldBe original
    }

    @Test
    fun `Permission enum round-trips through JSON`() {
        Permission.entries.forEach { permission ->
            json.decodeFromString<Permission>(json.encodeToString(permission)) shouldBe permission
        }
    }

    @Test
    fun `PermissionStatus data objects round-trip through JSON`() {
        val statuses = listOf(
            PermissionStatus.Granted,
            PermissionStatus.Denied,
            PermissionStatus.Restricted,
            PermissionStatus.NotDetermined,
            PermissionStatus.Unknown
        )
        statuses.forEach { status ->
            val decoded = json.decodeFromString<PermissionStatus>(json.encodeToString<PermissionStatus>(status))
            decoded shouldBe status
        }
    }

    @Test
    fun `PermissionStatus Granted is exhaustively matchable`() {
        val status: PermissionStatus = PermissionStatus.Granted
        status.shouldBeInstanceOf<PermissionStatus.Granted>()
    }

    @Test
    fun `new error codes resolve to matching StringKeys`() {
        StringKey.fromCode("PERMISSION_DENIED") shouldBe StringKey.PERMISSION_DENIED
        StringKey.fromCode("PERMISSION_RESTRICTED") shouldBe StringKey.PERMISSION_RESTRICTED
        StringKey.fromCode("PERMISSION_NOT_DETERMINED") shouldBe StringKey.PERMISSION_NOT_DETERMINED
        StringKey.fromCode("NATIVE_UNAVAILABLE") shouldBe StringKey.NATIVE_UNAVAILABLE
        StringKey.fromCode("NATIVE_FAILURE") shouldBe StringKey.NATIVE_FAILURE
    }

    @Test
    fun `every new AppError code has a corresponding StringKey`() {
        val codes = listOf(
            AppError.Permission.Denied(Permission.Microphone).code,
            AppError.Permission.Restricted(Permission.Microphone).code,
            AppError.Permission.NotDetermined(Permission.Microphone).code,
            AppError.Native.Unavailable().code,
            AppError.Native.Failure(reason = "x").code
        )
        codes.forEach { code ->
            StringKey.fromCode(code).shouldBeInstanceOf<StringKey>()
        }
    }
}
