package com.dudlatitat.nextchapter.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import kotlin.random.Random

class SwapsTest {
    @Test fun framesHaveOneSlotAndPinyinForEveryCharacter() {
        Swaps.frames.forEach { f ->
            assertEquals("", f.words[f.slot].first)
            assertTrue(f.options.size >= 3)
            f.options.forEach { o ->
                val c = Swaps.card(f, o)
                // Every character has a syllable (checked with the Build splitter).
                val words = Builder.words(c)
                assertTrue("${c.hanzi} / ${c.pinyin}", words != null)
            }
        }
    }

    @Test fun aSwapBuildsANewSentenceFromOneYouKnow() {
        val f = Swaps.frames.first()
        assertEquals("我想跟你一起看电影。", Swaps.card(f, f.options[2]).hanzi)
        assertEquals("wǒ xiǎng gēn nǐ yìqǐ kàn diànyǐng.", Swaps.card(f, f.options[2]).pinyin)
        repeat(30) { i ->
            val s = Swaps.pick(Random(i))
            assertFalse(s.from.hanzi == s.to.hanzi)
            assertTrue(s.tiles.containsAll(s.answer))
            assertEquals(s.to.hanzi.dropLast(1), s.answer.joinToString(""))
        }
    }

    @Test fun tonightsSentenceShowsSixToSevenOnTheRightDay() {
        val d = 20_400L
        assertEquals(d, TonightLine.dayFor(d, 14 * 60))
        assertEquals("starred after 7 pm: tomorrow", d + 1, TonightLine.dayFor(d, 19 * 60 + 5))
        val line = TonightLine(d, "我想你", "wǒ xiǎng nǐ", "I miss you")
        assertFalse(TonightLine.showing(line, d, 17 * 60 + 59))
        assertTrue(TonightLine.showing(line, d, 18 * 60))
        assertTrue(TonightLine.showing(line, d, 18 * 60 + 59))
        assertFalse("never during the call", TonightLine.showing(line, d, 19 * 60))
        assertFalse(TonightLine.showing(line, d + 1, 18 * 60 + 30))
    }

    @Test fun tripPrepOpensSixtyDaysBeforeTheTrip() {
        val today = LocalDate.of(2026, 11, 10).toEpochDay()
        val s = AppState(tripDate = "2027-01-15")
        assertEquals(66L, TripPrep.daysToTrip(s, today))
        assertFalse(TripPrep.open(s, today))
        assertEquals(6L, TripPrep.opensIn(s, today))
        assertTrue(TripPrep.open(s, today + 6))
        assertNull(TripPrep.opensIn(s, today + 6))
        assertFalse("after the trip", TripPrep.open(s, today + 67))
    }

    @Test fun tripPhrasesWaitUntilTheDeckOpensThenComeFirst() {
        val today = LocalDate.of(2026, 11, 10).toEpochDay()
        val s = AppState(tripDate = "2027-01-15", newPerDay = 5, cards = Seed.cards)
        assertTrue(Srs.queue(s, today).none { it.deck == "trip" })
        val open = Srs.queue(s, today + 10)
        assertTrue(open.take(5).all { it.deck == "trip" })
    }
}
