package com.dudlatitat.nextchapter.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MoneyTest {
    @Test fun convertsBothWaysAndRounds() {
        assertEquals(712L, Money.toYuan(100.0, 7.1234))
        assertEquals(713L, Money.toYuan(100.0, 7.1299))
        assertEquals(143, Money.toWholeDollars(1000.0, 7.0)) // 142.86 rounds to 143, not 142
        assertEquals(14.29, Money.toDollars(100.0, 7.0), 0.005)
    }

    @Test fun aBadRateNeverDividesByZero() {
        assertEquals(Money.toWholeDollars(700.0, Money.FALLBACK_RATE), Money.toWholeDollars(700.0, 0.0))
        assertEquals(Money.toYuan(10.0, Money.FALLBACK_RATE), Money.toYuan(10.0, -3.0))
    }

    @Test fun savingsPaceRoundsUpSoYouActuallyGetThere() {
        // $10,000 left over 365 days ≈ 11.99 months → 834.0..., rounded up.
        val each = Money.savePerMonth(goal = 12_000, saved = 2_000, daysLeft = 365)!!
        assertEquals(834, each)
        val months = 365 / 30.4375
        assert(each * months >= 10_000) { "following the pace must reach the goal" }
        assertEquals(500, Money.savePerMonth(goal = 1_000, saved = 500, daysLeft = 10)) // under a month: all of it
        assertNull(Money.savePerMonth(goal = 1_000, saved = 1_000, daysLeft = 100))
        assertNull(Money.savePerMonth(goal = 0, saved = 0, daysLeft = 100))
        assertNull(Money.savePerMonth(goal = 1_000, saved = 0, daysLeft = 0))
    }

    @Test fun monthsCovered() {
        assertEquals(10.0, Money.monthsCovered(savedUsd = 7_000, rate = 7.0, monthlyRmb = 4_900), 1e-9)
        assertEquals(0.0, Money.monthsCovered(7_000, 7.0, 0), 0.0)
    }

    @Test fun readsTheRateFromTheReplyAndIgnoresNonsense() {
        assertEquals(7.1234, Money.parseCny("""{"rates":{"CAD":1.37,"CNY":7.1234,"EUR":0.92}}""")!!, 1e-9)
        assertNull(Money.parseCny("""{"rates":{"CNY":0}}"""))
        assertNull(Money.parseCny("""{"rates":{"CNY":712.34}}"""))
        assertNull(Money.parseCny("""{"result":"error"}"""))
    }

    @Test fun runwayFillsMonthByMonth() {
        assertEquals(listOf(1f, 1f, 0.5f, 0f), Money.runway(2.5, 4))
        assertEquals(listOf(1f, 1f), Money.runway(30.0, 2))
        assertEquals(listOf(0f, 0f), Money.runway(0.0, 2))
        assertEquals(12, Money.runway(3.0).size)
    }
}
