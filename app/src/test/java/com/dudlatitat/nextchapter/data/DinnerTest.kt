package com.dudlatitat.nextchapter.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DinnerTest {
    @Test fun scenesAreWellFormed() {
        assertEquals(3, Dinner.scenes.size)
        assertEquals(Dinner.scenes.size, Dinner.scenes.map { it.id }.toSet().size)
        Dinner.scenes.forEach { sc ->
            assertTrue(sc.beats.size in 6..10)
            sc.beats.forEach { b ->
                assertEquals("${b.hanzi}: two replies", 2, b.replies.size)
                assertTrue(b.pinyin.isNotBlank() && b.english.isNotBlank())
                b.replies.forEach { r -> assertTrue("${r.hanzi} needs pinyin and meaning", r.pinyin.isNotBlank() && r.english.isNotBlank()) }
            }
        }
    }

    @Test fun nothingPersonalInThePublicRepo() {
        val all = Dinner.scenes.flatMap { sc -> sc.beats.flatMap { b -> listOf(b.hanzi, b.english, b.tip) + b.replies.flatMap { listOf(it.hanzi, it.english) } } }
        assertFalse(all.any { "成都" in it || "Chengdu" in it })
    }

    @Test fun chopsticksAndEndings() {
        assertEquals(2, Dinner.chopsticks(Callouts.Verdict.GREAT))
        assertEquals(1, Dinner.chopsticks(Callouts.Verdict.CLOSE))
        assertEquals(0, Dinner.chopsticks(Callouts.Verdict.MISS))
        val sc = Dinner.scenes.first()
        assertEquals(sc.beats.size * 2, Dinner.most(sc))
        assertEquals("🏮", Dinner.ending(16, 16).first)
        assertEquals("🌱", Dinner.ending(0, 16).first)
        assertEquals("🌱", Dinner.ending(0, 0).first)
    }
}
