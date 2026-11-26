package com.dudlatitat.nextchapter.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CoreTwoTest {
    private val d = java.time.LocalDate.of(2026, 11, 26).toEpochDay()

    @Test fun deckIsSoundAndWeighted() {
        assertTrue("about 140 cards", APlus2.cards.size >= 130)
        assertEquals("unique ids", APlus2.cards.size, APlus2.cards.map { it.id }.toSet().size)
        assertTrue(APlus2.cards.all { it.id.startsWith("c2:") && it.domain in APlus2.domains.keys })
        assertEquals(100, APlus2.domains.values.sumOf { it.second })
        // Every domain has enough cards for a 20-question test's share, with 3 wrong options each.
        APlus2.domains.keys.forEach { k -> assertTrue("domain $k", APlus2.cards.count { it.domain == k } >= 20) }
        assertTrue(APlus.all.map { it.id }.toSet().size == APlus.all.size)
    }

    @Test fun statusFollowsWhatYouDo() {
        val fresh = AppState()
        assertEquals("planned", Career.status(fresh, "aplus1"))
        val studying = fresh.copy(aplus = listOf(Review(APlus.core1[0].id, firstSeen = d)))
        assertEquals("studying", Career.status(studying, "aplus1"))
        val booked = Career.withDate(studying, "aplus1", "2027-01-16")
        assertEquals("booked", Career.status(booked, "aplus1"))
        assertEquals("one shared date", "2027-01-16", APlus.examDate(booked))
        assertEquals("2027-01-16", booked.aplusExam)
        // The older A+ date alone also counts as booked.
        assertEquals("booked", Career.status(AppState(aplusExam = "2027-01-16"), "aplus1"))
    }

    @Test fun passingCoreOneMovesToCoreTwo() {
        val s0 = Career.withDate(AppState(aplus = listOf(Review(APlus.core1[0].id, firstSeen = d - 30, due = d, interval = 10))), "aplus1", "2027-01-16")
        assertEquals(1, APlus.core(s0))
        val s = Career.withPassed(s0, "aplus1", true)
        assertEquals(2, APlus.core(s))
        assertEquals("passed", Career.status(s, "aplus1"))
        assertEquals("A+ Core 2", APlus.examName(s))
        assertNull("Core 2 has no date yet", APlus.examDate(s))
        val q = APlus.queue(s.copy(aplusPerDay = 10), d)
        assertTrue("new cards are Core 2", q.filter { it.id.startsWith("c2:") }.size == 10)
        assertTrue("due Core 1 card comes back as a refresh", q.any { it.id == APlus.core1[0].id })
        assertEquals(0, APlus.readyPct(s))
        assertEquals("Core 2 ready counts Core 2 only", null, APlus.weakest(s))
        // A practice test on Core 2 only asks Core 2.
        assertTrue(PracticeTest.build(20, kotlin.random.Random(1), core = 2).all { it.card.id.startsWith("c2:") })
        // Undo goes back.
        assertEquals(1, APlus.core(Career.withPassed(s, "aplus1", false)))
    }

    @Test fun refreshIsCapped() {
        val due = APlus.core1.take(10).map { Review(it.id, firstSeen = d - 40, due = d - 1, interval = 20) }
        val s = Career.withPassed(AppState(aplus = due, aplusPerDay = 0), "aplus1", true)
        assertEquals(APlus.REFRESH, APlus.queue(s, d).size)
    }
}
