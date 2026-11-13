package com.dudlatitat.nextchapter.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class CalloutsTest {
    @Test fun everyPromptIsARealIdentityVCard() {
        val idv = Seed.cards.filter { it.deck == "idv" }.map { it.hanzi }.toSet()
        Callouts.prompts.forEach { assertTrue("${it.hanzi} must be an idv card (so it has a voice and pinyin)", it.hanzi in idv) }
        assertEquals("no duplicates", Callouts.prompts.size, Callouts.prompts.map { it.hanzi }.toSet().size)
    }

    @Test fun aRoundIsTenDifferentPrompts() {
        val r = Callouts.round(Random(1))
        assertEquals(Callouts.ROUND, r.size)
        assertEquals(r.size, r.toSet().size)
    }

    @Test fun verdicts() {
        assertEquals(Callouts.Verdict.GREAT, Callouts.verdict(right = 2, tone = 0, missed = 0))
        assertEquals("one tone slip in four is close", Callouts.Verdict.CLOSE, Callouts.verdict(3, 1, 0))
        assertEquals(Callouts.Verdict.CLOSE, Callouts.verdict(3, 0, 1))
        assertEquals(Callouts.Verdict.MISS, Callouts.verdict(1, 0, 2))
        assertEquals("heard nothing", Callouts.Verdict.MISS, Callouts.verdict(0, 0, 0))
    }

    @Test fun fasterIsWorthMore() {
        assertEquals(180, Callouts.points(Callouts.Verdict.GREAT, 8))
        assertEquals(100, Callouts.points(Callouts.Verdict.GREAT, 0))
        assertEquals(100, Callouts.points(Callouts.Verdict.GREAT, -3))
        assertEquals(60, Callouts.points(Callouts.Verdict.CLOSE, 2))
        assertEquals(0, Callouts.points(Callouts.Verdict.MISS, 8))
        assertEquals(1800, Callouts.maxScore)
    }
}
