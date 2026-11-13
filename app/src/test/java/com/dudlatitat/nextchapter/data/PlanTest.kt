package com.dudlatitat.nextchapter.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class PlanTest {
    @Test fun friendlyCountdowns() {
        assertEquals("passed", friendlySpan(-1))
        assertEquals("today", friendlySpan(0))
        assertEquals("1 day", friendlySpan(1))
        assertEquals("99 days", friendlySpan(99))
        assertEquals("6 months", friendlySpan(180))
        assertEquals("1 yr", friendlySpan(365))
        assertEquals("1 yr 7 mo", friendlySpan(365 + 7 * 30))
    }

    @Test fun daysUntilCountsRealCalendarDays() {
        assertEquals(10L, daysUntil(LocalDate.now().plusDays(10).toString()))
        assertEquals(-1L, daysUntil(LocalDate.now().minusDays(1).toString()))
        assertEquals(null, daysUntil("not a date"))
        assertEquals(null, daysUntil(null))
    }

    @Test fun changingNextTripMovesThePlannedTripNotAHiddenDate() {
        val today = LocalDate.now()
        val trip = Trip("t1", today.plusDays(30).toString(), today.plusDays(44).toString())
        val later = Trip("t2", today.plusDays(200).toString())
        val s = AppState(trips = listOf(trip, later))
        val moved = s.withNextTripDate(today.plusDays(40).toString())
        assertEquals("the card shows the new date", today.plusDays(40).toString(), moved.nextTripDate())
        assertEquals("the return keeps the trip length", today.plusDays(54).toString(), moved.trips.first { it.id == "t1" }.returnDate)
        assertEquals("other trips untouched", later, moved.trips.first { it.id == "t2" })
        // With no planned trip, the hand-set date is what changes.
        assertEquals(today.plusDays(9).toString(), AppState().withNextTripDate(today.plusDays(9).toString()).nextTripDate())
    }

    @Test fun progressNeverGoesPastFull() {
        assertEquals(0f, AppState(savingsGoal = 0, saved = 500).savingsProgress(), 0f)
        assertEquals(0.5f, AppState(savingsGoal = 1000, saved = 500).savingsProgress(), 1e-6f)
        assertEquals(1f, AppState(savingsGoal = 1000, saved = 5000).savingsProgress(), 1e-6f)
    }

    @Test fun phraseOfTheDayStaysPutWhileYouReview() {
        val day = 20_000L
        val learned = (1..5).map { Card("d:$it", "字$it", "zì", "w$it", "daily", firstSeen = day - 10, due = day) }
        val fresh = Card("d:new", "新", "xīn", "new", "daily")
        val morning = AppState(cards = learned + fresh)
        val pick = morning.phraseOfTheDay(day)
        // After the morning review the new card is seen today and another phrase is added: same pick.
        val evening = morning.copy(cards = learned + fresh.copy(firstSeen = day, due = day + 1) + Card("mine:x", "加", "jiā", "add", "mine", custom = true))
        assertEquals(pick, evening.phraseOfTheDay(day))
        assertEquals("never a card first seen today", true, pick!!.firstSeen!! < day)
    }
}
