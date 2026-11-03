package com.dudlatitat.nextchapter.data

import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.MonthDay

/** One certification on the path, and how far along it is. */
@Serializable
data class CertStep(val id: String, val name: String, val status: String = "planned", val date: String? = null)

/** A class still in the way of the degree (or done). */
@Serializable
data class SchoolClass(val id: String, val name: String, val term: String = "", val done: Boolean = false)

/** A job posting you're watching. Status: saved → applied → interview → offer, or closed. */
@Serializable
data class JobPosting(
    val id: String,
    val title: String,
    val org: String = "",
    val url: String = "",
    /** Application deadline, ISO date. */
    val deadline: String? = null,
    val status: String = "saved",
    val notes: String = "",
)

object Career {
    val statuses = listOf("planned", "studying", "booked", "passed")
    val jobStatuses = listOf("saved", "applied", "interview", "offer", "closed")

    val defaultCerts = listOf(
        CertStep("aplus1", "A+ Core 1 (220-1201)"),
        CertStep("aplus2", "A+ Core 2 (220-1202)"),
        CertStep("netplus", "Network+"),
        CertStep("secplus", "Security+"),
    )

    fun certs(state: AppState): List<CertStep> = state.certs.ifEmpty { defaultCerts }

    /** The next status in the cycle (tapping a cert steps it along). */
    fun nextStatus(s: String): String = statuses[(statuses.indexOf(s) + 1).mod(statuses.size)]

    /** Days until a posting's deadline, or null without one. */
    fun daysLeft(job: JobPosting, today: LocalDate): Long? =
        parseDate(job.deadline)?.let { it.toEpochDay() - today.toEpochDay() }

    /** Saved postings (not applied yet) whose deadline is within [within] days, soonest first. */
    fun deadlinesSoon(state: AppState, today: LocalDate, within: Int = 7): List<Pair<JobPosting, Long>> =
        state.jobs.filter { it.status == "saved" }
            .mapNotNull { j -> daysLeft(j, today)?.takeIf { it in 0..within }?.let { j to it } }
            .sortedBy { it.second }

    /** Postings to remind about today: 3 days and 1 day before the deadline. */
    fun remindToday(state: AppState, today: LocalDate): List<JobPosting> =
        deadlinesSoon(state, today, 3).filter { it.second == 3L || it.second == 1L }.map { it.first }
}

/** Someone close (Yuling, her mom…), with dates worth remembering. */
@Serializable
data class Person(val id: String, val name: String, val dates: List<PersonDate> = emptyList())

/** A yearly date: birthday, anniversary… (month 1–12, day 1–31). */
@Serializable
data class PersonDate(val label: String, val month: Int, val day: Int)

/** Something to remember about a person: a gift idea, something they like, a size, a moment. */
@Serializable
data class PersonNote(val id: String, val personId: String, val text: String, val kind: String = "idea", val added: Long = 0)

object People {
    val starters = listOf("Yuling", "Her mom", "Her dad")
    val kinds = listOf("idea" to "🎁 Gift idea", "likes" to "💛 Likes", "size" to "📏 Size", "moment" to "✨ Moment")

    /** The next time a yearly date comes round, today included. 29 Feb falls on 28 Feb in other years. */
    fun next(d: PersonDate, today: LocalDate): LocalDate {
        val md = MonthDay.of(d.month, d.day)
        val thisYear = md.atYear(today.year)
        return if (thisYear.isBefore(today)) md.atYear(today.year + 1) else thisYear
    }

    data class Upcoming(val person: Person, val date: PersonDate, val on: LocalDate, val days: Long)

    /** Dates within [within] days, soonest first. */
    fun upcoming(state: AppState, today: LocalDate, within: Int = 21): List<Upcoming> =
        state.people.flatMap { p ->
            p.dates.mapNotNull { d ->
                try {
                    val on = next(d, today)
                    val days = on.toEpochDay() - today.toEpochDay()
                    if (days <= within) Upcoming(p, d, on, days) else null
                } catch (_: Exception) { null }
            }
        }.sortedBy { it.days }

    fun notesFor(state: AppState, personId: String): List<PersonNote> = state.notes.filter { it.personId == personId }.sortedByDescending { it.added }
}
