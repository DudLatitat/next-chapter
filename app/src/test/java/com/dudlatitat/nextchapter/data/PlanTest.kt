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

    @Test fun progressNeverGoesPastFull() {
        assertEquals(0f, AppState(savingsGoal = 0, saved = 500).savingsProgress(), 0f)
        assertEquals(0.5f, AppState(savingsGoal = 1000, saved = 500).savingsProgress(), 1e-6f)
        assertEquals(1f, AppState(savingsGoal = 1000, saved = 5000).savingsProgress(), 1e-6f)
    }
}
