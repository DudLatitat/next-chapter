package com.dudlatitat.nextchapter.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NumbersTest {
    @Test fun everyLineSaysWhatItsAnswerSays() {
        (Numbers.prices + Numbers.times).forEach { assertEquals(it.hanzi, it.answer, Numbers.read(it.hanzi)) }
        assertEquals("no duplicates", (Numbers.prices + Numbers.times).size, (Numbers.prices + Numbers.times).map { it.hanzi }.toSet().size)
    }

    @Test fun readingChineseNumbers() {
        assertEquals(105, Numbers.whole("一百零五"))
        assertEquals(110, Numbers.whole("一百一十"))
        assertEquals(12, Numbers.whole("十二"))
        assertEquals(200, Numbers.whole("两百"))
        assertEquals("8:55", Numbers.read("差五分九点"))
    }

    @Test fun typedAnswers() {
        val price = Numbers.Item(Numbers.Kind.PRICE, "三十五块五", "35.5")
        assertTrue(Numbers.matches(price, "35.50")); assertTrue(Numbers.matches(price, " 35.5 "))
        assertFalse(Numbers.matches(price, "35.05")); assertFalse(Numbers.matches(price, "abc"))
        val time = Numbers.Item(Numbers.Kind.TIME, "差五分九点", "8:55")
        assertTrue(Numbers.matches(time, "8:55")); assertTrue(Numbers.matches(time, "855")); assertTrue(Numbers.matches(time, "8.55"))
        assertFalse(Numbers.matches(time, "9:55"))
        val whole = Numbers.Item(Numbers.Kind.TIME, "七点", "7:00")
        assertTrue(Numbers.matches(whole, "7")); assertTrue(Numbers.matches(whole, "7:00"))
    }

    @Test fun roundsAndSpeed() {
        assertEquals(Numbers.ROUND, Numbers.round().size)
        assertTrue(Numbers.round(Numbers.Kind.TIME).all { it.kind == Numbers.Kind.TIME })
        assertEquals(1f, Numbers.speed(0)); assertEquals(1.5f, Numbers.speed(20))
    }
}
