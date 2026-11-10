package com.dudlatitat.nextchapter.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SeedTest {
    @Test fun retiredCardsGoUnlessYouStudiedThem() {
        val untouched = Card("idv:压机", "压机", "yā jī", "Hold the last cipher", "idv")
        val studied = Card("idv:开门", "开门", "kāi mén", "Open the gate", "idv", firstSeen = 100, due = 103, interval = 3, reps = 2)
        val kept = Card("idv:快跑！", "快跑！", "kuài pǎo!", "Run!", "idv", firstSeen = 90, due = 95, interval = 5, reps = 3)
        val merged = Seed.merge(listOf(untouched, studied, kept))
        val ids = merged.map { it.id }
        assertTrue("never-studied retired card is removed", "idv:压机" !in ids)
        assertTrue("a retired card you studied keeps its progress", merged.first { it.id == "idv:开门" }.reps == 2)
        assertEquals("existing progress on a kept card is untouched", 3, merged.first { it.id == "idv:快跑！" }.reps)
        assertTrue("new Cryptic Notes cards arrive", "idv:快撤离！" in ids && "idv:这里有辞章" in ids)
        assertEquals("no duplicates", ids.size, ids.toSet().size)
    }

    @Test fun starterCardsThatLeftTheListGoOnlyIfNeverStudied() {
        val gone = Card("family:old", "旧", "jiù", "old line", "family")
        val goneStudied = Card("core:old", "旧", "jiù", "old word", "core", firstSeen = 100, due = 101, interval = 1, reps = 1)
        val mine = Card("mine:1", "我的", "wǒ de", "mine", "mine", custom = true)
        val ids = Seed.merge(listOf(gone, goneStudied, mine)).map { it.id }
        assertTrue("never-studied old starter card is removed", "family:old" !in ids)
        assertTrue("an old card you studied stays", "core:old" in ids)
        assertTrue("your own phrases always stay", "mine:1" in ids)
    }

    @Test fun noStarterCardIsAlsoRetired() {
        assertTrue((Seed.cards + Vocab.cards).none { it.id in Seed.retired })
    }

    @Test fun everyStarterCardBelongsToARealDeckAndIdsAreUnique() {
        val ids = (Seed.cards + Vocab.cards).map { it.id }
        assertEquals(ids.size, ids.toSet().size)
        val decks = Decks.map { it.id }.toSet()
        assertTrue((Seed.cards + Vocab.cards).all { it.deck in decks })
        assertEquals(48, Seed.cards.count { it.deck == "games" })
        assertEquals(35, Seed.cards.count { it.deck == "daily" })
        assertEquals(25, Seed.cards.count { it.deck == "sweet" })
        assertEquals(27, Seed.cards.count { it.deck == "trip" })
    }
}
