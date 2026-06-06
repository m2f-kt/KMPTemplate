package com.m2f.template.permissions

import com.sun.jna.Function
import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.NativeLibrary
import com.sun.jna.Pointer

/**
 * Self-contained, macOS-only JNA bridge backing the JVM-desktop [PermissionController].
 *
 * This module deliberately does NOT reuse `core:platform`'s JNA internals: those are
 * `jvmMain`-internal and not exported. Mirroring the approach `app:designsystem` took for its
 * reduced-motion probe, `core:permissions` keeps a *minimal* self-contained JNA surface here
 * (just the ObjC-runtime calls + ApplicationServices binding it needs), and only consumes the
 * JNA-free [com.m2f.template.platform.isMacOs] boolean from `core:platform` to gate every call.
 *
 * Generalized from NoType's `MacOsMicrophonePermission` / `AccessibilityPermission.jvm.kt`:
 * - the microphone-specific `AVMediaTypeAudio` constant is parameterized to any
 *   [AvMediaType] (audio → Microphone, video → Camera);
 * - the accessibility check is the same `AXIsProcessTrusted()` call.
 *
 * Every entry point gates on [com.m2f.template.platform.isMacOs] first and wraps the native
 * call so an `UnsatisfiedLinkError` (or any `Throwable` from symbol resolution) never
 * propagates across the boundary — it maps to the safe indeterminate default.
 */

/** AVFoundation media-type constant selector for the generic device-authorization calls. */
internal enum class AvMediaType(val symbol: String) {
    AUDIO("AVMediaTypeAudio"),
    VIDEO("AVMediaTypeVideo")
}

/**
 * Minimal mapped binding to `ApplicationServices.framework` for the silent
 * `AXIsProcessTrusted()` accessibility check (never shows a prompt).
 */
internal interface ApplicationServicesLib : Library {
    fun AXIsProcessTrusted(): Boolean

    companion object {
        val INSTANCE: ApplicationServicesLib by lazy {
            Native.load("ApplicationServices", ApplicationServicesLib::class.java)
        }
    }
}

/**
 * The minimal Objective-C runtime bridge. Resolves the `objc_getClass` / `sel_registerName` /
 * `objc_msgSend` C entry points from the `objc` runtime library and the `AVMediaType*`
 * `extern NSString * const` symbols from the `AVFoundation` framework — the
 * `NativeLibrary.getInstance(...)` + `getGlobalVariableAddress(...)` idiom.
 *
 * Everything is `by lazy` so a non-macOS host (or an unused bridge) never resolves a single
 * native symbol; the public callers gate on [com.m2f.template.platform.isMacOs] first.
 */
internal object MacOsObjcBridge {

    private val objcLib: NativeLibrary by lazy { NativeLibrary.getInstance("objc") }

    /** Force the AVFoundation image to load so `AVCaptureDevice` + the media-type constants resolve. */
    private val avFoundationLib: NativeLibrary by lazy {
        NativeLibrary.getInstance(
            "/System/Library/Frameworks/AVFoundation.framework/AVFoundation"
        )
    }

    private val objcGetClass: Function by lazy { objcLib.getFunction("objc_getClass") }
    private val selRegisterName: Function by lazy { objcLib.getFunction("sel_registerName") }
    private val objcMsgSend: Function by lazy { objcLib.getFunction("objc_msgSend") }

    private val avCaptureDeviceClass: Pointer by lazy {
        objcGetClass.invokePointer(arrayOf<Any>("AVCaptureDevice"))
    }

    private val selAuthorizationStatus: Pointer by lazy {
        selRegisterName.invokePointer(arrayOf<Any>("authorizationStatusForMediaType:"))
    }

    private val selRequestAccess: Pointer by lazy {
        selRegisterName.invokePointer(arrayOf<Any>("requestAccessForMediaType:completionHandler:"))
    }

    /** Resolve an `extern NSString * const` media-type constant (one deref of its address). */
    private fun mediaTypeConstant(mediaType: AvMediaType): Pointer =
        avFoundationLib.getGlobalVariableAddress(mediaType.symbol).getPointer(0L)

    /**
     * `[AVCaptureDevice authorizationStatusForMediaType:<mediaType>]` — returns the
     * `AVAuthorizationStatus` `NSInteger`. Read the full 64-bit return via `invokeLong` and
     * narrow (the enum values 0-3 fit an `Int`); `invokeInt` would read only the low 32-bit
     * word, an ABI-fragile read of a status that gates the whole flow.
     */
    fun authorizationStatus(mediaType: AvMediaType): Int =
        objcMsgSend.invokeLong(
            arrayOf<Any>(avCaptureDeviceClass, selAuthorizationStatus, mediaTypeConstant(mediaType))
        ).toInt()

    /**
     * `[AVCaptureDevice requestAccessForMediaType:<mediaType> completionHandler:nil]` — shows
     * the macOS consent prompt. The completion handler is a native NULL (`Pointer(0L)`, not
     * `Pointer.NULL` which is literally `null` and would NPE marshalling into the untyped
     * `arrayOf<Any>`); the prompt is shown regardless and the decision is observed by polling
     * [authorizationStatus] rather than bridging an Objective-C block through JNA.
     */
    fun requestAccess(mediaType: AvMediaType) {
        objcMsgSend.invoke(
            Void::class.java,
            arrayOf<Any>(
                avCaptureDeviceClass,
                selRequestAccess,
                mediaTypeConstant(mediaType),
                Pointer(0L)
            )
        )
    }
}
