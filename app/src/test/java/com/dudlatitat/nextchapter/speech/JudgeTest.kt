package com.dudlatitat.nextchapter.speech

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class JudgeTest {
    @Test fun thirdToneBeforeThirdMayBeSecond() {
        // 你好 nǐ hǎo: natives say ní hǎo, and both count.
        assertEquals(listOf(setOf(3, 2), setOf(3)), Judge.acceptedTones("你好", listOf(3, 3)))
    }

    @Test fun buAndYiChange() {
        assertEquals(setOf(4, 2), Judge.acceptedTones("不是", listOf(4, 4))[0])
        assertEquals(setOf(4), Judge.acceptedTones("不好", listOf(4, 3))[0])
        assertEquals(setOf(1, 2), Judge.acceptedTones("一个", listOf(1, 4))[0])
        assertEquals(setOf(1, 4), Judge.acceptedTones("一起", listOf(1, 3))[0])
        assertEquals(setOf(1), Judge.acceptedTones("十一", listOf(2, 1))[1])
    }

    @Test fun neutralToneIsNotJudged() {
        assertTrue(Judge.acceptedTones("谢谢", listOf(4, 5))[1].isEmpty())
    }

    @Test fun spansCountOnlyVoicedFrames() {
        // 10 frames, voiced at 2..3 and 6..9 (6 voiced). A span 0.06–0.10 s covers the last 4.
        val hz = floatArrayOf(0f, 0f, 100f, 100f, 0f, 0f, 100f, 100f, 100f, 100f)
        val s = Judge.spansOnCurve(hz, listOf(0.0f to 0.04f, 0.06f to 0.10f))
        assertEquals(0f, s[0].first, 1e-6f); assertEquals(2 / 6f, s[0].second, 1e-6f)
        assertEquals(2 / 6f, s[1].first, 1e-6f); assertEquals(1f, s[1].second, 1e-6f)
    }

    @Test fun matchTimesInOrderAndSkipsMissing() {
        val heard = listOf(Triple('我', 0.1f, 0.3f), Triple('你', 0.5f, 0.8f))
        val t = Judge.matchTimes("我爱你", heard)
        assertEquals(0.1f to 0.3f, t[0]); assertNull(t[1]); assertEquals(0.5f to 0.8f, t[2])
    }

    @Test fun repeatedCharactersTakeTheirOwnTimes() {
        val heard = listOf(Triple('妈', 0.1f, 0.3f), Triple('妈', 0.3f, 0.5f))
        val t = Judge.matchTimes("妈妈", heard)
        assertEquals(0.1f to 0.3f, t[0]); assertEquals(0.3f to 0.5f, t[1])
    }

    @Test fun wordsSplitIntoCharacters() {
        val c = Judge.charsOf(listOf(VoskWord("你好", 0.6f, 1.4f, 1f)))
        assertEquals(listOf('你', '好'), c.map { it.first })
        assertEquals(1.0f, c[0].third, 1e-6f); assertEquals(1.0f, c[1].second, 1e-6f)
    }

    @Test fun retonePutsTheMarkInTheRightPlace() {
        assertEquals("táng", Judge.retone("tāng", 2))
        assertEquals("ròu", Judge.retone("rou", 4))
        assertEquals("xiě", Judge.retone("xiè", 3))
        assertEquals("guǐ", Judge.retone("guì", 3))
        assertEquals("lǘ", Judge.retone("lǜ", 2))
        assertEquals("ma", Judge.retone("mā", 5))
    }

    @Test fun charactersVoskOnlyKnowsInWordsStayJoined() {
        assertEquals(listOf("一", "杯", "咖啡"), Judge.tokens("一杯咖啡"))
        assertEquals(listOf("成", "都", "比", "洛杉矶", "冷"), Judge.tokens("成都比洛杉矶冷"))
        assertEquals(listOf("我 爱 你", "我", "爱", "你"), Judge.grammarFor("我爱你"))
    }

    @Test fun phraseToneModelMatchesTheTrainingTool() {
        // Same inputs as the "golden" line printed by tools/tone_proto/phrase_tones.py.
        fun line(a: Float, b: Float) = FloatArray(20) { a + (b - a) * it / 19f }
        val got = Judge.phraseTones(listOf(line(3f, 4f), line(4f, -2f), line(-2f, -3.5f)))
        // Written into the model by the trainer, so this still holds after CI retrains it.
        val want = PhraseToneModel.GOLDEN
        got.zip(want).forEach { (g, w) -> assertEquals(w.first, g!!.first); assertEquals(w.second, g.second, 2e-3f) }
    }

    @Test fun noPitchMeansNoGuess() {
        assertNull(Judge.phraseTones(listOf(FloatArray(20) { 1f }, null))[1])
    }
}
