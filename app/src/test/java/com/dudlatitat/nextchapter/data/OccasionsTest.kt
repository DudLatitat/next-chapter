package com.dudlatitat.nextchapter.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class OccasionsTest {
    // A fake lunar calendar with known 2027 dates (the app uses the phone's ICU calendar).
    private val fake = Occasions.Lunar { y, m, d ->
        when (Triple(y, m, d)) {
            Triple(2027, 1, 1) -> LocalDate.of(2027, 2, 6)
            Triple(2027, 1, 15) -> LocalDate.of(2027, 2, 20)
            else -> null
        }
    }

    @Test fun nextThreeWeeksSoonestFirst() {
        val up = Occasions.upcoming(LocalDate.of(2027, 1, 30), fake)
        assertEquals(listOf("spring", "valentine", "lantern"), up.map { it.occasion.id })
        assertEquals(7, up[0].daysAway)
        assertEquals("新年快乐", up[0].occasion.greeting)
    }

    @Test fun acrossNewYearAndWithoutLunar() {
        val up = Occasions.upcoming(LocalDate.of(2026, 12, 10), null)
        assertEquals("christmas", up.single().occasion.id)
        assertEquals(15, up.single().daysAway)
        assertTrue(Occasions.upcoming(LocalDate.of(2026, 3, 1), null).isEmpty())
    }

    @Test fun theCheckedTable() {
        assertEquals(LocalDate.of(2027, 2, 6), Occasions.Table.toDate(2027, 1, 1)) // not Feb 7
        assertEquals(LocalDate.of(2028, 5, 28), Occasions.Table.toDate(2028, 5, 5))
        val up = Occasions.upcoming(LocalDate.of(2026, 9, 10), Occasions.Table)
        assertEquals("midautumn", up.first().occasion.id)
        assertEquals(15, up.first().daysAway)
    }
}
