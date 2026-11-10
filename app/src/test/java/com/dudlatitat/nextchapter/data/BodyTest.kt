package com.dudlatitat.nextchapter.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BodyTest {
    private val d = 20_400L

    @Test fun oneWeightPerDayAndTheLatestWins() {
        var w = Body.withWeight(emptyList(), d - 1, 183.0)
        w = Body.withWeight(w, d, 182.6)
        w = Body.withWeight(w, d, 182.4) // weighed again: replaces today's
        assertEquals(2, w.size)
        assertEquals(182.4, Body.latest(w)!!.lb, 1e-9)
    }

    @Test fun weekChangeComparesWithAboutAWeekAgo() {
        val w = listOf(WeightEntry(d - 10, 185.0), WeightEntry(d - 7, 184.0), WeightEntry(d - 2, 183.0), WeightEntry(d, 182.5))
        assertEquals(-1.5, Body.weekChange(w)!!, 1e-9)
        assertNull(Body.weekChange(listOf(WeightEntry(d - 3, 184.0), WeightEntry(d, 183.0))))
    }

    @Test fun caloriesAddUpForTodayAndUndoTakesBackTheLastTap() {
        var c = Body.addCalories(emptyList(), d - 1, 2000)
        c = Body.addCalories(c, d, 450)
        c = Body.addCalories(c, d, 600)
        c = Body.addCalories(c, d, 0) // ignored
        assertEquals(1050, Body.caloriesOn(c, d))
        c = Body.undoLast(c, d)
        assertEquals(450, Body.caloriesOn(c, d))
        assertEquals("yesterday untouched", 2000, Body.caloriesOn(c, d - 1))
    }

    @Test fun pillReadsNaturally() {
        assertEquals("⚖️ Add weight · 🍽 0 cal", Body.pill(AppState(), d))
        val s = AppState(
            weights = listOf(WeightEntry(d, 182.4)), goalWeight = 170.0, calorieTarget = 2000,
            calorieAdds = listOf(CalorieAdd(d, 1450)),
        )
        assertEquals("⚖️ 182.4 → 170 lb · 🍽 1,450 / 2,000 cal", Body.pill(s, d))
        assertEquals("⚖️ 182.4 lb · 🍽 1,450 cal", Body.pill(s.copy(goalWeight = null, calorieTarget = 0), d))
    }
}
