package com.dudlatitat.nextchapter.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TextingTest {
    @Test fun everyExampleReplyPassesItsOwnCheck() {
        assertEquals(Texting.threads.size, Texting.threads.map { it.id }.toSet().size)
        Texting.threads.forEach { th ->
            th.turns.forEach { t ->
                assertTrue(t.them.isNotEmpty() && t.examples.isNotEmpty() && t.words.isNotEmpty())
                t.examples.forEach { e -> assertTrue("${th.id}: ${e.hanzi} must pass", Texting.accepts(t, e.hanzi)) }
            }
        }
    }

    @Test fun repliesPassOnKeyWordsInYourOwnWords() {
        val t = Texting.threads.first { it.id == "evening" }.turns[0] // 下班了吗？
        assertTrue(Texting.accepts(t, "下班了！好累"))
        assertTrue(Texting.accepts(t, "还没呢"))
        assertFalse(Texting.accepts(t, "你好"))
        assertFalse("pinyin isn't Chinese typing", Texting.accepts(t, "xiaban le"))
        assertFalse(Texting.accepts(t, ""))
    }

    @Test fun sprintsCheckCharactersAndSpeed() {
        val c = Card("text:收到", "收到", "shōudào", "Got it", "text")
        assertTrue(Texting.typedRight("收到！", c)); assertTrue(Texting.typedRight(" 收 到 ", c))
        assertFalse(Texting.typedRight("收", c)); assertFalse(Texting.typedRight("shoudao", c))
        assertEquals(30, Texting.cpm(15, 30_000)); assertEquals(0, Texting.cpm(5, 0))
        val pool = Texting.sprintPool(AppState(cards = Seed.cards))
        assertTrue(pool.isNotEmpty() && pool.all { it.deck in Texting.sprintDecks })
    }

    @Test fun nothingPersonal() {
        val all = Texting.threads.flatMap { th -> listOf(th.contact, th.title) + th.turns.flatMap { t -> t.them.map { it.hanzi + it.english } + t.examples.map { it.hanzi + it.english } + t.tip } }
        assertFalse(all.any { "成都" in it || "Chengdu" in it || "Yuling" in it || "玉玲" in it })
    }
}
