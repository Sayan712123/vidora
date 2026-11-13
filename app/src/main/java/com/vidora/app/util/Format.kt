package com.vidora.app.util

import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * ISO-8601 duration ("PT1H4M9S") to clock text ("1:04:09").
 *
 * Parsed by hand rather than with java.time so the app keeps working on API 21
 * without the extra weight of core-library desugaring.
 */
fun formatIsoDuration(iso: String): String {
    if (iso.isBlank() || iso == "P0D" || !iso.startsWith("PT")) return ""
    var hours = 0L
    var minutes = 0L
    var seconds = 0L
    var number = StringBuilder()
    for (ch in iso.removePrefix("PT")) {
        when (ch) {
            'H' -> { hours = number.toString().toLongOrNull() ?: 0L; number = StringBuilder() }
            'M' -> { minutes = number.toString().toLongOrNull() ?: 0L; number = StringBuilder() }
            'S' -> { seconds = number.toString().toLongOrNull() ?: 0L; number = StringBuilder() }
            in '0'..'9' -> number.append(ch)
            else -> return ""
        }
    }
    if (hours == 0L && minutes == 0L && seconds == 0L) return ""
    return if (hours > 0) {
        String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%d:%02d", minutes, seconds)
    }
}

/** 1234567 -> "1.2M" */
fun compactCount(value: Long): String = when {
    value >= 1_000_000_000L -> trimZero(value / 1_000_000_000.0) + "B"
    value >= 1_000_000L -> trimZero(value / 1_000_000.0) + "M"
    value >= 1_000L -> trimZero(value / 1_000.0) + "K"
    else -> value.toString()
}

private fun trimZero(v: Double): String {
    val s = String.format(Locale.US, "%.1f", v)
    return if (s.endsWith(".0")) s.dropLast(2) else s
}

fun viewLabel(views: Long): String = if (views <= 0L) "" else compactCount(views) + " views"

fun subscriberLabel(subs: Long): String =
    if (subs <= 0L) "Subscribers hidden" else compactCount(subs) + " subscribers"

private fun parseIsoInstantMillis(iso: String): Long? {
    return runCatching {
        val pattern = Regex("(\\d{4})-(\\d{2})-(\\d{2})[T ](\\d{2}):(\\d{2}):(\\d{2})")
        val m = pattern.find(iso) ?: return null
        val (y, mo, d, h, mi, s) = m.destructured
        val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        cal.clear()
        cal.set(
            y.toInt(), mo.toInt() - 1, d.toInt(),
            h.toInt(), mi.toInt(), s.toInt()
        )
        cal.timeInMillis
    }.getOrNull()
}

/** "2024-04-02T10:00:00Z" -> "2 years ago" */
fun relativeTime(iso: String): String {
    val millis = parseIsoInstantMillis(iso) ?: return ""
    val minutes = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - millis)
        .coerceAtLeast(0L)
    val hours = minutes / 60
    val days = hours / 24
    return when {
        minutes < 1L -> "Just now"
        minutes < 60L -> plural(minutes, "minute")
        hours < 24L -> plural(hours, "hour")
        days < 7L -> plural(days, "day")
        days < 30L -> plural(days / 7, "week")
        days < 365L -> plural(days / 30, "month")
        else -> plural(days / 365, "year")
    }
}

private fun plural(n: Long, unit: String): String =
    if (n == 1L) "1 $unit ago" else "$n ${unit}s ago"

fun initialOf(name: String): String =
    name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
