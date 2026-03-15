package com.m2f.template.designsystem.util

import kotlinx.datetime.LocalDateTime

/**
 * Formats an ISO-8601 date string (e.g. "2026-03-22T20:47:29.811518Z") as "DD/MM/YYYY".
 * Returns "-" if the string is blank or parsing fails.
 */
fun String.toDisplayDate(): String {
    if (isBlank()) return "-"
    return try {
        val dateTime = LocalDateTime.parse(trimIsoSuffix())
        val day = dateTime.day.toString().padStart(2, '0')
        val month = dateTime.month.ordinal.plus(1).toString().padStart(2, '0')
        val year = dateTime.year.toString()
        "$day/$month/$year"
    } catch (_: Exception) {
        "-"
    }
}

/**
 * Formats an ISO-8601 date string (e.g. "2026-03-22T20:47:29.811518Z") as "DD/MM/YYYY HH:mm".
 * Returns "-" if the string is blank or parsing fails.
 */
fun String.toDisplayDateTime(): String {
    if (isBlank()) return "-"
    return try {
        val dateTime = LocalDateTime.parse(trimIsoSuffix())
        val day = dateTime.day.toString().padStart(2, '0')
        val month = dateTime.month.ordinal.plus(1).toString().padStart(2, '0')
        val year = dateTime.year.toString()
        val hour = dateTime.hour.toString().padStart(2, '0')
        val minute = dateTime.minute.toString().padStart(2, '0')
        "$day/$month/$year $hour:$minute"
    } catch (_: Exception) {
        "-"
    }
}

/**
 * Strips the trailing "Z" or timezone offset from an ISO-8601 string
 * so that [LocalDateTime.parse] can handle it.
 *
 * Assumes the server always returns UTC timestamps.
 */
fun String.trimIsoSuffix(): String =
    removeSuffix("Z").removeSuffix("z").let { s ->
        // Remove +HH:MM or -HH:MM offset if present at the end
        val offsetPattern = Regex("[+-]\\d{2}:\\d{2}$")
        offsetPattern.replace(s, "")
    }
