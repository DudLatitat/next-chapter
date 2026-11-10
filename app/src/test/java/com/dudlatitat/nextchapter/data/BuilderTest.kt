package com.dudlatitat.nextchapter.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class BuilderTest {
    private fun card(h: String, p: String, deck: String = "games", isNew: Boolean = false) =
        Card("$deck:$h", h, p, "e", deck, firstSeen = if (isNew) null else 1)

    @Test fun countsSyllables() {
        assertEquals(1, Builder.syllables("xiǎng"))
        assertEquals(2, Builder.syllables("yìqǐ"))
        assertEquals(2, Builder.syllables("kě'ài"))
        assertEquals(3, Builder.syllables("nǚpéngyou"))
        assertEquals(2, Builder.syllables("shēngyīn"))
    }

    @Test fun splitsPhrasesIntoWordsByThePinyin() {
        assertEquals(listOf("我", "想", "跟", "你", "一起", "玩"), Builder.words(card("我想跟你一起玩", "wǒ xiǎng gēn nǐ yìqǐ wán")))
        assertEquals(listOf("你", "笑", "什么"), Builder.words(card("你笑什么？", "nǐ xiào shénme?")))
        assertEquals(listOf("不", "用", "了", "谢谢"), Builder.words(card("不用了，谢谢", "bú yòng le, xièxie")))
        // Erhua doesn't line up (哪儿 is one syllable): skipped rather than guessed.
        assertNull(Builder.words(card("你在哪儿？", "nǐ zài nǎr?")))
    }

    @Test fun onlyMediumPhrasesFromDecksAreUsed() {
        val cards = listOf(
            card("我想跟你一起玩", "wǒ xiǎng gēn nǐ yìqǐ wán"),
            card("你好", "nǐ hǎo"),                      // too short
            card("一起", "yìqǐ", deck = "core"),          // core word
        )
        assertEquals(1, Builder.eligible(cards).size)
    }

    @Test fun aRoundMixesKindsAndStartsWithPhrasesYouKnow() {
        val cards = Seed.cards.filter { Builder.words(it)?.size in 3..8 }.take(10)
            .mapIndexed { i, c -> c.copy(firstSeen = if (i < 5) 1 else null) }
        val r = Builder.round(cards, Random(1))
        assertEquals(8, r.size)
        assertEquals(listOf(Builder.Kind.BUILD, Builder.Kind.FILL, Builder.Kind.LISTEN), r.take(3).map { it.kind })
        assertTrue("the 5 started phrases come first", r.take(5).all { !it.card.isNew })
    }

    @Test fun buildTilesHoldEveryWordPlusDecoysAndFillHasTheAnswer() {
        val cards = listOf(
            card("我想跟你一起玩", "wǒ xiǎng gēn nǐ yìqǐ wán"),
            card("你今天怎么样？", "nǐ jīntiān zěnmeyàng?"),
            card("我们是最好的队友", "wǒmen shì zuì hǎo de duìyǒu"),
        )
        val r = Builder.round(cards, Random(7))
        r.forEach { item ->
            when (item.kind) {
                Builder.Kind.FILL -> { assertTrue(item.answer[item.blank] in item.tiles); assertTrue(item.tiles.size in 2..4) }
                else -> { assertTrue(item.tiles.containsAll(item.answer)); assertTrue(item.tiles.size > item.answer.size) }
            }
        }
    }

    @Test fun plentyOfTheStarterPhrasesCanBeBuilt() {
        val ok = Builder.eligible(Seed.cards)
        assertTrue("only ${ok.size} buildable", ok.size >= 40)
        // Every split puts every character back in order.
        ok.forEach { (c, ws) -> assertEquals(c.hanzi.filter { ch -> ch.toString().matches(Regex("""\p{IsHan}""")) }, ws.joinToString("")) }
    }

    @Test fun checking() {
        val a = listOf("我", "想", "你")
        assertTrue(Builder.isRight(a, listOf("我", "想", "你")))
        assertFalse(Builder.isRight(a, listOf("我", "你", "想")))
        assertEquals(listOf(true, false, false), Builder.marks(a, listOf("我", "你", "想")))
    }
}
