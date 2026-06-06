package com.m2f.template.platform

import com.sun.jna.Function
import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.NativeLibrary
import com.sun.jna.NativeLong
import com.sun.jna.Pointer
import com.sun.jna.ptr.PointerByReference

/**
 * Generic macOS JNA scaffolding extracted from NoType's `MacOsNative` / `NativeWindow` /
 * `MacOsMicrophonePermission`. This file holds only the REUSABLE primitives that any
 * JVM-desktop feature could need to talk to macOS:
 *
 *  - [AppKitLib] — the Objective-C runtime via AppKit (`objc_getClass` / `sel_registerName` /
 *    `objc_msgSend`).
 *  - [ApplicationServicesLib] — generic Accessibility (AX) element reading + AX trust query.
 *  - [CoreFoundationLib] — CFString create/read + CFRelease.
 *  - CoreFoundation constant resolvers ([kCFRunLoopCommonModes], [kCFBooleanTrue]).
 *  - [ObjcBridge] — a high-level helper for the common "read a BOOL property off a shared
 *    singleton class" Objective-C call, used e.g. by reduced-motion checks.
 *
 * Product-specific bits are deliberately LEFT BEHIND in NoType (not extracted): CGEventTap
 * creation/enable + the CGEventTapCallback, CGEvent field/flag introspection, global-hotkey
 * virtual keycodes and CGEvent constants, the AX-trust prompt dictionary, the AWT→NSWindow
 * reflective peer walk, and the AVFoundation microphone-permission flow.
 *
 * Everything loads lazily; callers MUST gate on [isMacOsHost] before touching any `INSTANCE`
 * or constant, because the library load fails on non-macOS hosts.
 */

/**
 * JNA bindings to the Objective-C runtime via the AppKit framework. AppKit re-exports
 * `objc_msgSend` and `sel_registerName` — loading "AppKit" is sufficient (no separate `objc`
 * library load needed).
 *
 * `objc_msgSend` is variadic in C; the single-`Long`-arg overload we declare here is matched by
 * JNA by name + arg count. For other call shapes use the lower-level [ObjcBridge] / raw
 * [Function] approach.
 */
internal interface AppKitLib : Library {
    fun sel_registerName(name: String): Pointer
    fun objc_getClass(name: String): Long
    fun objc_msgSend(receiver: Long, selector: Pointer, arg: Long): Long

    companion object {
        @Suppress("ObjectPropertyNaming") // JNA convention
        val INSTANCE: AppKitLib by lazy {
            Native.load("AppKit", AppKitLib::class.java) as AppKitLib
        }
    }
}

/**
 * JNA bindings to the reusable slice of `ApplicationServices` — generic Accessibility (AX)
 * element reading plus the AX-trust query. Bound deliberately narrow.
 *
 * Loaded at first reference via [INSTANCE]; on non-macOS hosts the load fails — callers MUST
 * guard with [isMacOsHost] before touching [INSTANCE].
 */
internal interface ApplicationServicesLib : Library {
    /** `Boolean AXIsProcessTrusted(void)` — silent (non-prompting) Accessibility-trust check. */
    fun AXIsProcessTrusted(): Boolean

    /**
     * `AXUIElementRef AXUIElementCreateSystemWide(void)` — the root system-wide AX element whose
     * `kAXFocusedUIElementAttribute` resolves to whatever control currently has keyboard focus.
     * Returns a CFType the caller MUST `CFRelease`.
     */
    fun AXUIElementCreateSystemWide(): Pointer?

    /**
     * `AXError AXUIElementCopyAttributeValue(AXUIElementRef element, CFStringRef attribute,
     *      CFTypeRef *value)` — copies the value of [attribute] on [element] into [value].
     * Returns `kAXErrorSuccess` (0) on success; the out-param then holds a CFType the caller MUST
     * `CFRelease`. Any non-zero AXError means no value was written (do not release the out-param).
     */
    fun AXUIElementCopyAttributeValue(element: Pointer, attribute: Pointer, value: PointerByReference): Int

    /** `void CFRelease(CFTypeRef cf)` — releases a CFType. Bound here so AX callers stay self-contained. */
    fun CFRelease(cf: Pointer?)

    companion object {
        @Suppress("ObjectPropertyNaming") // JNA convention
        val INSTANCE: ApplicationServicesLib by lazy {
            Native.load("ApplicationServices", ApplicationServicesLib::class.java) as ApplicationServicesLib
        }
    }
}

/**
 * JNA bindings to the slice of CoreFoundation needed to build CFString keys and read a CFString
 * value back into a Kotlin [String], plus [CFRelease].
 *
 * Loaded via [INSTANCE]; like the rest of the `MacOs*` JNA family it must only be touched after
 * an [isMacOsHost] guard.
 */
internal interface CoreFoundationLib : Library {
    /**
     * `CFStringRef CFStringCreateWithCString(CFAllocatorRef alloc, const char *cStr,
     *      CFStringEncoding encoding)` — builds a CFString from a NUL-terminated C string.
     * Pass `null` allocator (default) and [K_CF_STRING_ENCODING_UTF8]. The returned CFString is
     * owned by the caller and MUST be `CFRelease`d.
     */
    fun CFStringCreateWithCString(alloc: Pointer?, cStr: String, encoding: Int): Pointer?

