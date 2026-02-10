package com.m2f.template.logging

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity

/**
 * Application logging utility built on Kermit.
 * Provides module-tagged loggers following the convention: [MODULE_TAG]
 *
 * Per project convention:
 * - Module tags: [AUTH], [SDK], [STORAGE], [AI], [APP], [NAV]
 * - Structured logs include key-value metadata
 * - Default log level: Debug (verbose for first-run DX)
 *
 * Usage:
 * ```
 * val logger = AppLogger.withTag(LogTag.Auth)
 * logger.d("Login attempt", mapOf("email" to email, "status" to "pending"))
 * ```
 */
object AppLogger {

    init {
        // Set minimum severity to Debug for template DX
        Logger.setMinSeverity(Severity.Debug)
    }

    fun withTag(tag: LogTag): TaggedLogger = TaggedLogger(tag)
    fun withTag(tag: String): TaggedLogger = TaggedLogger(LogTag.Custom(tag))
}

sealed class LogTag(val value: String) {
    data object Auth : LogTag("AUTH")
    data object Sdk : LogTag("SDK")
    data object Storage : LogTag("STORAGE")
    data object Ai : LogTag("AI")
    data object App : LogTag("APP")
    data object Nav : LogTag("NAV")
    data object Di : LogTag("DI")
    data class Custom(val tag: String) : LogTag(tag)
}

class TaggedLogger(private val tag: LogTag) {
    private val logger = Logger.withTag("[${tag.value}]")

    fun v(message: String, metadata: Map<String, Any?> = emptyMap()) {
        logger.v { formatMessage(message, metadata) }
    }

    fun d(message: String, metadata: Map<String, Any?> = emptyMap()) {
        logger.d { formatMessage(message, metadata) }
    }

    fun i(message: String, metadata: Map<String, Any?> = emptyMap()) {
        logger.i { formatMessage(message, metadata) }
    }

    fun w(message: String, throwable: Throwable? = null, metadata: Map<String, Any?> = emptyMap()) {
        if (throwable != null) {
            logger.w(throwable) { formatMessage(message, metadata) }
        } else {
            logger.w { formatMessage(message, metadata) }
        }
    }

    fun e(message: String, throwable: Throwable? = null, metadata: Map<String, Any?> = emptyMap()) {
        if (throwable != null) {
            logger.e(throwable) { formatMessage(message, metadata) }
        } else {
            logger.e { formatMessage(message, metadata) }
        }
    }

    private fun formatMessage(message: String, metadata: Map<String, Any?>): String {
        if (metadata.isEmpty()) return message
        val metaString = metadata.entries.joinToString(", ") { "${it.key}=${it.value}" }
        return "$message | $metaString"
    }
}
