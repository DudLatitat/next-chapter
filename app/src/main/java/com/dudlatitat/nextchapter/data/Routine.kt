package com.dudlatitat.nextchapter.data

import kotlinx.serialization.Serializable
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * One evening's assigned job in the weekly routine: what to do, the tiny first step that gets you
 * off the couch, and the bad-day minimum that still counts. [dow] is 1 = Monday … 7 = Sunday;
 * [minutes] is when it starts (minutes after midnight). [kind] decides what Start opens:
 * "aplus" (practice test), "aplus-review" (missed A+ cards), "chinese" (save a sentence) or "" (timer).
 * Texts may say {her}, filled in with her name from Settings.
 */
@Serializable
data class RoutineDay(
    val dow: Int,
    val task: String,
    val firstStep: String,
    val minimum: String,
    val minutes: Int = 18 * 60,
    val kind: String = "",
)

@Serializable
enum class CheckIn { DONE, MINIMUM, SKIPPED }

@Serializable
data class RoutineCheck(val day: Long, val status: CheckIn)

/** The Sunday review's two questions for the week starting on that Monday. */
@Serializable
data class RoutineReview(val worked: String = "", val change: String = "")

/** The weekly routine: one job per evening, so there's nothing to decide when you get home. */
object Routine {
    /** Seeded schedule (editable in Life → Weekly routine). Personal details are typed on the phone, not here. */
    val seed = listOf(
        RoutineDay(1, "A+ practice questions", "Open the A+ practice test", "10 questions", kind = "aplus"),
        RoutineDay(2, "20-min walk + Chinese with {her}", "Shoes on, step outside", "Learn 1 sentence from {her}", kind = "chinese"),
        RoutineDay(3, "A+ practice questions", "Open the A+ practice test", "10 questions", kind = "aplus"),
        RoutineDay(4, "20-min walk + Chinese with {her}", "Shoes on, step outside", "Learn 1 sentence from {her}", kind = "chinese"),
        RoutineDay(5, "A+ review of missed questions", "Open your missed A+ cards", "Review 5 missed questions", kind = "aplus-review"),
        RoutineDay(6, "Walk or gym (morning) + 1 hour on your side project", "Lay out what you need for it", "One small step on it", minutes = 10 * 60),
        RoutineDay(7, "Rest · 15-min weekly plan + lunch prep", "Open this week's review", "Plan next week only", minutes = 17 * 60),
    )

    /** The fixed points of the day, shown with the schedule. Times follow Settings. */
    fun anchors(state: AppState): List<Pair<Int, String>> = listOf(
        state.wakeMinutes to "Wake, water, no phone for the first 10 minutes",
        state.homeMinutes to "Home → change → start today's job (no couch first)",
        20 * 60 + 30 to "Free time, guilt-free",
        state.windDownMinutes to "Phone down, wind down",
    )

    val rules = listOf(
        "Bad day? The minimum still counts.",
        "Missed a day? Don't make it up. Just do the next day's job.",
        "Run it for 2 weeks before changing anything.",
    )

    /** Today's job for a date (the schedule is always 7 days; a missing day falls back to the seed). */
    fun jobFor(schedule: List<RoutineDay>, date: LocalDate): RoutineDay {
        val dow = date.dayOfWeek.value
        return schedule.firstOrNull { it.dow == dow } ?: seed.first { it.dow == dow }
    }

    /** Fills {her} with her name, or "her" when it isn't set. */
    fun text(s: String, herName: String): String = s.replace("{her}", herName.trim().ifEmpty { "her" })

    fun checkFor(checks: List<RoutineCheck>, day: Long): CheckIn? = checks.lastOrNull { it.day == day }?.status

    /** Sets (or changes) a day's check-in, keeping the last 400 days. */
    fun withCheck(checks: List<RoutineCheck>, check: RoutineCheck): List<RoutineCheck> =
        (checks.filterNot { it.day == check.day } + check).filter { it.day > check.day - 400 }.sortedBy { it.day }

    fun isWin(status: CheckIn?) = status == CheckIn.DONE || status == CheckIn.MINIMUM

    /** Days in a row with a win (Done or Minimum), ending today or, if today isn't checked yet, yesterday. */
    fun streak(checks: List<RoutineCheck>, day: Long): Int =
        if (checkFor(checks, day) == CheckIn.SKIPPED) 0
        else Srs.streak(checks.filter { isWin(it.status) }.map { it.day }, day)

    /** Monday-to-Sunday check-ins for the week containing [date] (null = not checked in). */
    fun week(checks: List<RoutineCheck>, date: LocalDate): List<CheckIn?> {
        val monday = date.with(DayOfWeek.MONDAY).toEpochDay()
        return (0L until 7L).map { checkFor(checks, monday + it) }
    }

    /** True during the first two weeks after the first check-in: the "run it before changing" stretch. */
    fun inTrial(checks: List<RoutineCheck>, day: Long): Boolean {
        val first = checks.minOfOrNull { it.day } ?: return true
        return day - first < 14
    }

    /** Replaces one day of the schedule, keeping it sorted Monday → Sunday. */
    fun withDay(schedule: List<RoutineDay>, day: RoutineDay): List<RoutineDay> =
        (schedule.filterNot { it.dow == day.dow } + day).sortedBy { it.dow }

    /** A kind word for each check-in. Never "failed". */
    fun cheer(status: CheckIn): String = when (status) {
        CheckIn.DONE -> "Done ✓ 很好! That's the whole job."
        CheckIn.MINIMUM -> "The minimum counts. You showed up. 加油!"
        CheckIn.SKIPPED -> "Rest is okay. Tomorrow's job is all that matters. 明天见."
    }
}
