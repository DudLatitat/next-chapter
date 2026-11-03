package com.dudlatitat.nextchapter.data

import kotlinx.serialization.Serializable
import java.time.LocalDate
import kotlin.random.Random

/** One stretch of study time. */
@Serializable
data class StudySession(val day: Long, val subject: String, val minutes: Int)

/** One A+ practice test: how it went, and which cards you missed. */
@Serializable
data class TestResult(val day: Long, val score: Int, val total: Int, val seconds: Int, val missed: List<String> = emptyList())

/** The study timer and log (A+, school, Chinese…). */
object Study {
    val subjects = listOf("A+", "School", "Chinese", "Other")

    /** Minutes between two clock times; a timer left running is capped at 4 hours. */
    fun minutes(startMs: Long, endMs: Long): Int = ((endMs - startMs) / 60_000).toInt().coerceIn(0, 240)

    /** Adds a session (merging with the same day and subject), keeping the last 400 days. */
    fun add(log: List<StudySession>, s: StudySession): List<StudySession> {
        if (s.minutes <= 0) return log
        val same = log.firstOrNull { it.day == s.day && it.subject == s.subject }
        val merged = if (same == null) log + s else log.map { if (it === same) it.copy(minutes = it.minutes + s.minutes) else it }
        return merged.filter { it.day > s.day - 400 }
    }

    fun dayTotal(state: AppState, day: Long = today()): Int = state.study.filter { it.day == day }.sumOf { it.minutes }

    /** Minutes per subject in the week starting on Monday [start]. */
    fun week(state: AppState, start: LocalDate): Map<String, Int> {
        val from = start.toEpochDay(); val to = from + 6
        return state.study.filter { it.day in from..to }.groupBy { it.subject }.mapValues { (_, v) -> v.sumOf { it.minutes } }
    }

    /** "2 h 5 min", "45 min". */
    fun label(minutes: Int): String = if (minutes >= 60) "${minutes / 60} h" + (if (minutes % 60 > 0) " ${minutes % 60} min" else "") else "$minutes min"
}

/** A+ practice tests: mixed multiple-choice questions, weighted like the real exam. */
object PracticeTest {
    data class Question(val card: APlus.Q, val options: List<String>, val correct: Int)

    /** The real exam's pass mark (675 on a 100–900 scale), roughly 75%. */
    const val PASS = 0.75f

    /** How many questions each domain gets, in proportion to its share of the exam (largest remainder). */
    fun perDomain(n: Int): Map<Int, Int> {
        val shares = APlus.domains.mapValues { it.value.second * n / 100.0 }
        val base = shares.mapValues { it.value.toInt() }.toMutableMap()
        var left = n - base.values.sum()
        shares.entries.sortedByDescending { it.value - it.value.toInt() }.forEach { if (left > 0) { base[it.key] = base.getValue(it.key) + 1; left-- } }
        return base
    }

    /**
     * Builds a test. Wrong options are other answers from the same domain, so they sound
     * plausible (a port among ports, a RAID level among RAID levels).
     */
    fun build(n: Int = 20, random: Random = Random.Default): List<Question> =
        perDomain(n).flatMap { (d, count) ->
            val pool = APlus.core1.filter { it.domain == d }
            pool.shuffled(random).take(count).map { card ->
                val wrong = pool.filter { it.id != card.id && it.a != card.a }.shuffled(random).take(3).map { it.a }
                val options = (wrong + card.a).shuffled(random)
                Question(card, options, options.indexOf(card.a))
            }
        }.shuffled(random)
}
