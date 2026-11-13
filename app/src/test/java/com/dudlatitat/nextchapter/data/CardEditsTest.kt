package com.dudlatitat.nextchapter.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CardEditsTest {
    private val d = 20_400L
    private val starter = Card("games:玩", "玩", "wán", "play", "games")

    @Test fun gradingCountsTheReviewForTheDay() {
        var s = AppState(cards = listOf(starter))
        s = CardEdits.grade(s, "games:玩", Grade.GOOD, d)
        s = CardEdits.grade(s, "games:玩", Grade.GOOD, d)
        assertEquals(2, s.days[d]!!.reviews)
        assertEquals(d, s.cards[0].last)
    }

    @Test fun undoPutsTheCardAndTheCountBack() {
        val start = AppState(cards = listOf(starter))
        val once = CardEdits.grade(start, "games:玩", Grade.GOOD, d)
        val mistap = CardEdits.grade(once, "games:玩", Grade.AGAIN, d)
        val undone = CardEdits.ungrade(mistap, once.cards[0], d)
        assertEquals("the card is exactly as before the mis-tap", once.cards, undone.cards)
        assertEquals(1, undone.days[d]!!.reviews)
        // Never below zero, even if the log was trimmed.
        assertEquals(0, CardEdits.ungrade(start, starter, d).days[d]!!.reviews)
    }

    @Test fun addEditAndDelete() {
        var s = CardEdits.add(AppState(), " 我想你 ", "wǒ xiǎng nǐ", "I miss you", now = 5)
        assertEquals("mine:5", s.cards.single().id); assertEquals("我想你", s.cards.single().hanzi)
        s = CardEdits.update(s, "mine:5", "我好想你", "wǒ hǎo xiǎng nǐ", "I miss you so much")
        assertEquals("我好想你", s.cards.single().hanzi)
        assertTrue(CardEdits.delete(s, "mine:5").cards.isEmpty())
    }

    @Test fun starterPhrasesKeepTheirCharacters() {
        val s = CardEdits.update(AppState(cards = listOf(starter)), "games:玩", "吃", "wán", "to play")
        assertEquals("玩", s.cards.single().hanzi)
        assertEquals("to play", s.cards.single().english)
        assertFalse(s.cards.single().custom)
    }
}
