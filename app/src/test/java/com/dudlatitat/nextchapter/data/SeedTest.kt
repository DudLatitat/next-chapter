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

    @Test fun noStarterCardIsAlsoRetired() {
        assertTrue((Seed.cards + Vocab.cards).none { it.id in Seed.retired })
    }
}
