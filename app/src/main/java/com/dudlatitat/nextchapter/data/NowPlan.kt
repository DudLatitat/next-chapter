package com.dudlatitat.nextchapter.data

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * What Today's one "Now" card is about, by the time of day (your phone's clock). The call hour
 * (7–8 pm) and after wind-down always win: then the app asks nothing. Pure, tested in NowPlanTest.
 */
object NowPlan {
    enum class Kind { EARLY, MORNING, WORK, HEADING_HOME, DAY, EVENING, CALL, AFTER_DONE, AFTER_OPEN, WIND_DOWN }

    const val CALL_START = 19 * 60
    const val CALL_END = 20 * 60

    /** The plain-numbers version, so every window can be tested. */
    fun kind(
        now: Int, wake: Int, windDown: Int, evening: Int,
        atWork: Boolean = false, headingHome: Boolean = false, jobChecked: Boolean = false,
    ): Kind = when {
        now in CALL_START until CALL_END -> Kind.CALL
        now >= windDown || now < 3 * 60 -> Kind.WIND_DOWN
        now >= CALL_END -> if (jobChecked) Kind.AFTER_DONE else Kind.AFTER_OPEN
        headingHome -> Kind.HEADING_HOME
        atWork -> Kind.WORK
        now < wake + 30 -> Kind.EARLY
        now >= evening - 15 -> if (jobChecked) Kind.AFTER_DONE else Kind.EVENING
        now < 12 * 60 -> Kind.MORNING
        else -> Kind.DAY
    }

    fun of(s: AppState, now: LocalTime = LocalTime.now(), day: Long = today()): Kind {
        val m = now.hour * 60 + now.minute
        val job = Routine.jobOn(s, LocalDate.ofEpochDay(day))
        return kind(
            m, s.wakeMinutes, s.windDownMinutes, minOf(Places.homeTime(s, day), job.minutes),
            atWork = Places.atWork(s, day, m), headingHome = Places.headingHome(s, day, m),
            jobChecked = Routine.checkFor(s.routineChecks, day) != null,
        )
    }

    /** "call in 2h 10m" before it, "call now" during it, "call tmrw 7 PM" after it. */
    fun callLine(now: Int): String = when {
        now < CALL_START -> (CALL_START - now).let { left -> if (left >= 60) "call in ${left / 60}h ${left % 60}m" else "call in ${left}m" }
        now < CALL_END -> "call now 💗"
        else -> "call tmrw 7 PM"
    }

    /** Her local time and what part of her day it is ("afternoon"), for the line under your clock. */
    fun herPart(now: ZonedDateTime, herZone: ZoneId = Life.HER_ZONE): String {
        val h = now.withZoneSameInstant(herZone).hour
        return when (h) { in 5..10 -> "morning"; in 11..13 -> "midday"; in 14..17 -> "afternoon"; in 18..21 -> "evening"; else -> "night" }
    }
}