    /**
     * `Boolean CFStringGetCString(CFStringRef theString, char *buffer, CFIndex bufferSize,
     *      CFStringEncoding encoding)` — copies the CFString into [buffer] (UTF-8). Returns false
     * if the buffer was too small.
     */
    fun CFStringGetCString(theString: Pointer, buffer: ByteArray, bufferSize: NativeLong, encoding: Int): Boolean

    /** `void CFRelease(CFTypeRef cf)` — releases a CFType. */
    fun CFRelease(cf: Pointer?)

    companion object {
        /** `kCFStringEncodingUTF8` from CoreFoundation's CFStringEncodingExt. */
        const val K_CF_STRING_ENCODING_UTF8: Int = 0x08000100

        @Suppress("ObjectPropertyNaming") // JNA convention
        val INSTANCE: CoreFoundationLib by lazy {
            Native.load("CoreFoundation", CoreFoundationLib::class.java) as CoreFoundationLib
        }
    }
}

/**
 * Resolves the `kCFRunLoopCommonModes` CFStringRef constant from CoreFoundation.
 *
 * The header declares it as `extern const CFStringRef kCFRunLoopCommonModes;` —
 * `getGlobalVariableAddress` returns the address of the variable, and one pointer dereference
 * (`getPointer(0)`) yields the CFStringRef itself. Required by `CFRunLoopAddSource` (passing NULL
 * there crashes inside CoreFoundation).
 *
 * Nullable + try/catch: a future macOS renaming/removing the symbol would otherwise throw
 * `UnsatisfiedLinkError` out of this lazy; returning null lets callers short-circuit gracefully.
 */
internal val kCFRunLoopCommonModes: Pointer? by lazy {
    runCatching {
        NativeLibrary.getInstance("CoreFoundation")
            .getGlobalVariableAddress("kCFRunLoopCommonModes")
            .getPointer(0L)
    }.getOrNull()
}

/**
 * Resolves the `kCFBooleanTrue` CFBooleanRef constant from CoreFoundation.
 *
 * `extern const CFBooleanRef kCFBooleanTrue;` — `getGlobalVariableAddress` + one dereference
 * yields the CFBooleanRef. Generic CoreFoundation primitive (e.g. the `true` value of a one-entry
 * CFDictionary). Nullable + try/catch for the same defensive reason as [kCFRunLoopCommonModes].
 */
internal val kCFBooleanTrue: Pointer? by lazy {
    runCatching {
        NativeLibrary.getInstance("CoreFoundation")
            .getGlobalVariableAddress("kCFBooleanTrue")
            .getPointer(0L)
    }.getOrNull()
}

/**
 * High-level Objective-C runtime bridge for the common "send a zero-/one-arg message" shape.
 * Resolves `objc_getClass` / `sel_registerName` / `objc_msgSend` from the `objc` runtime library
 * once, lazily, and exposes typed helpers so feature code never marshals raw [Function] calls.
 *
 * Generalized from NoType's mic-permission `ObjcBridge` (the AVFoundation / `AVMediaTypeAudio`
 * specifics were left behind). Everything is `by lazy`, so a non-macOS host never resolves a
 * single native symbol; callers MUST gate on [isMacOsHost] first.
 */
internal object ObjcBridge {

    private val objcLib: NativeLibrary by lazy { NativeLibrary.getInstance("objc") }

    private val objcGetClass: Function by lazy { objcLib.getFunction("objc_getClass") }
    private val selRegisterName: Function by lazy { objcLib.getFunction("sel_registerName") }
    private val objcMsgSend: Function by lazy { objcLib.getFunction("objc_msgSend") }

    /** `Class objc_getClass(const char *name)` — the named Objective-C class pointer. */
    fun objcClass(name: String): Pointer = objcGetClass.invokePointer(arrayOf<Any>(name))

    /** `SEL sel_registerName(const char *name)` — the selector for a method name. */
    fun selector(name: String): Pointer = selRegisterName.invokePointer(arrayOf<Any>(name))

    /** `id objc_msgSend(receiver, selector)` — zero-arg message, returning an `id`/pointer. */
    fun sendPointer(receiver: Pointer, selector: Pointer): Pointer =
        objcMsgSend.invokePointer(arrayOf<Any>(receiver, selector))

    /**
     * `objc_msgSend(receiver, selector)` read as a full 64-bit return, narrowed to [Boolean].
     * Objective-C `BOOL` is returned in the low byte of the result register, so we mask before
     * testing (reading the full long avoids ABI-fragile high-word assumptions of `invokeInt`).
     */
    fun sendBool(receiver: Pointer, selector: Pointer): Boolean =
        (objcMsgSend.invokeLong(arrayOf<Any>(receiver, selector)) and OBJC_BOOL_MASK) != 0L

    /** Objective-C `BOOL` lives in the low byte of the return register. */
    private const val OBJC_BOOL_MASK: Long = 0xFFL
}
