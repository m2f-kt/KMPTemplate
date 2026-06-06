package com.m2f.template.permissions

import com.m2f.template.models.Permission
import com.m2f.template.models.PermissionStatus
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.nulls.shouldNotBeNull
import kotlin.test.Test

/**
 * JVM-host tests for the desktop [PermissionController] actual.
 *
 * These assert the read-only [PermissionController.status] probe behaves safely on the build
 * host: it returns a valid [PermissionStatus] WITHOUT throwing and — crucially — WITHOUT
 * mutating TCC (it never calls the prompting `requestAccess` path). On a non-macOS CI host the
 * controller short-circuits to [PermissionStatus.Unknown]; on a macOS host it performs the
 * silent AVFoundation / AXIsProcessTrusted read. Either outcome is a non-null, valid status.
 */
class PermissionControllerJvmTest {

    private val validStatuses = listOf(
        PermissionStatus.Granted,
        PermissionStatus.Denied,
        PermissionStatus.Restricted,
        PermissionStatus.NotDetermined,
        PermissionStatus.Unknown
    )

    @Test
    fun `status for Microphone returns a valid status without throwing`() {
        val status = PermissionController().status(Permission.Microphone)
        status.shouldNotBeNull()
        validStatuses shouldContain status
    }

    @Test
    fun `status for every permission returns a valid status without throwing`() {
        val controller = PermissionController()
        Permission.entries.forEach { permission ->
            val status = controller.status(permission)
            validStatuses shouldContain status
        }
    }
}
