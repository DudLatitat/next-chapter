package com.dudlatitat.nextchapter.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class FamilyTest {
    @Test fun relativesAreWellFormed() {
        assertEquals(Family.all.size, Family.all.map { it.id }.toSet().size)
        Family.all.forEach {
            assertEquals("${it.id}: greet is the title + 好", it.hanzi + "好", it.greet)
            assertTrue(it.greetPinyin.startsWith(it.pinyin))
        }
        assertTrue("her mom's side is there first", Family.side(Family.Side.MOM).size >= 8)
    }

    @Test fun questionsHaveFourDifferentChoicesIncludingTheAnswer() {
        repeat(50) { i ->
            val (answer, choices) = Family.question(Random(i))
            assertEquals(4, choices.size)
            assertTrue(answer in choices)
            assertEquals(4, choices.map { it.hanzi }.toSet().size)
        }
    }
}
