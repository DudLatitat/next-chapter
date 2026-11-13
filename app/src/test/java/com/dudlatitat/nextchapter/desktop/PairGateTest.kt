package com.dudlatitat.nextchapter.desktop

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PairGateTest {
    @Test fun closedUntilThePhoneScreenIsOpen() {
        val g = PairGate()
        assertEquals(PairGate.Closed, g.check(0))
        g.open(); assertEquals(PairGate.Ok, g.check(0))
        g.close(); assertEquals(PairGate.Closed, g.check(0))
    }

    @Test fun fiveWrongCodesLockForLongerEachTime() {
        val g = PairGate().apply { open() }
        repeat(4) { assertFalse(g.wrong(0)) }
        assertTrue("the fifth starts a lockout", g.wrong(0))
        assertEquals(PairGate.Locked(30), g.check(0))
        assertEquals(PairGate.Ok, g.check(30_000))
        repeat(5) { g.wrong(30_000) }
        assertEquals("doubles", PairGate.Locked(60), g.check(30_000))
        // However many tries, never more than an hour.
        var t = 100_000L
        repeat(20) { repeat(5) { g.wrong(t) }; t += 3_600_000 }
        assertEquals(PairGate.Locked(3600), g.check(t - 3_600_000))
    }

    @Test fun pairingResetsTheCount() {
        val g = PairGate().apply { open() }
        repeat(4) { g.wrong(0) }
        g.paired()
        repeat(4) { assertFalse(g.wrong(0)) }
        assertEquals(PairGate.Ok, g.check(0))
    }
}
