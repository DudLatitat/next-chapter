package com.dudlatitat.nextchapter.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DayPlanTest {
    private val d = 20_500L
    private fun due(n: Int, overdueBy: Int = 0) = (0 until n).map { Card("daily:c$it", "字$it", "zì", "w", "daily", firstSeen = 1, due = d - overdueBy - it, interval = 3, reps = 2) }

    @Test fun threeItemsAndTheBarFills() {
        val s = AppState(cards = due(30), studyDays = listOf(d - 1), newPerDay = 0)
        val p = DayPlan.today(s, d)
        assertEquals(listOf(DayPlan.Kind.REVIEW, DayPlan.Kind.PLAY, DayPlan.Kind.LIFE), p.items.map { it.kind })
        assertEquals("Review 10 phrases", p.items[0].title)
        assertEquals(0, p.welcomeBack)
        val after = DayPlan.played(s.copy(days = mapOf(d to DayLog(reviews = 10))), "numbers", d)
        val q = DayPlan.today(after, d)
        assertTrue(q.items[0].done)
        assertTrue(q.items[1].done)
        assertEquals(2, q.doneCount)
    }

    @Test fun nothingDueCountsAsDone() {
        val p = DayPlan.today(AppState(newPerDay = 0), d)
        assertTrue(p.items[0].done)
        assertEquals("Reviews: all caught up", p.items[0].title)
    }

    @Test fun gameRotatesToTheOneNotPlayedLongest() {
        val all = DayPlan.games.keys.associateWith { d - 1 }
        val s = AppState(played = all + ("family" to d - 9))
        assertEquals("family", DayPlan.pickGame(s, d))
        // Never-played games come first.
        assertEquals("sprint", DayPlan.pickGame(AppState(played = all - "sprint"), d))
    }

    @Test fun comebackCapsTheSessionAfterTwoMissedDays() {
        val back = AppState(cards = due(60, overdueBy = 5), studyDays = listOf(d - 3), newPerDay = 0)
        assertEquals(2, DayPlan.missed(back, d))
        assertEquals(DayPlan.COMEBACK_CAP, DayPlan.sessionLimit(back, d))
        assertEquals("Welcome back: 15 phrases", DayPlan.today(back, d).items[0].title)
        // One missed day (a rest day) is no comeback.
        val rest = back.copy(studyDays = listOf(d - 2))
        assertEquals(0, DayPlan.missed(rest, d))
        assertEquals(Int.MAX_VALUE, DayPlan.sessionLimit(rest, d))
        // Capped sessions take the most overdue first.
        val first = Srs.queue(back, d).take(DayPlan.sessionLimit(back, d))
        assertEquals(15, first.size)
        assertTrue(first.zipWithNext().all { (a, b) -> a.due <= b.due })
    }

    @Test fun restDayKeepsTheStreak() {
        assertEquals(5, Stats.withRest(listOf(1, 2, 3, 5, 6), 6))
        assertEquals("two missed in a row ends it", 2, Stats.withRest(listOf(1, 2, 5, 6), 6))
        assertEquals("only one rest day a week", 4, Stats.withRest(listOf(1, 3, 5, 6, 7), 7))
        assertEquals("rests a week apart are fine", 9, Stats.withRest(listOf(1, 2, 4, 5, 6, 7, 8, 9, 11), 11))
        assertEquals("today not done yet: still counting", 2, Stats.withRest(listOf(4, 5), 6))
        assertEquals(0, Stats.withRest(emptyList(), 6))
        assertFalse(Stats.withRest(listOf(1), 6) > 0)
    }
}

class BestStreakTest {
    @Test fun bestUsesTheRestDayRuleToo() {
        val days = listOf(1L, 2, 3, 5, 6, 7) // one rest day (4)
        assertEquals("the bug: best used to say 3 while the current streak said 6", 6, Stats.bestWithRest(days))
        assertEquals(Stats.withRest(days, 7), Stats.bestWithRest(days))
        assertEquals(3, Stats.bestWithRest(listOf(1L, 2, 3, 10, 11)))
        assertEquals(0, Stats.bestWithRest(emptyList()))
    }
}
