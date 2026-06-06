package com.m2f.template.permissions

import com.m2f.template.models.AppError
import com.m2f.template.models.Permission
import com.m2f.template.models.PermissionStatus
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.test.Test

/**
 * Unit tests for the PURE, platform-independent mapping helpers. No native symbol is resolved
 * and no host privacy state is touched, so the full branch matrix is exercised on every target.
 */
class PermissionMappingTest {

    @Test
    fun `osAuthorizationCodeToStatus maps authorized code to Granted`() {
        osAuthorizationCodeToStatus(OsAuthorizationCode.AUTHORIZED) shouldBe PermissionStatus.Granted
    }

    @Test
    fun `osAuthorizationCodeToStatus maps denied code to Denied`() {
        osAuthorizationCodeToStatus(OsAuthorizationCode.DENIED) shouldBe PermissionStatus.Denied
    }

    @Test
    fun `osAuthorizationCodeToStatus maps restricted code to Restricted`() {
        osAuthorizationCodeToStatus(OsAuthorizationCode.RESTRICTED) shouldBe PermissionStatus.Restricted
    }

    @Test
    fun `osAuthorizationCodeToStatus maps not-determined code to NotDetermined`() {
        osAuthorizationCodeToStatus(OsAuthorizationCode.NOT_DETERMINED) shouldBe PermissionStatus.NotDetermined
    }

    @Test
    fun `osAuthorizationCodeToStatus maps unknown codes to Unknown`() {
        osAuthorizationCodeToStatus(-1) shouldBe PermissionStatus.Unknown
        osAuthorizationCodeToStatus(99) shouldBe PermissionStatus.Unknown
    }

    @Test
    fun `permissionStatusToOutcome returns Right for Granted`() {
        permissionStatusToOutcome(Permission.Microphone, PermissionStatus.Granted) shouldBe
            arrow.core.Either.Right(Unit)
    }

    @Test
    fun `permissionStatusToOutcome maps Denied to Permission Denied carrying the permission`() {
        val outcome = permissionStatusToOutcome(Permission.Camera, PermissionStatus.Denied)
        val error = outcome.leftOrNull()
        error.shouldBeInstanceOf<AppError.Permission.Denied>()
        error.permission shouldBe Permission.Camera
    }

    @Test
    fun `permissionStatusToOutcome maps Restricted to Permission Restricted carrying the permission`() {
        val outcome = permissionStatusToOutcome(Permission.Accessibility, PermissionStatus.Restricted)
        val error = outcome.leftOrNull()
        error.shouldBeInstanceOf<AppError.Permission.Restricted>()
        error.permission shouldBe Permission.Accessibility
    }

    @Test
    fun `permissionStatusToOutcome maps NotDetermined to Permission NotDetermined carrying the permission`() {
        val outcome = permissionStatusToOutcome(Permission.Notifications, PermissionStatus.NotDetermined)
        val error = outcome.leftOrNull()
        error.shouldBeInstanceOf<AppError.Permission.NotDetermined>()
        error.permission shouldBe Permission.Notifications
    }

    @Test
    fun `permissionStatusToOutcome maps Unknown to Native Unavailable`() {
        val outcome = permissionStatusToOutcome(Permission.Location, PermissionStatus.Unknown)
        outcome.leftOrNull().shouldBeInstanceOf<AppError.Native.Unavailable>()
    }
}
