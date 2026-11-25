package com.dudlatitat.nextchapter.data

import java.time.DayOfWeek
import java.time.LocalDate

/** One week of practice, Monday to Sunday. */
data class WeekStats(
    val start: LocalDate,
    val daysPracticed: Int,
    val reviews: Int,
    val minutes: Int,
    val newPhrases: Int,
    /** Share of tone attempts that were right, or null with too few attempts. */
    val toneAccuracy: Float?,
)

object Stats {
    fun weekStart(day: LocalDate = LocalDate.now()): LocalDate = day.with(DayOfWeek.MONDAY)

    /** Days you practiced: finished a session, or answered at least one card. */
    /**
     * Marks [day] as practised. Finishing a review session does this, and so does finishing any game
     * round (Callouts, Talk, Texts, Numbers, Sprint, a Prayer rung): playing counts for the streak.
     */
    fun practiced(s: AppState, day: Long = today()): AppState =
        if (day in s.studyDays) s else s.copy(studyDays = (s.studyDays + day).takeLast(800))

    fun practiceDays(state: AppState): List<Long> =
        (state.studyDays + state.days.filterValues { it.reviews > 0 }.keys).distinct().sorted()

    /**
     * Days in a row you practiced (ending today or yesterday). Any day you answered a card
     * counts, the same as the 12-week grid, not only days you finished a whole session.
     */
    fun streak(state: AppState, day: Long = today()): Int = withRest(practiceDays(state), day)

    /**
     * Days in a row, where one missed day a week is a free rest day: it doesn't break the streak
     * (and doesn't count in it). Two missed days in a row, or two rest days within 7 days, end it.
     */
    /**
     * Your best streak ever, by the same rule as [withRest] (a rest day a week doesn't break it),
     * so "best" can never be lower than the streak you're on.
     */
    fun bestWithRest(days: List<Long>): Int {
        val sorted = days.distinct().sorted()
        return sorted.maxOfOrNull { withRest(sorted, it) } ?: 0
    }

    fun withRest(days: List<Long>, day: Long = today()): Int {
        val set = days.toSet()
        var d = if (day in set) day else day - 1
        var n = 0
        var lastRest: Long? = null
        while (true) {
            if (d in set) { n++; d--; continue }
            val restOk = (lastRest == null || lastRest - d >= 7) && (d - 1) in set
            if (restOk) { lastRest = d; d--; continue }
            return n
        }
    }

    fun week(state: AppState, start: LocalDate): WeekStats {
        val from = start.toEpochDay()
        val to = from + 6
        val logs = state.days.filterKeys { it in from..to }
        val tones = state.toneEvents.filter { it.day in from..to }
        return WeekStats(
            start = start,
            daysPracticed = (from..to).count { it in state.studyDays || (logs[it]?.reviews ?: 0) > 0 },
            reviews = logs.values.sumOf { it.reviews },
            minutes = logs.values.sumOf { it.seconds } / 60,
            newPhrases = state.cards.count { (it.firstSeen ?: -1) in from..to },
            toneAccuracy = if (tones.size >= 5) tones.count { it.target == it.got }.toFloat() / tones.size else null,
        )
    }

    /** Accuracy per tone (1–4) over the last [days] days; null where there's too little data. */
    fun toneAccuracy(state: AppState, days: Int = 30): Map<Int, Float?> {
        val since = today() - days
        val recent = state.toneEvents.filter { it.day >= since }
        return (1..4).associateWith { t ->
            val mine = recent.filter { it.target == t }
            if (mine.size < 4) null else mine.count { it.got == t }.toFloat() / mine.size
        }
    }

    /** Accuracy per tone between two days (inclusive); null with fewer than [min] attempts. */
    fun toneAccuracyBetween(state: AppState, from: Long, to: Long, min: Int = 4): Map<Int, Float?> {
        val events = state.toneEvents.filter { it.day in from..to }
        return (1..4).associateWith { t ->
            val mine = events.filter { it.target == t }
            if (mine.size < min) null else mine.count { it.got == t }.toFloat() / mine.size
        }
    }

    /** A kind word for a tone's accuracy instead of a bare percentage. */
    fun toneWord(a: Float?): String = when {
        a == null -> "not enough yet"
        a < 0.4f -> "needs practice"
        a < 0.7f -> "getting there"
        else -> "solid ✓"
    }

    /** The two tones you mix up most (either way round), or null if nothing stands out yet. */
    fun weakestPair(state: AppState, days: Int = 30): Pair<Int, Int>? {
        val since = today() - days
        val misses = state.toneEvents.filter { it.day >= since && it.target != it.got }
        if (misses.size < 3) return null
        return misses.groupingBy { minOf(it.target, it.got) to maxOf(it.target, it.got) }.eachCount().maxByOrNull { it.value }?.key
    }
}
