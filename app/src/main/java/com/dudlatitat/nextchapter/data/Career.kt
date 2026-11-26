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

    // ---- Move checklist steps that mirror the Life tab ----

    /** Seed task ids in Move → what ticks them in Life. */
    val linkedTasks = mapOf(
        Seed.AS_ID to "classes",
        "School & certs:Pass CompTIA A+" to "aplus",
        "School & certs:Pass CompTIA Network+" to "netplus",
        "School & certs:Pass CompTIA Security+" to "secplus",
    )

    private fun passed(state: AppState, id: String) = certs(state).firstOrNull { it.id == id }?.status == "passed"

    /** Whether a linked step is done according to Life; null when Life doesn't know (no classes entered). */
    fun linkedDone(state: AppState, link: String): Boolean? = when (link) {
        "classes" -> if (state.classes.isEmpty()) null else state.classes.all { it.done }
        "aplus" -> passed(state, "aplus1") && passed(state, "aplus2")
        else -> passed(state, link)
    }

    /** Ticks or unticks Move's linked steps to match Life. Run after any change to certs or classes. */
    fun sync(state: AppState): AppState = state.copy(tasks = state.tasks.map { t ->
        val done = linkedTasks[t.id]?.let { linkedDone(state, it) }
        if (done == null || done == t.done) t else t.copy(done = done)
    })

    /**
     * Ticking a linked step in Move updates Life instead: a cert becomes "passed" (or back to
     * "studying" when unticked). Returns null for steps Move handles itself; returns the state
     * unchanged for the degree when classes are listed (it ticks itself when they're all done).
     */
    fun tickFromMove(state: AppState, taskId: String, done: Boolean): AppState? {
        val link = linkedTasks[taskId] ?: return null
        val ids = when (link) { "classes" -> return if (state.classes.isEmpty()) null else state; "aplus" -> setOf("aplus1", "aplus2"); else -> setOf(link) }
        val certs = certs(state).map { if (it.id in ids) it.copy(status = if (done) "passed" else "studying") else it }
        return sync(state.copy(certs = certs))
    }

    // ---- Status that follows what you do ----

    private fun rank(st: String) = statuses.indexOf(st).coerceAtLeast(0)

    /** A cert's exam date (A+ Core 1 also reads the older A+ exam date). */
    fun dateOf(s: AppState, c: CertStep): String? = c.date ?: if (c.id == "aplus1") s.aplusExam else null

    /** A+ cards of a core you've started ("c1:" / "c2:"). */
    private fun studied(s: AppState, prefix: String) = s.aplus.any { it.id.startsWith(prefix) && it.firstSeen != null }

    /**
     * What the tile shows: "passed" only when you said so; otherwise the further of what you set and
     * what the app sees (an exam date means booked; A+ cards reviewed means studying).
     */
    fun effective(s: AppState, c: CertStep): String {
        if (c.status == "passed") return "passed"
        val seen = when {
            dateOf(s, c) != null -> "booked"
            c.id == "aplus1" && studied(s, "c1:") -> "studying"
            c.id == "aplus2" && studied(s, "c2:") -> "studying"
            else -> "planned"
        }
        return statuses[maxOf(rank(c.status), rank(seen))]
    }

    fun status(s: AppState, id: String): String = certs(s).firstOrNull { it.id == id }?.let { effective(s, it) } ?: "planned"

    private fun edit(s: AppState, id: String, f: (CertStep) -> CertStep): AppState =
        sync(s.copy(certs = certs(s).map { if (it.id == id) f(it) else it }))

    /** Sets (or clears) a cert's exam date. Core 1's is also kept in the older A+ date so nothing reads a stale one. */
    fun withDate(s: AppState, id: String, iso: String?): AppState =
        edit(s, id) { it.copy(date = iso) }.let { if (id == "aplus1") it.copy(aplusExam = iso) else it }

    /** "I passed 🎉" (or undo: back to studying). Passing Core 1 moves A+ study to Core 2. */
    fun withPassed(s: AppState, id: String, passed: Boolean): AppState =
        edit(s, id) { it.copy(status = if (passed) "passed" else "studying") }

    /** For certs without cards (Network+, Security+): "I'm studying this" on or off. */
    fun withStudying(s: AppState, id: String, on: Boolean): AppState =
        edit(s, id) { it.copy(status = if (on) "studying" else "planned") }

    /** The first cert not passed yet: what Career path calls "next". */
    fun next(s: AppState): CertStep? = certs(s).firstOrNull { effective(s, it) != "passed" }

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
