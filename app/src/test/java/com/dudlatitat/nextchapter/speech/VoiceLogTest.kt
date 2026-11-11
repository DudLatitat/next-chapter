package com.dudlatitat.nextchapter.speech

import org.junit.Assert.assertEquals
import org.junit.Test

class VoiceLogTest {
    private fun a(i: Int) = LoggedAttempt(
        time = i.toLong(), hanzi = "你好", pinyin = "nǐ hǎo", expected = listOf(3, 3), accepted = listOf(listOf(2, 3), listOf(3)),
        contours = listOf(List(20) { -1f }, null), guessTone = listOf(2, null), guessConfidence = listOf(0.8f, null),
        baseHz = 120f, calibrated = true,
    )

    @Test fun keepsOnlyTheNewestHundred() {
        var log = emptyList<LoggedAttempt>()
        repeat(130) { log = VoiceLog.withAttempt(log, a(it)) }
        assertEquals(100, log.size)
        assertEquals(30L, log.first().time)
        assertEquals(129L, log.last().time)
    }

    @Test fun survivesARoundTripAndBadFiles() {
        val log = listOf(a(1), a(2))
        assertEquals(log, VoiceLog.decode(VoiceLog.encode(log)))
        assertEquals(emptyList<LoggedAttempt>(), VoiceLog.decode("not json"))
    }
}
