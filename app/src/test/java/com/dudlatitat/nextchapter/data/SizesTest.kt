package com.dudlatitat.nextchapter.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SizesTest {
    @Test fun longPhrasesStepDownSoTheyFit() {
        assertEquals(44f, Sizes.hanzi("你好", 44f))
        assertEquals("the phrase from the bug report (9 characters)", 44f * 0.82f, Sizes.hanzi("别忙了，坐下一起吃吧", 44f), 0.01f)
        assertTrue(Sizes.hanzi("我永远站在你这边，我们一起想办法", 44f) < Sizes.hanzi("我永远站在你这边", 44f))
        assertEquals("never below 20sp", 20f, Sizes.hanzi("一".repeat(40), 22f))
    }

    @Test fun linesNeverOverlap() {
        // The bug: 44sp characters on the theme's 24sp line height. Spacing must exceed the glyph size.
        listOf(22f, 34f, 44f).forEach { assertTrue(Sizes.lineHeight(it) > it) }
    }
}
