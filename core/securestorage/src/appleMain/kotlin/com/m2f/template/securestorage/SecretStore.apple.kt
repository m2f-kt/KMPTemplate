package com.m2f.template.securestorage

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDataCreate
import platform.CoreFoundation.CFDataGetBytePtr
import platform.CoreFoundation.CFDataGetLength
import platform.CoreFoundation.CFDataRef
import platform.CoreFoundation.CFDictionaryAddValue
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFMutableDictionaryRef
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFStringCreateWithCString
import platform.CoreFoundation.CFStringRef
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFStringEncodingUTF8
import platform.CoreFoundation.kCFTypeDictionaryKeyCallBacks
import platform.CoreFoundation.kCFTypeDictionaryValueCallBacks
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecItemNotFound
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData
import platform.darwin.OSStatus
import platform.posix.memcpy

/**
 * Apple (iOS) [SecretStore] backed by `Security.framework` generic-password Keychain items.
 *
 * Each `(service, account)` pair maps to a `kSecClassGenericPassword` item keyed by
 * `kSecAttrService` / `kSecAttrAccount`. Items are stored with
 * `kSecAttrAccessible = kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly` so encrypted
 * device backups CANNOT be used to extract secrets off-device.
 *
 * Dictionaries use the canonical `CFDictionaryCreateMutable + CFDictionaryAddValue` idiom
 * — the documented Kotlin/Native + Apple Security.framework pattern. Each
 * `CFMutableDictionaryRef` is `CFRelease`d after use.
 *
 * [put] does delete-then-add to keep the operation idempotent across racy concurrent
 * invocations.
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal class AppleKeychainSecretStore : SecretStore {

    override suspend fun get(service: String, account: String): ByteArray? = memScoped {
        val query = buildBaseQuery(service, account, includeReturnData = true)
        val resultVar = alloc<CFTypeRefVar>()
        try {
            val rc: OSStatus = SecItemCopyMatching(query, resultVar.ptr)
            when (rc) {
                errSecSuccess -> {
                    @Suppress("UNCHECKED_CAST")
                    val data = resultVar.value as CFDataRef? ?: return@memScoped null
                    val len = CFDataGetLength(data).toInt()
                    val out = ByteArray(len)
                    if (len > 0) {
                        out.usePinned { pinned ->
                            memcpy(pinned.addressOf(0), CFDataGetBytePtr(data), len.convert())
                        }
                    }
                    CFRelease(data)
                    out
                }
                errSecItemNotFound -> null
                else -> error("SecItemCopyMatching failed: $rc")
            }
        } finally {
            CFRelease(query)
        }
    }

    override suspend fun put(service: String, account: String, bytes: ByteArray): Unit = memScoped {
        // Delete-then-add — idempotent overwrite.
        val deleteQuery = buildBaseQuery(service, account, includeReturnData = false)
        try {
            SecItemDelete(deleteQuery)
        } finally {
            CFRelease(deleteQuery)
        }
        val attrs = buildAddAttributes(service, account, bytes)
        try {
            val rc = SecItemAdd(attrs, null)
            check(rc == errSecSuccess) { "SecItemAdd failed: $rc" }
        } finally {
            CFRelease(attrs)
        }
    }

    override suspend fun delete(service: String, account: String): Unit = memScoped {
        val deleteQuery = buildBaseQuery(service, account, includeReturnData = false)
        try {
            val rc = SecItemDelete(deleteQuery)
            // errSecItemNotFound is success for a delete (already absent / no-op).
            check(rc == errSecSuccess || rc == errSecItemNotFound) {
                "SecItemDelete failed: $rc"
            }
        } finally {
            CFRelease(deleteQuery)
        }
    }

    private fun MemScope.buildBaseQuery(
        service: String,
        account: String,
        includeReturnData: Boolean,
    ): CFMutableDictionaryRef {
        val dict = CFDictionaryCreateMutable(
            allocator = null,
            capacity = 0,
            keyCallBacks = kCFTypeDictionaryKeyCallBacks.ptr,
            valueCallBacks = kCFTypeDictionaryValueCallBacks.ptr,
        ) ?: error("CFDictionaryCreateMutable failed (query)")
        CFDictionaryAddValue(dict, kSecClass, kSecClassGenericPassword)
        addCFStringEntry(dict, kSecAttrService, service)
        addCFStringEntry(dict, kSecAttrAccount, account)
        if (includeReturnData) {
            CFDictionaryAddValue(dict, kSecReturnData, kCFBooleanTrue)
            CFDictionaryAddValue(dict, kSecMatchLimit, kSecMatchLimitOne)
        }
        return dict
    }

    private fun MemScope.buildAddAttributes(
        service: String,
        account: String,
        bytes: ByteArray,
    ): CFMutableDictionaryRef {
        val dict = CFDictionaryCreateMutable(
            allocator = null,
            capacity = 0,
            keyCallBacks = kCFTypeDictionaryKeyCallBacks.ptr,
            valueCallBacks = kCFTypeDictionaryValueCallBacks.ptr,
        ) ?: error("CFDictionaryCreateMutable failed (add)")
        CFDictionaryAddValue(dict, kSecClass, kSecClassGenericPassword)
        addCFStringEntry(dict, kSecAttrService, service)
        addCFStringEntry(dict, kSecAttrAccount, account)
        CFDictionaryAddValue(dict, kSecAttrAccessible, kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly)
        addCFDataEntry(dict, kSecValueData, bytes)
        return dict
    }

    private fun MemScope.addCFStringEntry(dict: CFMutableDictionaryRef, key: CFStringRef?, value: String) {
        val cfStr: CFStringRef = CFStringCreateWithCString(null, value, kCFStringEncodingUTF8)
            ?: error("CFStringCreateWithCString failed for '$value'")
        try {
            CFDictionaryAddValue(dict, key, cfStr)
        } finally {
            CFRelease(cfStr)
        }
    }

    private fun MemScope.addCFDataEntry(dict: CFMutableDictionaryRef, key: CFStringRef?, bytes: ByteArray) {
        val cfData: CFDataRef = bytes.usePinned { pinned ->
            CFDataCreate(null, pinned.addressOf(0).reinterpret(), bytes.size.convert())
        } ?: error("CFDataCreate failed (size=${bytes.size})")
        try {
            CFDictionaryAddValue(dict, key, cfData)
        } finally {
            CFRelease(cfData)
        }
    }
}

public actual fun defaultSecretStore(): SecretStore = AppleKeychainSecretStore()
