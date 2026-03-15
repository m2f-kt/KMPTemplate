package com.m2f.template.designsystem.util

import androidx.compose.runtime.Composable
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.jetbrains.compose.resources.stringResource
import template.app.designsystem.generated.resources.Res
import template.app.designsystem.generated.resources.relative_time_days_ago
import template.app.designsystem.generated.resources.relative_time_hours_ago
import template.app.designsystem.generated.resources.relative_time_just_now
import template.app.designsystem.generated.resources.relative_time_minutes_ago
import template.app.designsystem.generated.resources.relative_time_months_ago
import template.app.designsystem.generated.resources.relative_time_weeks_ago
import template.app.designsystem.generated.resources.relative_time_years_ago
import template.app.designsystem.generated.resources.relative_time_yesterday
import kotlin.time.Clock

private sealed interface RelativeBucket {
    data object JustNow : RelativeBucket
    data class MinutesAgo(val value: Int) : RelativeBucket
    data class HoursAgo(val value: Int) : RelativeBucket
    data object Yesterday : RelativeBucket
    data class DaysAgo(val value: Int) : RelativeBucket
    data class WeeksAgo(val value: Int) : RelativeBucket
    data class MonthsAgo(val value: Int) : RelativeBucket
    data class YearsAgo(val value: Int) : RelativeBucket
    data object Fallback : RelativeBucket
}

private fun computeRelativeBucket(isoDate: String): RelativeBucket {
    if (isoDate.isBlank()) return RelativeBucket.Fallback
    return try {
        val dateTime = LocalDateTime.parse(isoDate.trimIsoSuffix())
        val instant = dateTime.toInstant(TimeZone.UTC)
        val now = Clock.System.now()
        val diffMs = now.toEpochMilliseconds() - instant.toEpochMilliseconds()

        if (diffMs < 0) return RelativeBucket.Fallback

        val minutes = diffMs / 60_000
        val hours = minutes / 60
        val days = hours / 24
        val weeks = days / 7
        val months = days / 30
        val years = days / 365

        when {
            minutes < 1 -> RelativeBucket.JustNow
            minutes < 60 -> RelativeBucket.MinutesAgo(minutes.toInt())
            hours < 24 -> RelativeBucket.HoursAgo(hours.toInt())
            days == 1L -> RelativeBucket.Yesterday
            days < 7 -> RelativeBucket.DaysAgo(days.toInt())
            weeks < 4 -> RelativeBucket.WeeksAgo(weeks.toInt())
            months < 12 -> RelativeBucket.MonthsAgo(months.toInt())
            else -> RelativeBucket.YearsAgo(years.toInt())
        }
    } catch (_: Exception) {
        RelativeBucket.Fallback
    }
}

/**
 * Formats an ISO-8601 date string as a localized relative time string.
 * Falls back to [toDisplayDate] if parsing fails or the date is in the future.
 */
@Composable
fun String.toRelativeTime(): String {
    return when (val bucket = computeRelativeBucket(this)) {
        RelativeBucket.JustNow -> stringResource(Res.string.relative_time_just_now)
        is RelativeBucket.MinutesAgo -> stringResource(Res.string.relative_time_minutes_ago, bucket.value)
        is RelativeBucket.HoursAgo -> stringResource(Res.string.relative_time_hours_ago, bucket.value)
        RelativeBucket.Yesterday -> stringResource(Res.string.relative_time_yesterday)
        is RelativeBucket.DaysAgo -> stringResource(Res.string.relative_time_days_ago, bucket.value)
        is RelativeBucket.WeeksAgo -> stringResource(Res.string.relative_time_weeks_ago, bucket.value)
        is RelativeBucket.MonthsAgo -> stringResource(Res.string.relative_time_months_ago, bucket.value)
        is RelativeBucket.YearsAgo -> stringResource(Res.string.relative_time_years_ago, bucket.value)
        RelativeBucket.Fallback -> toDisplayDate()
    }
}
