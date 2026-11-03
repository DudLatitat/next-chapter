package com.dudlatitat.nextchapter.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class LifeTabTest {
    @Test fun aplusCardIdsAreUnique() {
        assertEquals(APlus.core1.size, APlus.core1.map { it.id }.toSet().size)
        assertTrue(APlus.core1.all { it.domain in APlus.domains.keys })
        assertEquals(100, APlus.domains.values.sumOf { it.second }) // exam weights add up
    }

    @Test fun firstDayGivesNewCardsFromEveryDomain() {
        val q = APlus.queue(AppState(aplusPerDay = 10), day = 100)
        assertEquals(10, q.size)
        assertEquals(APlus.domains.keys, q.map { it.domain }.toSet())
    }

    @Test fun dueReviewsComeFirstAndNewCardsRespectTheDailyLimit() {
        val day = 100L
        val a = APlus.core1[0].id; val b = APlus.core1[1].id
        val s = AppState(
            aplusPerDay = 3,
            aplus = listOf(
                Review(a, firstSeen = 90, due = 99, interval = 3, reps = 2),
                Review(b, firstSeen = day, due = day + 1, interval = 1, reps = 1), // introduced today, not due
            ),
        )
        val q = APlus.queue(s, day)
        assertEquals(a, q.first().id)
        assertEquals(1 + 2, q.size) // 1 due + (3 per day − 1 already introduced)
        assertTrue(q.none { it.id == b })
    }

    @Test fun gradingUsesTheSameScheduleAsChinese() {
        val r = APlus.grade(Review("x"), Grade.GOOD, day = 50)
        assertEquals(50L, r.firstSeen); assertEquals(1, r.interval); assertEquals(51L, r.due)
        val again = APlus.grade(r, Grade.AGAIN, day = 51)
        assertEquals(51L, again.due); assertEquals(1, again.lapses)
    }

    @Test fun weakestWaitsUntilYouStartThenWeighsTheExam() {
        assertNull(APlus.weakest(AppState()))
        // Everything known except troubleshooting (domain 5, the biggest share).
        val known = APlus.core1.filter { it.domain != 5 }.map { Review(it.id, firstSeen = 1, due = 99, interval = 10, reps = 3) }
        assertEquals(5, APlus.weakest(AppState(aplus = known)))
    }

    @Test fun jobDeadlines() {
        val today = LocalDate.of(2026, 11, 3)
        val s = AppState(jobs = listOf(
            JobPosting("1", "Help desk", deadline = "2026-11-06"),
            JobPosting("2", "Tech II", deadline = "2026-11-04"),
            JobPosting("3", "Applied one", deadline = "2026-11-04", status = "applied"),
            JobPosting("4", "Far off", deadline = "2026-12-30"),
            JobPosting("5", "Gone", deadline = "2026-11-01"),
        ))
        assertEquals(listOf("2", "1"), Career.deadlinesSoon(s, today).map { it.first.id })
        assertEquals(listOf("2", "1"), Career.remindToday(s, today).map { it.id }) // 1 day and 3 days left
    }

    @Test fun certStatusCycles() {
        assertEquals("studying", Career.nextStatus("planned"))
        assertEquals("planned", Career.nextStatus("passed"))
    }

    @Test fun birthdaysRollToNextYearAndLeapDay() {
        val today = LocalDate.of(2026, 11, 3)
        assertEquals(LocalDate.of(2026, 11, 3), People.next(PersonDate("bday", 11, 3), today))
        assertEquals(LocalDate.of(2027, 1, 5), People.next(PersonDate("bday", 1, 5), today))
        assertEquals(LocalDate.of(2027, 2, 28), People.next(PersonDate("bday", 2, 29), today)) // 2027 isn't a leap year
        val s = AppState(people = listOf(Person("p", "Her mom", listOf(PersonDate("Birthday", 11, 20), PersonDate("Other", 6, 1)))))
        val up = People.upcoming(s, today)
        assertEquals(1, up.size); assertEquals(17L, up[0].days)
    }

    private val moveTasks = listOf(
        MoveTask("School & certs:Finish the AS in CIS at SAC", "Finish the AS", "School & certs"),
        MoveTask("School & certs:Pass CompTIA A+", "Pass CompTIA A+", "School & certs"),
        MoveTask("School & certs:Pass CompTIA Network+", "Pass CompTIA Network+", "School & certs"),
        MoveTask("Documents:Passport", "Passport", "Documents", done = true),
    )

    @Test fun lifeTicksMovesLinkedSteps() {
        var s = AppState(tasks = moveTasks)
        // A+ needs both cores passed.
        s = Career.sync(s.copy(certs = Career.defaultCerts.map { if (it.id == "aplus1") it.copy(status = "passed") else it }))
        assertTrue(s.tasks.none { it.id.endsWith("A+") && it.done })
        s = Career.sync(s.copy(certs = Career.certs(s).map { if (it.id == "aplus2") it.copy(status = "passed") else it }))
        assertTrue(s.tasks.first { it.id.endsWith("A+") }.done)
        // The degree ticks itself only once classes are listed and all done.
        assertTrue(!s.tasks.first { it.id.contains("AS in CIS") }.done)
        s = Career.sync(s.copy(classes = listOf(SchoolClass("c1", "CIS 101", done = true), SchoolClass("c2", "CIS 102"))))
        assertTrue(!s.tasks.first { it.id.contains("AS in CIS") }.done)
        s = Career.sync(s.copy(classes = s.classes.map { it.copy(done = true) }))
        assertTrue(s.tasks.first { it.id.contains("AS in CIS") }.done)
        // Unlinked steps are left alone.
        assertTrue(s.tasks.first { it.id == "Documents:Passport" }.done)
    }

    @Test fun tickingInMoveUpdatesLife() {
        val s = AppState(tasks = moveTasks)
        val after = Career.tickFromMove(s, "School & certs:Pass CompTIA Network+", done = true)!!
        assertEquals("passed", Career.certs(after).first { it.id == "netplus" }.status)
        assertTrue(after.tasks.first { it.id.endsWith("Network+") }.done)
        val undone = Career.tickFromMove(after, "School & certs:Pass CompTIA Network+", done = false)!!
        assertEquals("studying", Career.certs(undone).first { it.id == "netplus" }.status)
        assertTrue(!undone.tasks.first { it.id.endsWith("Network+") }.done)
        // Not linked → Move handles it; degree with no classes listed → Move handles it too.
        assertNull(Career.tickFromMove(s, "Documents:Passport", true))
        assertNull(Career.tickFromMove(s, "School & certs:Finish the AS in CIS at SAC", true))
    }

    @Test fun aDayWithReviewsCountsEvenIfTheSessionWasntFinished() {
        val s = AppState(studyDays = listOf(10, 11), days = mapOf(12L to DayLog(reviews = 4), 13L to DayLog(reviews = 0)))
        assertEquals(listOf(10L, 11L, 12L), Stats.practiceDays(s))
    }

    @Test fun toneTrendWindowsAndKindWords() {
        val ev = listOf(ToneEvent(10, 4, 1), ToneEvent(10, 4, 4), ToneEvent(11, 4, 4), ToneEvent(20, 4, 4))
        val s = AppState(toneEvents = ev)
        assertEquals(2 / 3f, Stats.toneAccuracyBetween(s, 10, 11, min = 3)[4]!!, 1e-6f)
        assertNull(Stats.toneAccuracyBetween(s, 10, 11, min = 4)[4]) // too few to say
        assertEquals("needs practice", Stats.toneWord(0.07f))
        assertEquals("getting there", Stats.toneWord(0.5f))
        assertEquals("solid ✓", Stats.toneWord(0.9f))
        assertEquals("not enough yet", Stats.toneWord(null))
    }

    @Test fun streakCountsAnyDayYouPracticed() {
        // Finished sessions Fri–Sun (days 10–12), then a few reviews Mon and Tue without finishing.
        val s = AppState(studyDays = listOf(10, 11, 12), days = mapOf(13L to DayLog(reviews = 1), 14L to DayLog(reviews = 2)))
        assertEquals(5, Stats.streak(s, day = 14))
        assertEquals(0, Stats.streak(s, day = 16)) // missed day 15
    }
}
