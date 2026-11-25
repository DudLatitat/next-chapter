package com.dudlatitat.nextchapter.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReadyTest {
    private fun learned(deck: String, n: Int) = (0 until n).map { Card("$deck:x$it", "字", "zì", "w", deck, firstSeen = 1, reps = 2, interval = 3) }

    @Test fun freshStartIsAtTheBottom() {
        val l = Ready.family(AppState())
        assertEquals(0, l.climbed)
        assertEquals("hello", l.next!!.id)
        assertEquals(0, l.percent)
        assertEquals(8, l.rungs.size)
    }

    @Test fun climbsFromWhatYouDid() {
        var s = AppState(cards = learned("family", 12) + learned("daily", 5))
        var l = Ready.family(s)
        assertEquals(1, l.climbed)
        assertEquals("call", l.next!!.id)
        assertEquals(1, l.next!!.missing.size) // the family phrases are there; only the scene is missing
        val most = Talk.scenes.first { it.id == "momcall" }.beats.size * 2
        s = s.copy(dinnerBest = mapOf("momcall" to most - 1))
        l = Ready.family(s)
        assertEquals(2, l.climbed)
        assertTrue(l.percent in 1..99)
    }

    @Test fun scoresBelowSixtyPercentDontCount() {
        val most = Talk.scenes.first { it.id == "arrive" }.beats.size * 2
        val low = Ready.family(AppState(dinnerBest = mapOf("arrive" to (most * 0.5).toInt())))
        assertFalse(low.rungs.first { it.id == "arrive" }.done)
    }

    @Test fun laterWorkStillCountsInThePercent() {
        val s = AppState(numbersBest = 400)
        assertTrue(Ready.family(s).percent > 0)
        assertEquals(0, Ready.family(s).climbed)
    }

    @Test fun everySceneAndThreadItUsesExists() {
        // Would throw if a scene/thread id in the ladder were renamed.
        Ready.family(AppState())
    }
}
