package com.dudlatitat.nextchapter.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StatusTest {
    private val d = java.time.LocalDate.of(2026, 11, 25).toEpochDay() // a Wednesday

    @Test fun nothingStartedShowsNoZeros() {
        val s = AppState()
        assertTrue(Status.life(s, d).isEmpty())
        assertTrue(Status.move(s, d).none { it.contains("0") && it.startsWith("✈") })
        assertTrue("no flame at zero", Status.chinese(s, d).none { it.startsWith("🔥") })
    }

    @Test fun lifeCountsThisWeekAndPrayer() {
        val s = AppState(
            routineChecks = listOf(RoutineCheck(d - 1, CheckIn.DONE), RoutineCheck(d - 2, CheckIn.MINIMUM), RoutineCheck(d - 9, CheckIn.DONE)),
            prayedDays = listOf(d, d - 3, d - 10),
        )
        val f = Status.life(s, d)
        assertEquals("🌙 2 this week", f[0])
        assertEquals("祷 2/7", f.last())
    }

    @Test fun moveShowsRunwayOnlyWhenThereIsABudget() {
        val s = AppState(saved = 12000, rate = 7.0, budget = listOf(BudgetItem("rent", "Rent", "房租", 6000)))
        assertTrue(Status.move(s, d).any { it.endsWith("mo runway") })
        assertTrue(Status.move(s.copy(budget = emptyList()), d).none { it.endsWith("mo runway") })
    }
}
