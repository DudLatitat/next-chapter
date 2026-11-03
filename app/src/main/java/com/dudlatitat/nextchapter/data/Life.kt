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

    /** Adds [day] to a list of days once, keeping the last 400. */
    fun addDay(days: List<Long>, day: Long): List<Long> = if (day in days) days else (days + day).filter { it > day - 400 }
}
