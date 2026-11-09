package com.dudlatitat.nextchapter.data

import java.time.LocalDate
import java.time.LocalTime

/** Tonight's one goal, picked in the morning or evening; [done] is null until answered. */
@kotlinx.serialization.Serializable
data class DayGoal(val day: Long, val text: String, val done: Boolean? = null)

/** The daily rhythm around Rudy's day: morning card, evening plan, call time, Sunday review. */
object Life {
    enum class Part { MORNING, DAY, PLAN, CALL, LATE }

    /** Morning until noon; plan the evening 5–7 pm; 7–8 pm is call time (nothing else); after 8, check in. */
    fun partOf(t: LocalTime): Part = when {
        t.hour < 12 -> Part.MORNING
        t.hour < 17 -> Part.DAY
        t.hour < 19 -> Part.PLAN
        t.hour < 20 -> Part.CALL
        else -> Part.LATE
    }

    /** Call time with Yuling: notifications arriving now stay silent. */
    fun isCallTime(t: LocalTime): Boolean = partOf(t) == Part.CALL

    /** Today's label for her clock: "name · city" from Settings, or a plain fallback when unset. */
    fun herLabel(name: String, city: String): String =
        listOf(name.trim(), city.trim()).filter { it.isNotEmpty() }.joinToString(" · ").ifEmpty { "Her time" }

    /** Where the move goes, for the Move tab: her city from Settings, or just Sichuan. */
    fun moveTo(city: String): String = city.trim().ifEmpty { "Sichuan" }

    /** Opening the app between 3 and 7 am counts as an early start. */
    fun earlyStart(t: LocalTime): Boolean = t.hour >= 3 && t.isBefore(LocalTime.of(7, 0))

    val goalIdeas = listOf("Chinese practice", "A+ study", "A project", "Rest")

    fun goalFor(state: AppState, day: Long = today()): DayGoal? = state.goals.lastOrNull { it.day == day }

    /** Sets (or replaces) a day's goal, keeping the last 120 days. */
    fun withGoal(goals: List<DayGoal>, goal: DayGoal): List<DayGoal> =
        (goals.filterNot { it.day == goal.day } + goal).filter { it.day > goal.day - 120 }.sortedBy { it.day }

    data class Week(val prayed: Int, val earlyStarts: Int, val goalsSet: Int, val goalsDone: Int)

    /** The Sunday review's numbers for the week starting on Monday [start]. */
    fun week(state: AppState, start: LocalDate): Week {
        val days = (0L until 7L).map { start.toEpochDay() + it }.toSet()
        val goals = state.goals.filter { it.day in days }
        return Week(
            prayed = state.prayedDays.count { it in days },
            earlyStarts = state.wakeDays.count { it in days },
            goalsSet = goals.size,
            goalsDone = goals.count { it.done == true },
        )
    }

    /** Her zone: Sichuan (all of China is on one time, with no daylight saving). */
    val HER_ZONE: java.time.ZoneId = java.time.ZoneId.of("Asia/Shanghai")

    /** The same moment in Sichuan. */
    fun herTime(now: java.time.ZonedDateTime): java.time.ZonedDateTime = now.withZoneSameInstant(HER_ZONE)

    /** "today", "tomorrow" or "yesterday": her calendar day compared with yours. */
    fun herDayWord(now: java.time.ZonedDateTime): String {
        val diff = herTime(now).toLocalDate().toEpochDay() - now.toLocalDate().toEpochDay()
        return when { diff > 0 -> "tomorrow"; diff < 0 -> "yesterday"; else -> "today" }
    }

    /** Hours she's ahead of you right now (15 or 16 from Los Angeles, as daylight saving changes). */
    fun hoursAhead(now: java.time.ZonedDateTime): Int =
        (HER_ZONE.rules.getOffset(now.toInstant()).totalSeconds - now.offset.totalSeconds) / 3600

    /** Adds [day] to a list of days once, keeping the last 400. */
    fun addDay(days: List<Long>, day: Long): List<Long> = if (day in days) days else (days + day).filter { it > day - 400 }
}
