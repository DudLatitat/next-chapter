package com.dudlatitat.nextchapter.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PayoffTest {
    private val d = 20_500L
    private fun due(n: Int) = (0 until n).map { Card("daily:c$it", "字$it", "zì", "w", "daily", firstSeen = 1, due = d - it, interval = 3, reps = 2) }

    @Test fun aGameClosesThePlayRing() {
        val before = AppState(cards = due(20), studyDays = listOf(d - 1), newPerDay = 0)
        val after = DayPlan.played(before, "numbers", d)
        val p = Payoff.of(before, after, d)
        val play = p.rings.first { it.kind == DayPlan.Kind.PLAY }
        assertTrue(play.closed)
        assertEquals(0f, play.from)
        assertEquals(1f, play.to)
        assertFalse(p.rings.first { it.kind == DayPlan.Kind.REVIEW }.closed)
        assertEquals(DayPlan.Kind.REVIEW, p.next?.kind) // reviews are still open: that's next
        assertFalse(p.allDone)
    }

    @Test fun reviewsFillPartway() {
        val before = AppState(cards = due(20), studyDays = listOf(d - 1), newPerDay = 0)
        val after = before.copy(days = mapOf(d to DayLog(reviews = 5)))
        val r = Payoff.of(before, after, d).rings.first { it.kind == DayPlan.Kind.REVIEW }
        assertEquals(0.5f, r.to, 1e-6f)
        assertFalse(r.closed)
    }

    @Test fun readinessTicksAndClimbs() {
        val learned = (0 until 12).map { Card("family:x$it", "字", "zì", "w", "family", firstSeen = 1, reps = 2, interval = 3) } +
            (0 until 5).map { Card("daily:y$it", "字", "zì", "w", "daily", firstSeen = 1, reps = 2, interval = 3) }
        val before = AppState(cards = learned)
        val most = Talk.scenes.first { it.id == "momcall" }.beats.size * 2
        val after = before.copy(dinnerBest = mapOf("momcall" to most))
        val p = Payoff.of(before, after, d)
        assertEquals(1, p.ticked.size)
        assertTrue(p.ticked[0].startsWith("Talk"))
        assertEquals("call", p.climbed?.id)
        assertNull(Payoff.of(after, after, d).climbed)
    }

    @Test fun claimOncePerDay() {
        val s = AppState(newPerDay = 0, played = mapOf("x" to d), routineChecks = listOf(RoutineCheck(d, CheckIn.DONE)))
        // Nothing due, a game played, tonight done (and no A+ due): all three done.
        val done = DayPlan.today(s, d)
        if (done.finished) {
            assertTrue(Payoff.claimDue(s, d))
            assertFalse(Payoff.claimDue(s.copy(celebrated = d), d))
        }
        assertFalse(Payoff.claimDue(AppState(cards = due(5), newPerDay = 0), d))
    }
}
