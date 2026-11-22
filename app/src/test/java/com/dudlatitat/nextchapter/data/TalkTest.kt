package com.dudlatitat.nextchapter.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TalkTest {
    @Test fun scenesAreWellFormed() {
        assertEquals(Talk.scenes.size, Talk.scenes.map { it.id }.toSet().size)
        assertEquals("the dinner scenes are in", true, Dinner.scenes.all { d -> Talk.byId(d.id) != null })
        Talk.Group.entries.forEach { g -> assertTrue("$g has scenes", Talk.scenes.any { it.group == g }) }
        Talk.scenes.forEach { sc ->
            assertTrue(sc.beats.size >= 4)
            sc.beats.forEach { b ->
                assertEquals(2, b.replies.size)
                b.replies.forEach { r -> assertTrue(r.hanzi.isNotBlank() && r.pinyin.isNotBlank() && r.english.isNotBlank()) }
            }
        }
    }

    @Test fun nothingPersonal() {
        val text = Talk.scenes.flatMap { sc -> sc.beats.flatMap { b -> listOf(b.hanzi, b.english, b.tip) + b.replies.flatMap { listOf(it.hanzi, it.english) } } }
        assertFalse(text.any { "成都" in it || "Chengdu" in it || "Yuling" in it || "玉玲" in it })
    }

    @Test fun typedAnswers() {
        val r = Dinner.Reply("早上好，宝贝", "zǎoshang hǎo, bǎobèi", "Good morning, babe")
        assertTrue(Talk.typedMatches("zaoshang hao baobei", r))
        assertTrue(Talk.typedMatches("Zǎoshang hǎo, bǎobèi!", r))
        assertTrue(Talk.typedMatches("zao3shang4 hao3 bao3bei4", r))
        assertTrue(Talk.typedMatches("早上好宝贝", r))
        assertTrue(Talk.typedMatches("早上好，宝贝！", r))
        assertFalse(Talk.typedMatches("zaoshang hao", r))
        assertFalse(Talk.typedMatches("早上好", r))
        assertFalse(Talk.typedMatches("  ", r))
        assertEquals("lvse", Talk.plain("lǜsè"))
    }

    @Test fun csCalloutsAreWellFormed() {
        assertTrue(Talk.csCallouts.size >= 15)
        assertEquals(Talk.csCallouts.size, Talk.csCallouts.map { it.hanzi }.toSet().size)
        Talk.csCallouts.forEach { assertTrue(it.pinyin.isNotBlank() && it.english.isNotBlank()) }
        assertTrue(Talk.byId("csmatch")!!.group == Talk.Group.GAMES)
        assertTrue(Talk.fortniteCallouts.size >= 12)
        assertEquals(Talk.fortniteCallouts.size, Talk.fortniteCallouts.map { it.hanzi }.toSet().size)
    }
}
