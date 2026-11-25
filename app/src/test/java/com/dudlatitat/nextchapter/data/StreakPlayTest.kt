package com.dudlatitat.nextchapter.data

import org.junit.Assert.assertEquals
import org.junit.Test

class StreakPlayTest {
    @Test fun aDayOfOnlyGamesKeepsTheStreak() {
        val d = 20_500L
        // Reviewed two days, then spent the third evening only playing Talk/Callouts (no reviews).
        var s = AppState(days = mapOf(d - 2 to DayLog(reviews = 10), d - 1 to DayLog(reviews = 5), d to DayLog(seconds = 600)))
        assertEquals("the bug: an evening of only games wasn't a practice day", false, d in Stats.practiceDays(s))
        s = Stats.practiced(s, d)
        assertEquals(3, Stats.streak(s, d))
        assertEquals("marking twice changes nothing", s, Stats.practiced(s, d))
    }
}
