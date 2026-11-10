package com.dudlatitat.nextchapter.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SrsTest {
    private fun card(id: String, deck: String = "daily") = Card(id = id, hanzi = id, pinyin = "", english = "", deck = deck)
    private val day = 20_000L

    @Test fun goodAnswersStretchTheGap() {
        var c = Srs.grade(card("a"), Grade.GOOD, day)
        assertEquals(1, c.interval); assertEquals(day + 1, c.due); assertEquals(day, c.firstSeen)
        c = Srs.grade(c, Grade.GOOD, day + 1)
        assertEquals(3, c.interval)
        c = Srs.grade(c, Grade.GOOD, day + 4)
        assertEquals(8, c.interval) // 3 × 2.5 = 7.5 → 8
        assertTrue(Srs.grade(c, Grade.EASY, day + 12).interval > Srs.grade(c, Grade.GOOD, day + 12).interval)
    }

    @Test fun aMissComesBackTodayAndSoonerAfter() {
        val learned = Srs.grade(Srs.grade(card("a"), Grade.GOOD, day), Grade.GOOD, day + 1)
        val missed = Srs.grade(learned, Grade.AGAIN, day + 4)
        assertEquals(day + 4, missed.due); assertEquals(0, missed.interval); assertEquals(1, missed.lapses)
        assertEquals(2.3, missed.ease, 1e-9)
        assertEquals(1, Srs.grade(missed, Grade.GOOD, day + 4).interval)
        // Ease never drops below 1.3, however often you miss.
        var c = learned
        repeat(20) { c = Srs.grade(c, Grade.AGAIN, day + 5) }
        assertEquals(1.3, c.ease, 1e-9)
    }

    @Test fun queueShowsDueReviewsThenOnlyTodaysShareOfNewCards() {
        val due = card("due").copy(firstSeen = day - 5, due = day - 1, interval = 3, reps = 2)
        val later = card("later").copy(firstSeen = day - 5, due = day + 3, interval = 3, reps = 2)
        val seenToday = card("today").copy(firstSeen = day, due = day + 1, interval = 1, reps = 1)
        val fresh = (1..10).map { card("new$it") }
        val q = Srs.queue(AppState(cards = listOf(due, later, seenToday) + fresh, newPerDay = 3), day)
        assertEquals("due", q.first().id)
        assertTrue("not due yet", q.none { it.id == "later" })
        assertEquals("3 a day minus the one already started today", 2, q.count { it.isNew })
    }

    @Test fun yourOwnPhrasesComeFirst() {
        val mine = card("mine:1", deck = "mine").copy(custom = true)
        val q = Srs.queue(AppState(cards = listOf(card("a"), card("b"), mine), newPerDay = 1), day)
        assertEquals(listOf("mine:1"), q.map { it.id })
    }

    @Test fun streakCountsBackFromTodayOrYesterday() {
        assertEquals(3, Srs.streak(listOf(day, day - 1, day - 2, day - 4), day))
        assertEquals(2, Srs.streak(listOf(day - 1, day - 2), day)) // today not done yet: still alive
        assertEquals(0, Srs.streak(listOf(day - 2), day))
    }
}
