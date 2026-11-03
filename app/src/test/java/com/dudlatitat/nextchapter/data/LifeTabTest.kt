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
}
