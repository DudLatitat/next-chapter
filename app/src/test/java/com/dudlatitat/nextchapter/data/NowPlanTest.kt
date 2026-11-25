package com.dudlatitat.nextchapter.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class NowPlanTest {
    private val wake = 6 * 60 + 30
    private val wind = 23 * 60 + 30
    private val eve = 17 * 60 + 45
    private fun k(h: Int, m: Int = 0, atWork: Boolean = false, heading: Boolean = false, checked: Boolean = false) =
        NowPlan.kind(h * 60 + m, wake, wind, eve, atWork, heading, checked)

    @Test fun everyWindow() {
        assertEquals(NowPlan.Kind.EARLY, k(6, 10))
        assertEquals(NowPlan.Kind.MORNING, k(8))
        assertEquals(NowPlan.Kind.WORK, k(11, atWork = true))
        assertEquals(NowPlan.Kind.HEADING_HOME, k(16, 50, atWork = true, heading = true))
        assertEquals(NowPlan.Kind.DAY, k(14))
        assertEquals(NowPlan.Kind.EVENING, k(17, 40))
        assertEquals(NowPlan.Kind.AFTER_DONE, k(18, checked = true))
        assertEquals(NowPlan.Kind.AFTER_OPEN, k(21))
        assertEquals(NowPlan.Kind.AFTER_DONE, k(21, checked = true))
        assertEquals(NowPlan.Kind.WIND_DOWN, k(23, 45))
        assertEquals(NowPlan.Kind.WIND_DOWN, k(1))
    }

    @Test fun theCallHourAlwaysWins() {
        assertEquals(NowPlan.Kind.CALL, k(19))
        assertEquals(NowPlan.Kind.CALL, k(19, 59, atWork = true, heading = true))
        assertEquals("call now 💗", NowPlan.callLine(19 * 60 + 5))
        assertEquals("call in 2h 10m", NowPlan.callLine(16 * 60 + 50))
        assertEquals("call in 15m", NowPlan.callLine(18 * 60 + 45))
        assertEquals("call tmrw 7 PM", NowPlan.callLine(21 * 60))
    }

    @Test fun herLineFollowsHerClock() {
        val la = ZoneId.of("America/Los_Angeles")
        // 10 pm in LA on a winter day is 2 pm next day in China (16 h ahead): afternoon for her.
        assertEquals("afternoon", NowPlan.herPart(LocalDate.of(2026, 12, 1).atTime(22, 0).atZone(la)))
    }
}

class PillarsTest {
    @Test fun sixPillarsNoDeficitsAtTheStart() {
        val p = Pillars.all(AppState(), 20_500)
        assertEquals(6, p.size)
        assertEquals(listOf("话", "业", "钱", "身", "祷", "心"), p.map { it.glyph })
        assertTrue("no zero counts when nothing's started", p.none { Regex("\\b0\\b").containsMatchIn(it.value) })
        assertTrue("short enough for a three-across grid", p.all { it.value.length <= 8 })
    }

    @Test fun moneyPrefersRunway() {
        val s = AppState(saved = 14000, rate = 7.0, budget = listOf(BudgetItem("rent", "Rent", "房租", 7000)))
        assertEquals("14 mo", Pillars.all(s, 20_500).first { it.id == Pillars.Id.MONEY }.value)
    }
}

class DayLineTest {
    private val la = ZoneId.of("America/Los_Angeles")

    @Test fun herNightMovesWithDaylightSaving() {
        // Her 23:00 is 07:00 in LA in winter (PST, 16 h behind) and 08:00 in summer (PDT, 15 h behind).
        val winter = LocalDate.of(2026, 12, 1).toEpochDay()
        val summer = LocalDate.of(2026, 7, 1).toEpochDay()
        val w = DayLine.hers(winter, la).first { it.kind == DayLine.Kind.HER_SLEEP && it.from > 0 }
        val s = DayLine.hers(summer, la).first { it.kind == DayLine.Kind.HER_SLEEP && it.from > 0 }
        assertEquals(7 * 60, w.from)
        assertEquals(8 * 60, s.from)
        // Her workday 9–18 is 17:00–02:00 (winter): your evening, split at your midnight.
        assertTrue(DayLine.hers(winter, la).any { it.kind == DayLine.Kind.HER_WORK && it.from == 17 * 60 && it.to == 1440 })
    }

    @Test fun myDayHasTheCallAndNoOverlaps() {
        val s = AppState()
        val day = LocalDate.of(2026, 11, 25).toEpochDay() // Wednesday
        val b = DayLine.mine(s, day)
        assertTrue(b.any { it.kind == DayLine.Kind.CALL && it.from == 19 * 60 && it.to == 20 * 60 })
        assertTrue(b.zipWithNext().all { (x, y) -> x.to <= y.from })
        assertTrue("weekday has work", b.any { it.kind == DayLine.Kind.WORK })
        assertTrue("saturday doesn't", DayLine.mine(s, day + 3).none { it.kind == DayLine.Kind.WORK })
    }
}
