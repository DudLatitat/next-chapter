package com.dudlatitat.nextchapter.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LadderTest {
    private val line = "我们的天父，愿你的名受显扬；"

    @Test fun rungsHideMoreButNeverPunctuation() {
        assertEquals(line, Ladder.masked(line, 0))
        assertEquals("我们的＿父，愿你的＿受显扬；", Ladder.masked(line, 1))
        assertEquals("我＿的＿父，愿＿的＿受＿扬；", Ladder.masked(line, 2))
        assertEquals("我＿＿＿＿，愿＿＿＿＿＿＿；", Ladder.masked(line, 3))
        assertEquals("＿＿＿＿＿，＿＿＿＿＿＿＿；", Ladder.masked(line, 4))
        assertEquals("same length, so lines don't jump around", line.length, Ladder.masked(line, 3).length)
    }

    @Test fun theOurFatherIsNeverChangedOnlyCovered() {
        val p = Prayers.daily.first { it.title == "Our Father" }
        p.lines.forEach { (zh, _) ->
            val m = Ladder.masked(zh, 2)
            zh.indices.forEach { i -> assertTrue(m[i] == zh[i] || m[i] == Ladder.GAP) }
        }
        assertTrue(Ladder.hiddenShare(p.lines.map { it.first }, 4) == 1f)
        assertEquals(0f, Ladder.hiddenShare(p.lines.map { it.first }, 0))
    }

    @Test fun climbingIsSavedAndNeverGoesDown() {
        var saved = Ladder.climbed(emptyMap(), "Our Father", 0)
        assertEquals(1, saved["Our Father"])
        saved = Ladder.climbed(saved, "Our Father", 0)
        assertEquals("redoing a lower rung keeps your place", 1, saved["Our Father"])
        assertFalse(Ladder.byHeart(saved, "Our Father"))
        saved = Ladder.climbed(saved, "Our Father", Ladder.TOP)
        assertTrue(Ladder.byHeart(saved, "Our Father"))
    }
}
