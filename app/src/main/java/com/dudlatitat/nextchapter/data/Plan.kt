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

/**
 * Changes the "Next trip" date. When the flight planner has an upcoming trip, that trip is the
 * one shown, so it's the one that moves (its return keeps the same trip length); otherwise the
 * hand-set date changes. (Before 1.53 only the hand-set date changed, so with a planned trip
 * the card never seemed to update.)
 */
fun AppState.withNextTripDate(iso: String?): AppState {
    val now = today()
    val next = trips.filter { (parseDate(it.depart)?.toEpochDay() ?: -1) >= now }.minByOrNull { it.depart }
    val newDepart = parseDate(iso)
    if (next == null || newDepart == null) return copy(tripDate = iso)
    val shift = newDepart.toEpochDay() - parseDate(next.depart)!!.toEpochDay()
    val moved = next.copy(depart = newDepart.toString(), returnDate = parseDate(next.returnDate)?.plusDays(shift)?.toString() ?: next.returnDate)
    return copy(trips = trips.map { if (it.id == next.id) moved else it })
}

fun prettyDate(iso: String?): String? = parseDate(iso)?.format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US))

fun money(n: Int): String = NumberFormat.getCurrencyInstance(Locale.US).apply { maximumFractionDigits = 0 }.format(n)

fun AppState.savingsProgress(): Float = if (savingsGoal <= 0) 0f else (saved.toFloat() / savingsGoal).coerceIn(0f, 1f)

fun AppState.taskProgress(): Float = if (tasks.isEmpty()) 0f else tasks.count { it.done }.toFloat() / tasks.size

/** How ready you are to move: half checklist, half savings (just checklist with no goal set). */
fun AppState.moveReady(): Float = if (savingsGoal <= 0) taskProgress() else (taskProgress() + savingsProgress()) / 2f

/** A phrase picked for today: the same all day, different tomorrow. */
fun AppState.phraseOfTheDay(day: Long = today()): Card? {
    if (cards.isEmpty()) return null
    // Only cards learned before today, in a fixed order, so today's reviews and adds can't change the pick.
    val pool = cards.filter { it.firstSeen != null && it.firstSeen < day }.ifEmpty { cards }.sortedBy { it.id }
    return pool[Math.floorMod(day, pool.size.toLong()).toInt()]
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
