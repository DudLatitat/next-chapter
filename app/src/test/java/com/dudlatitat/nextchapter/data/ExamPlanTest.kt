package com.dudlatitat.nextchapter.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExamPlanTest {
    private val d = java.time.LocalDate.of(2026, 11, 25).toEpochDay()
    private fun exam(daysAhead: Long) = java.time.LocalDate.ofEpochDay(d + daysAhead).toString()

    @Test fun newCardsADayToSeeEverythingAWeekEarly() {
        // 140 unseen, exam in 37 days → 30 study days → 5 a day.
        val p = ExamPlan.of(AppState(aplusExam = exam(37), aplusPerDay = 10), d, total = 140)!!
        assertEquals(37, p.daysLeft)
        assertEquals(5, p.newPerDay)
        assertTrue(p.onTrack)
        assertTrue(p.line.startsWith("On track"))
        val tight = ExamPlan.of(AppState(aplusExam = exam(37), aplusPerDay = 3), d, total = 140)!!
        assertFalse(tight.onTrack)
        assertEquals("5 new a day sees every card a week early", tight.line)
    }

    @Test fun edges() {
        assertNull("no exam date", ExamPlan.of(AppState(), d, total = 140))
        assertNull("exam passed", ExamPlan.of(AppState(aplusExam = exam(-1)), d, total = 140))
        // Inside the last week: everything left needs seeing now, capped at 30 a day.
        assertEquals(30, ExamPlan.of(AppState(aplusExam = exam(3)), d, total = 140)!!.newPerDay)
        val allSeen = AppState(aplusExam = exam(20), aplus = (1..140).map { Review("q$it", firstSeen = d - 5, interval = 8) })
        val p = ExamPlan.of(allSeen, d, total = 140)!!
        assertEquals(0, p.unseen); assertEquals(100, p.readyPct)
        assertTrue(p.line.startsWith("Every card seen"))
    }
}
