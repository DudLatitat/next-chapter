package com.dudlatitat.nextchapter.data

import kotlinx.serialization.Serializable

/** A saved spot (Work or Home), set on the phone (address, map pin or "my location"), never written in the code. */
@Serializable
data class Spot(val lat: Double, val lon: Double, val label: String = "", val radius: Int = Places.DEFAULT_RADIUS)

/** One arrival in a day: [kind] is "work" or "home"; [minutes] after midnight (work = start of the workday). */
@Serializable
data class Arrival(val day: Long, val kind: String, val minutes: Int)

/**
 * Knowing when you're at work and home, from arrivals the phone notices (Settings → Work & home).
 * Arriving at work starts the workday 5 minutes later (getting out of the car); the day is
 * expected to end 8 hours after that. Getting home after that opens the evening: "You're home"
 * and tonight's job. A quick trip home at lunch doesn't count.
 */
object Places {
    const val GRACE = 5
    const val WORKDAY = 8 * 60
    /** A home arrival only counts this long after the workday started (so lunch at home doesn't end the day). */
    const val MIN_AT_WORK = 4 * 60
    /** If the phone never notices you getting home, the evening opens this long after the expected end. */
    const val FALLBACK = 45
    const val DEFAULT_RADIUS = 150
    const val MIN_RADIUS = 50
    const val MAX_RADIUS = 500
    /** A work arrival only starts the workday in the morning (so testing at home at 5 pm can't make "work" run to midnight). */
    const val WORK_FROM = 5 * 60
    const val WORK_UNTIL = 13 * 60

    fun workStart(arrivals: List<Arrival>, day: Long): Int? = arrivals.firstOrNull { it.day == day && it.kind == "work" }?.minutes
    fun homeArrival(arrivals: List<Arrival>, day: Long): Int? = arrivals.firstOrNull { it.day == day && it.kind == "home" }?.minutes
    fun workEnd(arrivals: List<Arrival>, day: Long): Int? = workStart(arrivals, day)?.let { (it + WORKDAY).coerceAtMost(LAST) }

    /** The result of noticing an arrival: the new log, and whether this was "just got home from work". */
    data class Entered(val arrivals: List<Arrival>, val home: Boolean)

    /** You walked into [kind] ("work"/"home") at [minutes] on [day]. Only the first of each per day counts. */
    fun onEnter(arrivals: List<Arrival>, kind: String, day: Long, minutes: Int): Entered {
        val unchanged = Entered(arrivals, false)
        return when (kind) {
            "work" -> {
                if (minutes !in WORK_FROM until WORK_UNTIL) unchanged
                else if (workStart(arrivals, day) != null || homeArrival(arrivals, day) != null) unchanged
                else Entered(add(arrivals, Arrival(day, "work", (minutes + GRACE).coerceAtMost(LAST))), false)
            }
            "home" -> {
                val start = workStart(arrivals, day) ?: return unchanged
                if (homeArrival(arrivals, day) != null || minutes < start + MIN_AT_WORK) unchanged
                else Entered(add(arrivals, Arrival(day, "home", minutes)), true)
            }
            else -> unchanged
        }
    }

    /** Forget today's arrivals (Reset today, or after a spot changes), keeping earlier days. */
    fun resetDay(arrivals: List<Arrival>, day: Long): List<Arrival> = arrivals.filterNot { it.day == day }

    /** "I'm home" by hand: always counts (once a day). */
    fun manualHome(arrivals: List<Arrival>, day: Long, minutes: Int): List<Arrival> =
        if (homeArrival(arrivals, day) != null) arrivals else add(arrivals, Arrival(day, "home", minutes))

    /**
     * When today's evening starts (minutes after midnight): the moment you got home if the phone
     * noticed; else the expected end of work + [FALLBACK] if it saw you at work; else your usual
     * home time from Settings.
     */
    fun homeTime(state: AppState, day: Long): Int {
        if (!state.placesOn) return state.homeMinutes
        homeArrival(state.arrivals, day)?.let { return it }
        workEnd(state.arrivals, day)?.let { return (it + FALLBACK).coerceAtMost(LAST) }
        return state.homeMinutes
    }

    /** At work right now: the workday started today, you're not home yet, and the evening hasn't opened. */
    fun atWork(state: AppState, day: Long, nowMinutes: Int): Boolean {
        val start = workStart(state.arrivals, day) ?: return false
        return state.placesOn && homeArrival(state.arrivals, day) == null && nowMinutes >= start - GRACE && nowMinutes < homeTime(state, day)
    }

    private const val LAST = 23 * 60 + 59
    private fun add(arrivals: List<Arrival>, a: Arrival) = (arrivals + a).filter { it.day > a.day - 60 }
}
