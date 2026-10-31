package com.dudlatitat.nextchapter.data

import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

fun parseDate(iso: String?): LocalDate? = try { iso?.let(LocalDate::parse) } catch (_: Exception) { null }

/** Days from today to an ISO date (negative once it's past), or null if unset. */
fun daysUntil(iso: String?): Long? = parseDate(iso)?.let { it.toEpochDay() - today() }

/** The next trip: the earliest upcoming one in the flight planner, else the date set by hand. */
fun AppState.nextTripDate(): String? {
    val now = today()
    return trips.mapNotNull { parseDate(it.depart) }.filter { it.toEpochDay() >= now }.minOrNull()?.toString()
        ?: tripDate
}

fun prettyDate(iso: String?): String? = parseDate(iso)?.format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US))

fun money(n: Int): String = NumberFormat.getCurrencyInstance(Locale.US).apply { maximumFractionDigits = 0 }.format(n)

fun AppState.savingsProgress(): Float = if (savingsGoal <= 0) 0f else (saved.toFloat() / savingsGoal).coerceIn(0f, 1f)

fun AppState.taskProgress(): Float = if (tasks.isEmpty()) 0f else tasks.count { it.done }.toFloat() / tasks.size

/** How ready you are to move: half checklist, half savings (just checklist with no goal set). */
fun AppState.moveReady(): Float = if (savingsGoal <= 0) taskProgress() else (taskProgress() + savingsProgress()) / 2f

/** A phrase picked for today: the same all day, different tomorrow. */
fun AppState.phraseOfTheDay(): Card? {
    if (cards.isEmpty()) return null
    val pool = cards.filter { !it.isNew }.ifEmpty { cards }
    return pool[(today() % pool.size).toInt()]
}

/** Months and days, for a friendly countdown ("1 yr 7 mo"). */
fun friendlySpan(days: Long): String = when {
    days < 0 -> "passed"
    days == 0L -> "today"
    days < 100 -> if (days == 1L) "1 day" else "$days days"
    days < 365 -> "${days / 30} months"
    else -> {
        val y = days / 365
        val m = (days % 365) / 30
        if (m == 0L) "$y yr" else "$y yr $m mo"
    }
}
